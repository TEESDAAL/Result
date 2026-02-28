package result.objectives.ecjProxy;

import java.util.List;

public class DecisionProcessState {
    public Instance getInstance() {
        return new Instance();
    }

    public List<Task> getAllTasks() {
        return List.of();
    }

    public boolean isIncomplete() {
        return false;
    }

    public int numCancelledTasks() {
        return 0;
    }

    public double getCompletionTime() {
        return 0;
    }
}
