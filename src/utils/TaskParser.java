package utils;

import model.Epic;
import model.Subtask;
import model.Task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskParser {
    private static final String LOCAL_DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN);

    public static String toString(Task task) throws IllegalArgumentException {
        if (task == null) {
            throw new IllegalArgumentException();
        }
        int id = task.getId();
        String name = task.getName();
        String description = task.getDescription();
        String status = task.getStatus().name();
        int duration = task.getDuration();
        String startTime = task.getStartTime().format(formatter);

        if (task instanceof Epic) {
            String type = TaskType.EPIC.name();
            return String.format("%d,%s,%s,%s,%s,%d,%s,", id, type, name, status, description, duration, startTime);
        }

        if (task instanceof Subtask) {
            String type = TaskType.SUBTASK.name();
            Subtask subtask = (Subtask) task;
            int epicId = subtask.getEpicId();
            return String.format("%d,%s,%s,%s,%s,%d,%s,%d", id, type, name, status, description, duration, startTime, epicId);
        }

        String type = TaskType.TASK.name();
        return String.format("%d,%s,%s,%s,%s,%d,%s,", id, type, name, status, description, duration, startTime);
    }

    public static Task fromString(String value) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        String[] elements = value.split(",");
        int id = Integer.parseInt(elements[0]);
        TaskType taskType = TaskType.valueOf(elements[1]);
        String name = elements[2];
        Status status = Status.valueOf(elements[3]);
        String description = elements[4];
        int duration = Integer.parseInt(elements[5]);
        LocalDateTime startTime = LocalDateTime.parse(elements[6], formatter);

        if (taskType == TaskType.EPIC) {
            return new Epic(id, name, description, status, startTime);
        }

        if (taskType == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(elements[7]);
            return new Subtask(id, name, description, status, duration, startTime, epicId);
        }

        return new Task(id, name, description, status, duration, startTime);
    }
}
