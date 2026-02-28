package result.objectives;

import result.objectives.ecjProxy.DebugFlags;
import result.objectives.ecjProxy.DecisionProcessState;
import result.objectives.ecjProxy.Policy;
import result.objectives.ecjProxy.Task;

import java.util.List;
import java.util.stream.Collectors;

public enum WeightedResponseTime implements ObjectiveFn {
    SINGLETON;

    @Override
    public double evaluate(DecisionProcessState state, Policy<?, ?> policyHolder) {
        List<Task> tasks = state.getAllTasks();
        double result = 0.0;

        for (Task task : tasks) {
            if(task.isCancelled()){
                continue;
            }

            double timeRemaining = task.getSumOfRemainingDemand();

            int urgencyIndex = task.u1 - 1; // penalise based on the TRUE urgency
            if (state.isIncomplete()) { // solution hit infinite time.
                double contribution = ObjectiveFn.penalisedResponse(urgencyIndex, timeRemaining, true);
                if (DebugFlags.DEBUG_OUTPUT)
                    System.out.println("INCOMPLETE response for t.id: " + task.id + " of: " + contribution);
                result += contribution;
            } else if (timeRemaining > 0.0 || (task.pp == 1 && !task.hasCompletedPP())) { // task failed
                double contribution = ObjectiveFn.penalisedResponse(urgencyIndex, timeRemaining, false);
                if (DebugFlags.DEBUG_OUTPUT)
                    System.out.println("FAILED response for t.id: " + task.id + " of: " + contribution);
                result += contribution;
            } else { // the state and task succeeded
                double time = task.getStartTime() - task.min; // calculate response time.
                double target = ObjectiveFn.targetResponseTime(urgencyIndex); // target response time for this urgency.
                double weight = ObjectiveFn.getWeight(urgencyIndex); // weight of this urgency
                double contribution;
                if (time > target)
                    contribution = (target * weight) + ((time - target) * weight * ObjectiveFn.latePenalty(urgencyIndex));
                else
                    contribution = time * weight;

                if (DebugFlags.DEBUG_OUTPUT)
                    System.out.println("Standard response for t.id: " + task.id + " of: " + contribution + " (response: " + time + ", target: " + target + ", weight: " + weight + ")");

                result += contribution; // we can only respond to the first urgency assignment
            }
        }

        if (DebugFlags.DEBUG_OUTPUT) {
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
        }

        return result / (tasks.size() - state.numCancelledTasks());
    }
}
