package result.objectives;

import result.objectives.ecjProxy.EvolutionState;
import result.objectives.ecjProxy.Instance;
import result.objectives.ecjProxy.Parameter;
import result.objectives.ecjProxy.Setup;

public enum Vectors implements Setup {
    RESPONSE_TIME("target-response-time"),
    HISTORICAL_FREQ("historical-frequency"),
    INCOMPLETE_PENALTY("incomplete-penalty"),
    LATE_PENALTY("late-penalty");

    private double[] value;
    private final String name;
    Vectors(String name) {
        this.name = name;
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.value = readValues(state, base, this.name, Instance.URGENCY_DIM);
    }

    public static void setupAll(EvolutionState state, Parameter base) {
        for (Vectors parameter : values()) {
            parameter.setup(state, base);
        }
    }

    public int length() {
        return value.length;
    }

    public double get(int index) {
        assert index >= 0 && index < value.length;
        return value[index];
    }

    private static double[] readValues(
            EvolutionState state,
            Parameter base,
            String name,
            int dimension) {
        final double[] results = new double[Instance.URGENCY_DIM];

        if (state.parameters.exists(
                base.push(name),
                new Parameter(name))) {
            final double value = state.parameters.getDouble(
                    base.push(name),
                    new Parameter(name));

            for (int urgencyIndex = 0; urgencyIndex < dimension; urgencyIndex++) {
                results[urgencyIndex] = value;
            }
        } else {
            for (int urgencyIndex = 0; urgencyIndex < dimension; urgencyIndex++) {
                final String urgencyParameterName = name + (urgencyIndex + 1);

                final double value = state.parameters.getDouble(
                        base.push(urgencyParameterName),
                        new Parameter(urgencyParameterName));

                results[urgencyIndex] = value;
            }
        }

        return results;
    }
}
