package result.objectives;


import result.objectives.ecjProxy.*;

import java.util.Set;

public sealed interface ObjectiveFn extends Setup permits
        ExponentialCostEqualWeight, ResponseTime,
        WeightedResponseTime, NormalisedWeightedResponseTime,
        SimpleWeightedResponseTime {


    double evaluate(
            DecisionProcessState state,
            Policy<?, ?> policyHolder
    );

    @Override
    default void setup(EvolutionState state, Parameter base) {
        Vectors.setupAll(state, base);
    }
}

