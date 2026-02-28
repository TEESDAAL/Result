package result.objectives.ecjProxy;

public class Parameter {
    public Parameter(String name) {
    }

    public boolean exists(Parameter p1, Parameter p2) {
        return true;
    }

    public Parameter push(String name) {
        return this;
    }

    public double getDouble(Parameter p1, Parameter p2) {
        return 4;
    }
}
