package model;

import utils.Status;

public class Task {
    private static final int ID_INCREMENT_STEP = 1;
    private static final Status DEFAULT_TASK_STATUS = Status.NEW;
    private static int idSequence = 0;

    private static int getNextId() {
        return idSequence += ID_INCREMENT_STEP;
    }
    private final int id;
    private String name;
    private String description;
    private Status status;

    public Task(String name, String description) {
        this.id = getNextId();
        this.name = name;
        this.description = description;
        this.status = DEFAULT_TASK_STATUS;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
