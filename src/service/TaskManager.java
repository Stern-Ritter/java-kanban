package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public boolean addTask(Task task) {
        int id = task.getId();
        Task existingTaskWithThisId = tasks.putIfAbsent(id, task);
        return existingTaskWithThisId == null;
    }

    public boolean addEpic(Epic epic) {
        int id = epic.getId();
        Epic existingEpicWithThisId = epics.putIfAbsent(id, epic);
        return existingEpicWithThisId == null;
    }

    public boolean addSubtask(Subtask subtask) {
        int id = subtask.getId();
        Subtask existingSubtaskWithThisId = subtasks.putIfAbsent(id, subtask);
        return existingSubtaskWithThisId == null;
    }

    public boolean updateTask(Task task) {
        int id = task.getId();
        Task existingTaskWithThisId = tasks.replace(id, task);
        return existingTaskWithThisId != null;
    }

    public boolean updateEpic(Epic epic) {
        int id = epic.getId();
        Epic existingEpicWithThisId = epics.replace(id, epic);
        return existingEpicWithThisId != null;
    }

    public boolean updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        Subtask existingSubtaskWithThisId = subtasks.replace(id, subtask);
        return existingSubtaskWithThisId != null;
    }

    public boolean deleteTaskById(int id) {
        Task existingTaskWithThisId = tasks.remove(id);
        return existingTaskWithThisId != null;
    }

    public boolean deleteEpicById(int id) {
        Epic existingEpicWithThisId = epics.remove(id);
        if(existingEpicWithThisId != null) {
            for(Subtask subtask : existingEpicWithThisId.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
        }
        return existingEpicWithThisId != null;
    }

    public boolean deleteSubtaskById(int id) {
        Subtask existingSubtaskWithThisId = subtasks.remove(id);
        return existingSubtaskWithThisId != null;
    }

    public List<Subtask> getEpicSubtasksById(int id) {
        Epic epic = getEpicById(id);
        if (epic != null) {
            return epic.getSubtasks();
        }
        return Collections.emptyList();
    }
}
