package result.objectives;


import result.objectives.ecjProxy.DebugFlags;
import result.objectives.ecjProxy.DecisionProcessState;
import result.objectives.ecjProxy.Policy;

public enum NormalisedWeightedResponseTime implements ObjectiveFn {
    SINGLETON;

    @Override
    public double evaluate(DecisionProcessState state, Policy<?, ?> policyHolder) {
        double benchmark = state.getInstance().getBenchmark();
        double rawWeightedResponse = WeightedResponseTime.SINGLETON.evaluate(state, policyHolder);
        double normalised = rawWeightedResponse / benchmark;

        if (DebugFlags.DEBUG_OUTPUT) {
            System.out.println("FINAL SOLUTION VALUE ON INSTANCE " + state.getInstance().getName() + ":\n\tBenchmark: " + benchmark + "\n\tRaw Response Fitness: " + rawWeightedResponse + "\n\tNormalised Fitness: " + (rawWeightedResponse / benchmark));
        }
        return normalised;
    }
}
