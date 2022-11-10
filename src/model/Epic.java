package model;

import utils.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Epic extends Task {
    private static final int DEFAULT_EPIC_DURATION = 0;
    private List<Subtask> subtasks;

    public Epic(int id, String name, String description, LocalDateTime startTime) {
        super(id, name, description, DEFAULT_EPIC_DURATION, startTime);
        this.subtasks = new ArrayList<>();
    }

    public Epic(int id, String name, String description, Status status, LocalDateTime startTime) {
        super(id, name, description, status, DEFAULT_EPIC_DURATION, startTime);
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
    public Status getStatus() {
        updateStatus();
        return super.getStatus();
    }

    @Override
    public void setStatus(Status status) {
        updateStatus();
    }


    @Override
    public LocalDateTime getStartTime() {
        if(subtasks.isEmpty())  {
            return super.getStartTime();
        }
        NavigableSet<LocalDateTime> sortedSubtasksStartTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .collect(Collectors.toCollection(TreeSet::new));
        return sortedSubtasksStartTime.first();
    }

    @Override
    public int getDuration() {
        return subtasks.stream()
                .mapToInt(Subtask::getDuration)
                .sum();
    }

    @Override
    public LocalDateTime getEndTime() {
        if(subtasks.isEmpty())  {
            return super.getEndTime();
        }
        NavigableSet<LocalDateTime> sortedSubtasksEndTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .collect(Collectors.toCollection(TreeSet::new));
        return sortedSubtasksEndTime.last();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", subtasks=" + subtasks +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic)) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(getSubtasks(), epic.getSubtasks());
    }
}
