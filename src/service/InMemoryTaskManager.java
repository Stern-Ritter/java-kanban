package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public boolean addTask(Task task) {
        int id = task.getId();
        Task existingTaskWithThisId = tasks.putIfAbsent(id, task);
        return existingTaskWithThisId == null;
    }

    @Override
    public boolean addEpic(Epic epic) {
        int id = epic.getId();
        Epic existingEpicWithThisId = epics.putIfAbsent(id, epic);
        return existingEpicWithThisId == null;
    }

    @Override
    public boolean addSubtask(Subtask subtask) {
        int id = subtask.getId();
        Subtask existingSubtaskWithThisId = subtasks.putIfAbsent(id, subtask);
        return existingSubtaskWithThisId == null;
    }

    @Override
    public boolean updateTask(Task task) {
        int id = task.getId();
        Task existingTaskWithThisId = tasks.replace(id, task);
        return existingTaskWithThisId != null;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        int id = epic.getId();
        Epic existingEpicWithThisId = epics.replace(id, epic);
        return existingEpicWithThisId != null;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        int id = subtask.getId();
        Subtask existingSubtaskWithThisId = subtasks.replace(id, subtask);
        return existingSubtaskWithThisId != null;
    }

    @Override
    public boolean deleteTaskById(int id) {
        Task existingTaskWithThisId = tasks.remove(id);
        if(existingTaskWithThisId != null) {
            historyManager.remove(id);
        }
        return existingTaskWithThisId != null;
    }

    @Override
    public boolean deleteEpicById(int id) {
        Epic existingEpicWithThisId = epics.remove(id);
        if (existingEpicWithThisId != null) {
            for (Subtask subtask : existingEpicWithThisId.getSubtasks()) {
                int subtaskId = subtask.getId();
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
        return existingEpicWithThisId != null;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        Subtask existingSubtaskWithThisId = subtasks.remove(id);
        if(existingSubtaskWithThisId != null) {
            historyManager.remove(id);
        }
        return existingSubtaskWithThisId != null;
    }

    @Override
    public List<Subtask> getEpicSubtasksById(int id) {
        Epic epic = getEpicById(id);
        if (epic != null) {
            return epic.getSubtasks();
        }
        return Collections.emptyList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
