package exception;

public class SameUrlControllerException extends RuntimeException {

    public SameUrlControllerException(String message) {
        super(message);
    }
}
