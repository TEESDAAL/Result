package result;

public class CustomException extends Throwable {
    CustomException() {
        super();
    }
    public int uniqueMethod() {
        return 1;
    }
}
