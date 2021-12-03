package org.ohdsi.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dr.inference.distribution.DistributionLikelihood;
import dr.inference.loggers.Loggable;
import dr.inference.model.CompoundLikelihood;
import dr.inference.model.Likelihood;
import dr.inference.model.Parameter;
import dr.inference.operators.*;
import dr.math.distributions.NormalDistribution;
import org.ohdsi.cyclops.CyclopsLikelihood;

@SuppressWarnings("unused")
public class CyclopsAnalysis implements Analysis {

    private final Likelihood likelihood;
    private final Likelihood prior;
    private final Likelihood joint;

    private final Parameter beta;

    private final OperatorSchedule schedule;

    @SuppressWarnings("unused")
    public CyclopsAnalysis(CyclopsLikelihood cyclopsLikelihood, double priorMean, double priorSd) {

        // Build likelihood
        beta = cyclopsLikelihood.getParameter();

        int defaultThreads = 0; // No thread pools
        likelihood = new CompoundLikelihood(defaultThreads, Collections.singletonList(cyclopsLikelihood));

        // Build prior
        DistributionLikelihood betaPrior = new DistributionLikelihood(new NormalDistribution(priorMean, priorSd));
        betaPrior.addData(beta);

        prior = new CompoundLikelihood(Collections.singletonList(betaPrior));

        // Build joint
        joint = new CompoundLikelihood(Arrays.asList(likelihood, prior));

        // Build transition kernel
        schedule = new SimpleOperatorSchedule(1000, 0.0);
        double defaultWeight = 1.0;
        AdaptationMode mode = AdaptationMode.ADAPTATION_ON;

        RandomWalkOperator.BoundaryCondition condition = RandomWalkOperator.BoundaryCondition.reflecting;
        schedule.addOperator(new RandomWalkOperator(beta, 0.75, condition, defaultWeight, mode));
    }

    @Override
    public List<Loggable> getLoggerColumns() {

        List<Loggable> columns = new ArrayList<>();
        columns.add(likelihood);
        columns.add(prior);
        columns.add(beta);

        return columns;
    }

    @Override
    public Likelihood getJoint() {
        return joint;
    }

    @Override
    public OperatorSchedule getSchedule() {
        return schedule;
    }
}