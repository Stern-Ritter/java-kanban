package utils;

public enum Status {
    NEW("Создана"),
    IN_PROGRESS("В работе"),
    DONE("Выполнена");

    private String description;

    private Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

