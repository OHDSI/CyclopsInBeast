package org.ohdsi.cyclops;

import dr.inference.hmc.GradientWrtParameterProvider;
import dr.inference.model.*;

import java.util.LinkedList;
import java.util.Queue;

public class CyclopsLikelihood extends AbstractModelLikelihood implements GradientWrtParameterProvider {

    private static final boolean DEBUG = true;
    private static final boolean DEBUG_LAZY = true;

    private final CyclopsPtr cyclops;
    private final int dim;
    private final Parameter beta;

    private boolean betaChanged;
//    private boolean storedBetaChanged;
    private final Queue<Integer> betaFlag = new LinkedList<>();

    private double logLikelihood;
    private double storedLogLikelihood;

    private boolean logLikelihoodKnown;
    private boolean storedLogLikelihoodKnown;

    private boolean gradientKnown;
    private boolean storedGradientKnown;

    private double[] gh;
    private double[] storedGh;

    public CyclopsLikelihood(String name, String dataString, String fitString, Parameter parameter) {
        super(name);

        this.cyclops = new CyclopsPtr(dataString, fitString);
        this.dim = cyclops.getBetaSize();
        this.beta = parameter;

        if (beta.getDimension() != dim) {
            beta.setDimension(dim);
        }

        for (int i = 0; i < dim; ++i) {
            beta.setParameterValueQuietly(i, cyclops.getBeta(i));
        }
        beta.fireParameterChangedEvent();

        addVariable(beta);
        betaChanged = false;

        if (DEBUG) {
            System.err.println("cyclopsPtr: " + cyclops);
        }
    }

    private void updateBeta() {
        if (betaChanged) {
            if (betaFlag.isEmpty()) {
                cyclops.setBeta(beta.getParameterValues());
            } else {
                while (!betaFlag.isEmpty()) {
                    final int index = betaFlag.remove();
                    cyclops.setBeta(index, beta.getParameterValue(index));
                }
            }
            betaChanged = false;
        }
    }

//    private debug() {
//
//        System.err.print("Check getBetaSize ...");
//        int len = cyclops.getBetaSize();
//        System.err.println("Done");
//
//        System.err.print("Check getLogLikelihood ...");
//        double ll = cyclops.getLogLikelihood();
//        System.err.println("Done");
//
//        System.err.print("Check getBeta ...");
//        cyclops.getBeta( 0);
//        System.err.println("Done");
//
//        System.err.print("Check getGradient ...");
//        double[] gradient = new double[len * 2];
//        cyclops.getGradientAndHessianDiagonal(gradient);
//        for (int i = 0; i < gradient.length; ++i) {
//            System.err.print(" " + gradient[i]);
//        }
//        System.err.print(" ");
//        System.err.println("Done");
//
//        System.err.print("Check setBeta1 ...");
//        cyclops.setBeta(0, 2.0);
//        System.err.println("Done");
//
//        System.err.print("Check setBeta2 ...");
//        cyclops.setBeta(new double[len]);
//        System.err.println("Done");
//
//        System.err.println("logLike1 = " + ll);
//        System.err.println("logLike1 = " + cyclops.getLogLikelihood());
//
//        System.err.print("Check getGradient ...");
//        cyclops.getGradientAndHessianDiagonal(gradient);
//        for (int i = 0; i < gradient.length; ++i) {
//            System.err.print(" " + gradient[i]);
//        }
//        System.err.print(" ");
//        System.err.println("Done");
//
//    }

    @Override
    public Likelihood getLikelihood() {
        return this;
    }

    @Override
    public Parameter getParameter() {
        return beta;
    }

    @Override
    public int getDimension() {
        return beta.getDimension();
    }

    @Override
    public double[] getGradientLogDensity() {

        updateBeta();

        if (!gradientKnown) {
             computeGh();
        }

        double[] gradient = new double[dim];
        System.arraycopy(gh, 0, gradient, 0, dim);

        return gradient;
    }

    private void computeGh() {
        if (gh == null) {
            gh = new double[2 * dim];
        }

        cyclops.getGradientAndHessianDiagonal(gh);
    }

    @Override
    public Model getModel() {
        return this;
    }

    @Override
    public double getLogLikelihood() {

        updateBeta();

        if (!logLikelihoodKnown) {
            logLikelihood = computeLogLikelihood();
            logLikelihoodKnown = true;
        }

        if (DEBUG_LAZY) {
            checkLazySettings();
        }

        return logLikelihood;
    }

    private void checkLazySettings() {
        // TODO
    }

    @Override
    public void makeDirty() {
        betaChanged = true;
        betaFlag.clear();
    }

    @Override
    protected void handleModelChangedEvent(Model model, Object object, int index) {
        // Do nothing
    }

    @Override
    protected void handleVariableChangedEvent(Variable variable, int index, Variable.ChangeType type) {
        if (variable == beta) {
            betaChanged = true;
            if (type == Variable.ChangeType.ALL_VALUES_CHANGED) {
                betaFlag.clear();
            } else {
                betaFlag.add(index);
            }
        } else {
            throw new IllegalArgumentException("Unknown variable in Cyclops");
        }
    }

    @Override
    protected void storeState() {
        storedLogLikelihoodKnown = logLikelihoodKnown;
        storedGradientKnown = gradientKnown;

        storedLogLikelihood = logLikelihood;

        if (gh != null) {
            if (storedGh == null) {
                storedGh = new double[gh.length];
            }
            System.arraycopy(gh, 0, storedGh, 0, gh.length);
        }
    }

    @Override
    protected void restoreState() {
        logLikelihood = storedLogLikelihood;

        logLikelihoodKnown = storedLogLikelihoodKnown;
        gradientKnown = storedGradientKnown;

        double[] tmp = storedGh;
        storedGh = gh;
        gh = tmp;
    }

    @Override
    protected void acceptState() {
        // Do nothing
    }

    private double computeLogLikelihood() {
        return cyclops.getLogLikelihood();
    }
}
