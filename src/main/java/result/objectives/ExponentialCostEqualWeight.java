package result.objectives;


import result.objectives.ecjProxy.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static result.objectives.ObjectiveFnUtil.*;

public enum ExponentialCostEqualWeight implements ObjectiveFn {
    SINGLETON;

    @Override
    public double evaluate(DecisionProcessState state, Policy<?, ?> policyHolder) {
        List<Task> tasks = state.getAllTasks();

        int numUrgencies = Instance.URGENCY_DIM;

        int[] counts = new int[numUrgencies];
        HashMap<Integer, ArrayList<Double>> allResults = new HashMap<>(); // urgencyIndex --> set of contributions

        for(int i = 0; i < numUrgencies; i++){
            allResults.put(i, new ArrayList<>());
        }

        for (Task task : tasks) {

            // if this task was received in either the warm-up or cool-down periods, skip.
            double timeOfReceipt = task.min;
            if(timeOfReceipt < 0.0 || timeOfReceipt > DecisionProcess.TWENTY_FOUR_HOURS){
                continue;
            }

            int urgencyIndex = task.u1 - 1; // penalise based on the TRUE urgency

            // if this task was cancelled, we don't consider it in the penalty function.
            if(task.isCancelled()){
                continue;
            }

            counts[urgencyIndex]++;

            double timeRemaining = task.getSumOfRemainingDemand();
//            int urgencyIndex = task.u1 - 1;
            if (state.isIncomplete()) { // solution hit infinite time.
                double contribution = Vectors.RESPONSE_TIME.get(urgencyIndex) * Vectors.INCOMPLETE_PENALTY.get(urgencyIndex);
                allResults.get(urgencyIndex).add(contribution);
                if (DebugFlags.DEBUG_OUTPUT)
                    System.out.println("INCOMPLETE response for t.id: " + task.id + " of: " + contribution);
            } else if (timeRemaining > 0.0 || (task.pp == 1 && !task.hasCompletedPP())) { // task failed
                double contribution = timeRemaining *  Vectors.INCOMPLETE_PENALTY.get(urgencyIndex);
                allResults.get(urgencyIndex).add(contribution);
                if (DebugFlags.DEBUG_OUTPUT)
                    System.out.println("FAILED response for t.id: " + task.id + " of: " + contribution);
            } else { // the state and task succeeded
                double responseTime = task.getStartTime() - task.min; // calculate response time.
                double target = Vectors.RESPONSE_TIME.get(urgencyIndex); // target response time for this urgency.
                double contribution = getExponentialRelativeResponsePenalty(responseTime, target);

                if (DebugFlags.DEBUG_OUTPUT) {
                    System.out.println("Standard response for t.id: " + task.id + " of: " + contribution + " (response: " + responseTime + ", target: " + target + ", contribution: " + contribution + ")");
                }

                allResults.get(urgencyIndex).add(contribution); // we can only respond to the first urgency assignment
            }
        }

        double[] output = new double[numUrgencies];
        for(int urgencyIndex = 0; urgencyIndex < numUrgencies; urgencyIndex++){
            double divisor = counts[urgencyIndex];
            if(divisor > 0.0) {
                double[] data = allResults.get(urgencyIndex).stream().mapToDouble(Double::doubleValue).toArray();
                output[urgencyIndex] += Utility.calcMean(data);
                output[urgencyIndex] += Utility.calcStandardDeviation(data);
            } // else: nothing, because a divide by zero is bad.
        }

        if (DebugFlags.DEBUG_OUTPUT)
            // If your fitness numbers aren't matching what they're supposed to be, check
            // the output of this to find out how the Tasks were dealt with.
            System.err.println(
                    tasks.stream()
                            .sorted((a, b) -> Double.compare(a.min, b.min))
                            .map(
                                    task -> "Task " +
                                            task.id +
                                            " @ " +
                                            task.min +
                                            ": [" +
                                            task.getStartTime() +
                                            " -> " +
                                            task.getFinishTime() +
                                            "]")
                            .collect(Collectors.joining("\n")));

        return Utility.maxValueInArray(output);
    }

    public static double getExponentialRelativeResponsePenalty(double responseTime, double targetResponseTime){
        assert responseTime >= 0.0; // if you happen to be *at* the emergency at the time it was received, this *can* be zero.
        assert targetResponseTime > 0.0; // whereas we should never have a target of zero.
        return Math.pow(responseTime / targetResponseTime, 2);
    }
}
