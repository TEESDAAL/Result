package result.objectives;


import result.objectives.ecjProxy.DecisionProcessState;
import result.objectives.ecjProxy.Policy;
import result.objectives.ecjProxy.Task;

import java.util.List;

import static result.objectives.ObjectiveFnUtil.*;


public enum SimpleWeightedResponseTime implements ObjectiveFn {
    SINGLETON;

    @Override
    public double evaluate(DecisionProcessState state, Policy<?, ?> policyHolder) {
        List<Task> tasks = state.getAllTasks();
        double result = 0.0;

        for (Task task : tasks) { // to emulate a rolling start from minute x, just don't include tasks with t.min < x in this calculation
            double timeRemaining = task.getSumOfRemainingDemand();
            int urgencyIndex = task.getUrgency() - 1;

            if (timeRemaining > 0.0 || (task.pp == 1 && !task.hasCompletedPP())) {
                double contribution = simplePenalisedResponse(urgencyIndex);
                result += contribution;
            } else {
                double contribution = (task.getStartTime() - task.min) * getWeight(task.getUrgency() - 1) * ObjectiveFn.overtimeScalar(state.getCompletionTime());

                result += contribution; // we can only respond to the first urgency assignment
            }
        }

        return result / tasks.size();
    }


}
