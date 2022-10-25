package utils;

public enum TaskType {
    TASK("Задача"),
    EPIC("Эпик"),
    SUBTASK("Подзадача");

    private String description;

    private TaskType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
