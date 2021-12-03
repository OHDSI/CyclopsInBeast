package org.ohdsi.methods;

    import java.util.ArrayList;
import java.util.List;

    import dr.inference.loggers.*;
    import dr.inference.markovchain.MarkovChain;
import dr.inference.mcmc.MCMC;
import dr.inference.mcmc.MCMCOptions;
import dr.inference.model.Likelihood;
import dr.inference.operators.OperatorSchedule;
import dr.inference.trace.Trace;
import dr.inference.trace.TraceCorrelation;
import dr.math.MathUtils;

    public class Runner {

        private final Likelihood joint;
        private final OperatorSchedule schedule;
        private final Logger[] logger;

        final long chainLength;
        final int burnIn;
        final int subSampleFrequency;
        int consoleWidth = 175;

        public Runner(Analysis analysis, int chainLength, int burnIn, int subSampleFrequency, double seed) {

            // Set seed
            MathUtils.setSeed(Math.round(seed));

            this.joint = analysis.getJoint();
            this.schedule = analysis.getSchedule();
            this.logger = getLogger(analysis.getLoggerColumns(), subSampleFrequency);

            this.chainLength = chainLength;
            this.burnIn = burnIn;
            this.subSampleFrequency = subSampleFrequency;
        }

        private Logger[] getLogger(List<Loggable> columns, int subSampleFrequency) {

            ArrayLogFormatter formatter = new ArrayLogFormatter(false);

            MCLogger memory = new MCLogger(formatter, subSampleFrequency, false);
            for (Loggable column : columns) {
                memory.add(column);
            }

//            MCLogger screen = new MCLogger(new TabDelimitedFormatter(System.out), subSampleFrequency, true,
//                    subSampleFrequency);
//            for (Loggable column : columns) {
//                screen.add(column);
//            }

            Logger progressBar = new Logger() {
                @Override
                public void startLogging() {
                }

                @Override
                public void log(long iteration) {
                    if (iteration % 10000 == 0) {
                        progressPercentage(iteration, chainLength);
                    }
                }

                private void progressPercentage(long done, long total) {
                    if (done > total) {
                        throw new IllegalArgumentException();
                    }
                    int barSize = consoleWidth - 10;
                    int donePercent = (int) (100 * done / total);
                    int doneChars = (int) (barSize * done / total);
                    String bar = "|" + new String(new char[doneChars]).replace('\0', '=')
                            + new String(new char[barSize - doneChars]).replace('\0', ' ') + "|";
                    System.out.print("\r" + bar + " " + donePercent + "%");
                    if (done == total) {
                        System.out.print("\n");
                    }
                }

                @Override
                public void stopLogging() {
                }
            };

//		return new Logger[] { memory, screen, progressBar };
		return new Logger[] { memory, progressBar };
        }

        public void run() {
            MCMC mcmc = new MCMC("mcmc1");
            mcmc.setShowOperatorAnalysis(true);
            mcmc.init(getOptions(chainLength), joint, schedule, logger);
            mcmc.run();
        }

        public void setConsoleWidth(int consoleWidth) {
            this.consoleWidth = consoleWidth;
        }

        public String[] getParameterNames() {
            List<Trace> traces = getTraces(logger[0]);
            String[] parameterNames = new String[traces.size() - 1];
            for (int i = 1; i < traces.size(); ++i) {
                Trace trace = traces.get(i);
                parameterNames[i - 1] = trace.getName();
            }
            return parameterNames;
        }

        public double[] getTrace(int parameterIndex) {
            List<Trace> traces = getTraces(logger[0]);
            if (parameterIndex < 0 || parameterIndex >= traces.size())
                throw new RuntimeException("Parameter index out of range. Maximum index = " + traces.size());
            Trace trace = traces.get(parameterIndex);
            int start = burnIn / subSampleFrequency + 1;
            List<Double> values = trace.getValues(start, trace.getValueCount());
            double[] primitives = new double[values.size()];
            for (int i = 0; i < values.size(); i++)
                primitives[i] = values.get(i);
            return primitives;
        }

        public void processSamples() {

            System.out.println("name mean sd ess count");
            List<Trace> traces = getTraces(logger[0]);
            for (int i = 1; i < traces.size(); ++i) {
                Trace trace = traces.get(i);
                int start = burnIn / subSampleFrequency + 1;
                List<Double> values = trace.getValues(start, trace.getValueCount()); // Return these to R

                // Return 'values' to R
                TraceCorrelation statistics = new TraceCorrelation(values, trace.getTraceType(), subSampleFrequency);
                System.out.println(trace.getName() + " " + statistics.getMean() + " " + statistics.getStdError() + " "
                        + statistics.getESS() + " " + statistics.getSize());
            }
        }

        private static MCMCOptions getOptions(long chainLength) {

            boolean useAdaptation = true;
            long adaptationDelay = chainLength / 100;
            double adaptationTarget = 0.234;
            boolean useSmoothAcceptanceRatio = false;
            double temperature = 1.0;
            long fullEvaluationCount = 1000;
            double evaluationTestThreshold = MarkovChain.EVALUATION_TEST_THRESHOLD;
            int minOperatorCountForFullEvaluation = 1;

            return new MCMCOptions(chainLength, fullEvaluationCount, minOperatorCountForFullEvaluation,
                    evaluationTestThreshold, useAdaptation, adaptationDelay, adaptationTarget, useSmoothAcceptanceRatio,
                    temperature);
        }

        private List<Trace> getTraces(Logger logger) {
            List<Trace> traceList = new ArrayList<>();
            if (logger instanceof MCLogger) {
                for (LogFormatter f : ((MCLogger) logger).getFormatters()) {
                    if (f instanceof ArrayLogFormatter) {
                        traceList.addAll(((ArrayLogFormatter) f).getTraces());
                    }
                }
            }
            return traceList;
        }
}
