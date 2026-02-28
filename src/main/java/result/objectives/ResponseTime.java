package result.objectives;


import result.objectives.ecjProxy.DecisionProcessState;
import result.objectives.ecjProxy.Policy;
import result.objectives.ecjProxy.Task;

import java.util.List;

public enum ResponseTime implements ObjectiveFn {
    SINGLETON;

    @Override
    public double evaluate(DecisionProcessState state, Policy<?, ?> policyHolder) {
        List<Task> tasks = state.getAllTasks();

        double result = 0.0;
        for (Task task : tasks) { // to emulate a rolling start from minute x, just don't include tasks with t.min < x in this calculation
            if (task.hasRemainingDemand()) {
                throw new IllegalStateException("TASK INCOMPLETE: " + task.id);
            }

            result += task.getStartTime() - task.min;
        }

        return result / tasks.size();
    }
}
