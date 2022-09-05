package utils;

public enum Status {
    NEW("Создана"),
    IN_PROGRESS("В работе"),
    DONE("Выполнена");

    private final String description;

    private Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

