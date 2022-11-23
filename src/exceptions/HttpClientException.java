package exceptions;

public class HttpClientException extends RuntimeException {
    public HttpClientException() {
        super();
    }

    public HttpClientException(String message) {
        super(message);
    }

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
