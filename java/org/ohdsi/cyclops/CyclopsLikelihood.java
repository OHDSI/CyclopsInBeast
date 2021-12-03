package org.ohdsi.cyclops;

import dr.inference.hmc.GradientWrtParameterProvider;
import dr.inference.model.*;

import java.util.LinkedList;
import java.util.Queue;

public class CyclopsLikelihood extends AbstractModelLikelihood implements GradientWrtParameterProvider {

    private static final boolean DEBUG = false;
    private static final boolean DEBUG_LAZY = false;

    private final CyclopsPtr cyclops;
    private final int dim;
    private final Parameter beta;

    private boolean betaChanged;
    private boolean storedBetaChanged;
    private final Queue<Integer> betaFlag = new LinkedList<>();

    private double logLikelihood;
    private double storedLogLikelihood;

    private boolean logLikelihoodKnown;
    private boolean storedLogLikelihoodKnown;

    private boolean gradientKnown;
    private boolean storedGradientKnown;

    private double[] gh;
    private double[] storedGh;

    private int computeLikelihoodCount = 0;
    private int computeGradientCount = 0;

    public CyclopsLikelihood(String name, String dataString, String fitString, Parameter parameter) {
        super(name);

        this.cyclops = new CyclopsPtr(dataString, fitString);
        this.dim = cyclops.getBetaSize();
        this.beta = parameter;

        if (beta.getDimension() != dim) {
            beta.setDimension(dim);
        }

        beta.addBounds(new Parameter.DefaultBounds(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                beta.getDimension()));

        for (int i = 0; i < dim; ++i) {
            beta.setParameterValueQuietly(i, cyclops.getBeta(i));
        }
        beta.fireParameterChangedEvent();

        addVariable(beta);
        betaChanged = false;

        logLikelihood = cyclops.getLogLikelihood();
        logLikelihoodKnown = true;

        if (DEBUG) {
            System.err.println("cyclopsPtr: " + cyclops);
        }
    }

    @SuppressWarnings("unused")
    public int[] getCounts() {
        return new int[] { computeLikelihoodCount, computeGradientCount };
    }

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
        storedBetaChanged = betaChanged;

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
        logLikelihoodKnown = storedLogLikelihoodKnown;
        gradientKnown = storedGradientKnown;
        betaChanged = storedBetaChanged;

        logLikelihood = storedLogLikelihood;

        double[] tmp = storedGh;
        storedGh = gh;
        gh = tmp;
    }

    @Override
    protected void acceptState() {
        // Do nothing
    }

    private void updateBeta() {
        if (betaChanged) {
            if (betaFlag.isEmpty()) {
                if (DEBUG) {
                    System.err.println("Setting all beta");
                }
                cyclops.setBeta(beta.getParameterValues());
            } else {
                while (!betaFlag.isEmpty()) {
                    final int index = betaFlag.remove();
                    if (DEBUG) {
                        System.err.println("Setting beta dimension " + index);
                    }
                    cyclops.setBeta(index, beta.getParameterValue(index));
                }
            }
            betaChanged = false;
            logLikelihoodKnown = false;
            gradientKnown = false;
        }
    }

    private double computeLogLikelihood() {
        ++computeLikelihoodCount;
        return cyclops.getLogLikelihood();
    }

    private void computeGh() {
        ++computeGradientCount;
        if (gh == null) {
            gh = new double[2 * dim];
        }

        cyclops.getGradientAndHessianDiagonal(gh);
    }

    private void checkLazySettings() {
        // TODO
    }
}
