package result;

/**
 * Represents an operation that does not return a result which can error.
 * This is a functional interface whose functional method is runThrows().
 *
 * @author Alan Teesdale (300652164)
 */
public interface FallibleRunnable extends Runnable {
    @Override
    default void run() {
        try {
            this.runThrows();
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the operation.
     *
     * @throws Exception if the operation errors
     */
    void runThrows() throws Throwable;
}


