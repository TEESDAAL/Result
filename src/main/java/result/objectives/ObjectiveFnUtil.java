package result.objectives;

public class ObjectiveFnUtil {
    // could cache this fairly easily
    static double getWeight(int urgencyIndex) {
        assert urgencyIndex >= 0 && urgencyIndex < Vectors.RESPONSE_TIME.length();
        assert urgencyIndex < Vectors.HISTORICAL_FREQ.length();

        // TODO: THIS CHANGE BREAKS TESTS
//        return 1 / (Math.pow(targetResponse[urgencyIndex], 2) * historicalFreq[urgencyIndex]);
        return 1 / (Math.pow( Vectors.RESPONSE_TIME.get(urgencyIndex), 2));
    }

    static double simplePenalisedResponse(int urgencyIndex) {
        return getWeight(urgencyIndex) * Vectors.INCOMPLETE_PENALTY.get(urgencyIndex);
    }

    static double overtimeScalar(double completionTime) {
        if (Double.compare(completionTime, (24 * 60)) < 0)
            return 1.0;
        else
            return completionTime / (24 * 60);
    }

    static double penalisedResponse(int urgencyIndex, double timeRemaining, boolean isIncomplete) {
        assert urgencyIndex >= 0 && urgencyIndex < Vectors.RESPONSE_TIME.length();
        return getWeight(urgencyIndex) * (isIncomplete ? Vectors.RESPONSE_TIME.get(urgencyIndex) : timeRemaining) * incompletePenalty(urgencyIndex);
    }
}
