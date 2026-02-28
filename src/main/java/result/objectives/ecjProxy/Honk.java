package result.objectives.ecjProxy;

public interface Honk {
    void foo();
}

interface LoggedHonk extends Honk {
    default void foo() {
        System.out.println("System.out is basically logging");
        bar();
    }

    void bar();
}