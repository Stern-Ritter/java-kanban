package model;

import utils.Status;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        this.subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
        updateStatus();
    }

    protected void updateStatus() {
        if (this.subtasks.size() == 0) {
            super.setStatus(Status.NEW);
        }

        boolean hasOnlyNewTask = this.subtasks.stream().allMatch(task -> task.getStatus() == Status.NEW);
        boolean hasOnlyDoneTask = this.subtasks.stream().allMatch(task -> task.getStatus() == Status.DONE);
        if (hasOnlyNewTask) {
            super.setStatus(Status.NEW);
        } else if (hasOnlyDoneTask) {
            super.setStatus(Status.DONE);
        } else {
            super.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public void setStatus(Status status) {
        updateStatus();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasks=" + subtasks +
                '}';
    }
}
