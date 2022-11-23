package utils;

public enum ResponseStatus {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    INTERNAL_SERVER_ERROR(500);

    private int code;

    private ResponseStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
