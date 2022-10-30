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
    public static final int TASK_ID_INITIAL_VALUE = 0;
    public static final int TASK_ID_INCREMENT_STEP = 1;
    private int taskIdSequence;

    private final Map<Integer, Task> tasksStorage;
    private final Map<Integer, Epic> epicsStorage;
    private final Map<Integer, Subtask> subtasksStorage;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.taskIdSequence = TASK_ID_INITIAL_VALUE;
        this.historyManager = historyManager;
        this.tasksStorage = new HashMap<>();
        this.epicsStorage = new HashMap<>();
        this.subtasksStorage = new HashMap<>();
    }

    public void setTaskIdSequence(int taskIdSequence) {
        this.taskIdSequence = taskIdSequence;
    }

    public int getNextTaskId() {
        return taskIdSequence += TASK_ID_INCREMENT_STEP;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasksStorage.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicsStorage.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasksStorage.values());
    }

    @Override
    public void deleteAllTasks() {
        tasksStorage.clear();
    }

    @Override
    public void deleteAllEpics() {
        epicsStorage.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasksStorage.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasksStorage.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epicsStorage.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasksStorage.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public boolean addTask(Task task) {
        if (task == null) return false;

        int id = task.getId();
        Task existingTaskWithThisId = tasksStorage.putIfAbsent(id, task);
        return existingTaskWithThisId == null;
    }

    @Override
    public boolean addEpic(Epic epic) {
        if (epic == null) return false;

        int id = epic.getId();
        Epic existingEpicWithThisId = epicsStorage.putIfAbsent(id, epic);
        return existingEpicWithThisId == null;
    }

    @Override
    public boolean addSubtask(Subtask subtask) {
        if (subtask == null) return false;

        int id = subtask.getId();
        Subtask existingSubtaskWithThisId = subtasksStorage.putIfAbsent(id, subtask);
        return existingSubtaskWithThisId == null;
    }

    @Override
    public boolean updateTask(Task task) {
        if (task == null) return false;

        int id = task.getId();
        Task existingTaskWithThisId = tasksStorage.replace(id, task);
        return existingTaskWithThisId != null;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epic == null) return false;

        int id = epic.getId();
        Epic existingEpicWithThisId = epicsStorage.replace(id, epic);
        return existingEpicWithThisId != null;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        if (subtask == null) return false;

        int id = subtask.getId();
        Subtask existingSubtaskWithThisId = subtasksStorage.replace(id, subtask);
        return existingSubtaskWithThisId != null;
    }

    @Override
    public boolean deleteTaskById(int id) {
        Task existingTaskWithThisId = tasksStorage.remove(id);
        if (existingTaskWithThisId != null) {
            historyManager.remove(id);
        }
        return existingTaskWithThisId != null;
    }

    @Override
    public boolean deleteEpicById(int id) {
        Epic existingEpicWithThisId = epicsStorage.remove(id);
        if (existingEpicWithThisId != null) {
            for (Subtask subtask : existingEpicWithThisId.getSubtasks()) {
                int subtaskId = subtask.getId();
                subtasksStorage.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
        return existingEpicWithThisId != null;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        Subtask existingSubtaskWithThisId = subtasksStorage.remove(id);
        if (existingSubtaskWithThisId != null) {
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

    protected HistoryManager getHistoryManager() {
        return historyManager;
    }

    protected Map<Integer, Epic> getEpicsStorage() {
        return epicsStorage;
    }

    protected Map<Integer, Task> getAllTasksStorage() {
        Map<Integer, Task> tasks = new HashMap<>();
        tasks.putAll(tasksStorage);
        tasks.putAll(epicsStorage);
        tasks.putAll(subtasksStorage);
        return tasks;
    }

    protected List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(getTasks());
        tasks.addAll(getEpics());
        tasks.addAll(getSubtasks());
        return tasks;
    }
}
