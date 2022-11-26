package exceptions;

public class BadMethodException extends HttpException{
    public BadMethodException() {
    }

    public BadMethodException(String message) {
        super(message);
    }

    public BadMethodException(String message, Throwable cause) {
        super(message, cause);
    }
}
