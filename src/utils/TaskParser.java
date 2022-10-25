package utils;

import model.Epic;
import model.Subtask;
import model.Task;

public class TaskParser {
    public static String toString(Task task) throws IllegalArgumentException {
        if (task == null) {
            throw new IllegalArgumentException();
        }

        int id = task.getId();
        String name = task.getName();
        String description = task.getDescription();
        String status = task.getStatus().name();

        if (task instanceof Epic) {
            String type = TaskType.EPIC.name();
            return String.format("%d,%s,%s,%s,%s,", id, type, name, status, description);
        }

        if (task instanceof Subtask) {
            String type = TaskType.SUBTASK.name();
            Subtask subtask = (Subtask) task;
            int epicId = subtask.getEpicId();
            return String.format("%d,%s,%s,%s,%s,%d", id, type, name, status, description, epicId);
        }

        String type = TaskType.TASK.name();
        return String.format("%d,%s,%s,%s,%s,", id, type, name, status, description);
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

        if (taskType == TaskType.EPIC) {
            return new Epic(id, name, description, status);
        }

        if (taskType == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(elements[5]);
            return new Subtask(id, name, description, status, epicId);
        }

        return new Task(id, name, description, status);
    }
}
