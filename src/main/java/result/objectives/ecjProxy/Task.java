package result.objectives.ecjProxy;

public class Task {
    public double min =0;
    public int pp;
    public String id;
    public int u1;

    public boolean isCancelled() {
        return false;
    }

    public double getSumOfRemainingDemand() {
        return 0;
    }
    public boolean hasCompletedPP() {
        return false;
    }

    public double getStartTime() {
        return 0;
    }

    public double getFinishTime() {
        return 1;
    }

    public boolean hasRemainingDemand() {
        return false;
    }

    public int getUrgency() {
        return 0;
    }
}
