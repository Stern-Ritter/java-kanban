package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {
    public static final int TASK_ID_INITIAL_VALUE = 0;
    public static final int TASK_ID_INCREMENT_STEP = 1;
    private static final long DEFAULT_DATE_TIME_MILLIS = 0L;
    private static final Comparator<Task> startDateComparator = (firstDateTime, secondDateTime) -> {
        long firstDateMillis = Optional.of(firstDateTime.getStartTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()).orElse(DEFAULT_DATE_TIME_MILLIS);
        long secondDateMillis = Optional.of(secondDateTime.getStartTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli()).orElse(DEFAULT_DATE_TIME_MILLIS);
        return Long.compare(firstDateMillis, secondDateMillis);
    };

    private static final String ADD_TASK_INTERSECTION_ERROR_TEXT = "Добавляемая задача пересекается по времени с уже созданными.";
    private static final String UPDATE_TASK_INTERSECTION_ERROR_TEXT = "Обновляемая задача пересекается по времени с уже созданными.";

    private int taskIdSequence;
    private final Map<Integer, Task> tasksStorage;
    private final Map<Integer, Epic> epicsStorage;
    private final Map<Integer, Subtask> subtasksStorage;
    private final Set<Task> prioritizedTasksStorage;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.taskIdSequence = TASK_ID_INITIAL_VALUE;
        this.historyManager = historyManager;
        this.tasksStorage = new HashMap<>();
        this.epicsStorage = new HashMap<>();
        this.subtasksStorage = new HashMap<>();
        this.prioritizedTasksStorage = new TreeSet<>(startDateComparator);
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
        prioritizedTasksStorage.removeIf(task -> !(task instanceof Subtask));
    }

    @Override
    public void deleteAllEpics() {
        epicsStorage.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasksStorage.clear();
        prioritizedTasksStorage.removeIf(task -> task instanceof Subtask);
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
        if(isTaskOverlapping(task)) {
            throw new IllegalArgumentException(ADD_TASK_INTERSECTION_ERROR_TEXT);
        }

        int id = task.getId();
        Task existingTaskWithThisId = tasksStorage.putIfAbsent(id, task);
        prioritizedTasksStorage.add(task);
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
        if(isTaskOverlapping(subtask)) {
            throw new IllegalArgumentException(ADD_TASK_INTERSECTION_ERROR_TEXT);
        }

        int id = subtask.getId();
        Subtask existingSubtaskWithThisId = subtasksStorage.putIfAbsent(id, subtask);
        prioritizedTasksStorage.add(subtask);
        return existingSubtaskWithThisId == null;
    }

    @Override
    public boolean updateTask(Task task) {
        if (task == null) return false;
        if(isTaskOverlapping(task)) {
            throw new IllegalArgumentException(UPDATE_TASK_INTERSECTION_ERROR_TEXT);
        }

        int id = task.getId();
        Task existingTaskWithThisId = tasksStorage.replace(id, task);
        prioritizedTasksStorage.remove(task);
        prioritizedTasksStorage.add(task);
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
        if(isTaskOverlapping(subtask)) {
            throw new IllegalArgumentException(UPDATE_TASK_INTERSECTION_ERROR_TEXT);
        }

        int id = subtask.getId();
        Subtask existingSubtaskWithThisId = subtasksStorage.replace(id, subtask);
        prioritizedTasksStorage.remove(subtask);
        prioritizedTasksStorage.add(subtask);
        return existingSubtaskWithThisId != null;
    }

    @Override
    public boolean deleteTaskById(int id) {
        Task existingTaskWithThisId = tasksStorage.remove(id);
        if (existingTaskWithThisId != null) {
            prioritizedTasksStorage.remove(existingTaskWithThisId);
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
                deleteSubtaskById(subtaskId);
            }
            historyManager.remove(id);
        }
        return existingEpicWithThisId != null;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        Subtask existingSubtaskWithThisId = subtasksStorage.remove(id);
        if (existingSubtaskWithThisId != null) {
            prioritizedTasksStorage.remove(existingSubtaskWithThisId);
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

    @Override
    public List<Task> getPrioritizedTasks() {
        List<Task> prioritizedTasks = new ArrayList<>(prioritizedTasksStorage.size());
        prioritizedTasks.addAll(prioritizedTasksStorage);
        return prioritizedTasks;
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

    private boolean isTaskOverlapping(Task addedTask) {
        if(addedTask == null) {
            return false;
        }
        int addedTaskId = addedTask.getId();
        LocalDateTime addedTaskStartTime = addedTask.getStartTime();
        LocalDateTime addedTaskEndTime = addedTask.getEndTime();
        return prioritizedTasksStorage.stream()
                .anyMatch(savedTask -> addedTaskId != savedTask.getId() &&
                        addedTaskStartTime.compareTo(savedTask.getEndTime()) <= 0 &&
                        savedTask.getStartTime().compareTo(addedTaskEndTime) <= 0);
    }
}
