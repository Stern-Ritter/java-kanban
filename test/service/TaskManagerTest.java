package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class TaskManagerTest<T extends TaskManager> {
    private final int TASK_DURATION = 30;
    private static final LocalDateTime FIRST_TASK_START_TIME = LocalDateTime.parse("08.11.2022 11:00",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime SECOND_TASK_START_TIME = LocalDateTime.parse("08.11.2022 11:40",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime FIRST_EPIC_START_TIME = LocalDateTime.parse("08.11.2022 13:00",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime SECOND_EPIC_START_TIME = LocalDateTime.parse("08.11.2022 15:00",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime FIRST_SUBTASK_START_TIME = LocalDateTime.parse("08.11.2022 13:00",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime SECOND_SUBTASK_START_TIME = LocalDateTime.parse("08.11.2022 13:31",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

    T taskManager;

    abstract T getTaskManager();

    @BeforeEach
    void setUpTest() {
        taskManager = getTaskManager();
    }

    @Test
    void getNextTaskIdWhenCalledOnce() {
        final int expectedValue = 1;
        final int value = taskManager.getNextTaskId();
        assertEquals(expectedValue, value, "The id sequence value is incorrect.");
    }

    @Test
    void getNextTaskIdWhenCalledTwice() {
        taskManager.getNextTaskId();

        final int expectedValue = 2;
        final int value = taskManager.getNextTaskId();
        assertEquals(expectedValue, value, "The id sequence value is incorrect.");
    }

    @Test
    void getTasksWhenTasksListContainsValues() {
        final int firstTaskId = taskManager.getNextTaskId();
        final int secondTaskId = taskManager.getNextTaskId();
        final Task firstTask = new Task(firstTaskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(secondTaskId, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION ,SECOND_TASK_START_TIME);
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);

        final List<Task> expectedTasks = List.of(firstTask, secondTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = taskManager.getTasks();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void getTasksWhenTasksListIsEmpty() {
        final List<Task> expectedTasks = Collections.emptyList();
        final int expectedTasksCount = 0;
        final List<Task> tasks = taskManager.getTasks();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void getEpicsWhenEpicsListContainsValues() {
        final int firstEpicId = taskManager.getNextTaskId();
        final int secondEpicId = taskManager.getNextTaskId();
        final Epic firstEpic = new Epic(firstEpicId, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(secondEpicId, "Купить лежак для кошки.", "Купить лежак для кошки на подоконник.",
                SECOND_EPIC_START_TIME);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);

        final List<Epic> expectedEpics = List.of(firstEpic, secondEpic);
        final int expectedEpicsCount = expectedEpics.size();
        final List<Epic> epics = taskManager.getEpics();
        final int epicsCount = epics.size();

        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }

    @Test
    void getEpicsWhenEpicsListIsEmpty() {
        final List<Epic> expectedEpics = Collections.emptyList();
        final int expectedEpicsCount = 0;
        final List<Epic> epics = taskManager.getEpics();
        final int epicsCount = epics.size();

        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }

    @Test
    void getSubtasksWhenSubtasksListContainsValues() {
        final int epicId = taskManager.getNextTaskId();
        final int firstSubtaskId = taskManager.getNextTaskId();
        final int secondSubtaskId = taskManager.getNextTaskId();
        final Subtask firstSubtask = new Subtask(firstSubtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        final Subtask secondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, epicId);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);

        final List<Subtask> expectedSubtasks = List.of(firstSubtask, secondSubtask);
        final int expectedSubtasksCount = expectedSubtasks.size();
        final List<Subtask> subtasks = taskManager.getSubtasks();
        final int subtasksCount = subtasks.size();

        assertNotNull(subtasks, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void getSubtasksWhenSubtasksListIsEmpty() {
        final List<Subtask> expectedSubtasks = Collections.emptyList();
        final int expectedSubtasksCount = 0;
        final List<Subtask> subtasks = taskManager.getSubtasks();
        final int subtasksCount = subtasks.size();

        assertNotNull(subtasks, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void deleteAllTasksWhenTasksListContainsValues() {
        final int firstTaskId = taskManager.getNextTaskId();
        final int secondTaskId = taskManager.getNextTaskId();
        final Task firstTask = new Task(firstTaskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(secondTaskId, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.deleteAllTasks();

        final List<Task> expectedTasks = Collections.emptyList();
        final int expectedTasksCount = 0;
        final List<Task> tasks = taskManager.getTasks();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void deleteAllTasksWhenTasksListIsEmpty() {
        taskManager.deleteAllTasks();

        final List<Task> expectedTasks = Collections.emptyList();
        final int expectedTasksCount = 0;
        final List<Task> tasks = taskManager.getTasks();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void deleteAllEpicsWhenEpicsListContainsValues() {
        final int firstEpicId = taskManager.getNextTaskId();
        final int secondEpicId = taskManager.getNextTaskId();
        final Epic firstEpic = new Epic(firstEpicId, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(secondEpicId, "Купить лежак для кошки.", "Купить лежак для кошки на подоконник.",
                SECOND_EPIC_START_TIME);

        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.deleteAllEpics();

        final List<Epic> expectedEpics = Collections.emptyList();
        final int expectedEpicsCount = 0;
        final List<Epic> epics = taskManager.getEpics();
        final int epicsCount = epics.size();

        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }

    @Test
    void deleteAllEpicsWhenEpicsListIsEmpty() {
        taskManager.deleteAllEpics();

        final List<Epic> expectedEpics = Collections.emptyList();
        final int expectedEpicsCount = 0;
        final List<Epic> epics = taskManager.getEpics();
        final int epicsCount = epics.size();

        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }

    @Test
    void deleteAllSubtasksWhenSubtasksListContainsValues() {
        final int epicId = taskManager.getNextTaskId();
        final int firstSubtaskId = taskManager.getNextTaskId();
        final int secondSubtaskId = taskManager.getNextTaskId();
        final Subtask firstSubtask = new Subtask(firstSubtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        final Subtask secondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, epicId);

        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);
        taskManager.deleteAllSubtasks();

        final List<Subtask> expectedSubtasks = Collections.emptyList();
        final int expectedSubtasksCount = 0;
        final List<Subtask> subtasks = taskManager.getSubtasks();
        final int subtasksCount = subtasks.size();

        assertNotNull(subtasks, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void deleteAllSubtasksWhenSubtasksListIsEmpty() {
        taskManager.deleteAllSubtasks();

        final List<Subtask> expectedSubtasks = Collections.emptyList();
        final int expectedSubtasksCount = 0;
        final List<Subtask> subtasks = taskManager.getSubtasks();
        final int subtasksCount = subtasks.size();

        assertNotNull(subtasks, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void getExistingTaskById() {
        final int taskId = taskManager.getNextTaskId();
        final Task task = new Task(taskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);

        taskManager.addTask(task);
        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(task, "Task is not returned.");
        assertEquals(task, savedTask, "The returned task do not match.");
    }

    @Test
    void getNotExistingTaskById() {
        final int notExistingTaskId = 1;
        final Task savedTask = taskManager.getTaskById(notExistingTaskId);

        assertNull(savedTask, "Task is returned.");
    }

    @Test
    void getExistingEpicById() {
        final int epicId = taskManager.getNextTaskId();
        final Epic epic = new Epic(epicId, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);

        taskManager.addEpic(epic);
        final Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(epic, "Epic is not returned.");
        assertEquals(epic, savedEpic, "The returned epic do not match.");
    }

    @Test
    void getNotExistingEpicById() {
        final int notExistingEpicId = 1;
        final Epic savedEpic = taskManager.getEpicById(notExistingEpicId);

        assertNull(savedEpic, "Epic is returned.");
    }

    @Test
    void getExistingSubtaskById() {
        final int epicId = taskManager.getNextTaskId();
        final int subtaskId = taskManager.getNextTaskId();
        final Subtask subtask = new Subtask(subtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);

        taskManager.addSubtask(subtask);
        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(subtask, "Subtask is not returned.");
        assertEquals(subtask, savedSubtask, "The returned subtask do not match.");
    }

    @Test
    void getNotExistingSubtaskById() {
        final int notExistingSubtaskId = 1;
        final Subtask savedSubtask = taskManager.getSubtaskById(notExistingSubtaskId);

        assertNull(savedSubtask, "Subtask is returned.");
    }

    @Test
    void addTaskAddTaskWhenAddNotExistingTask() {
        final int taskId = taskManager.getNextTaskId();
        final Task task = new Task(taskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);

        final boolean success = taskManager.addTask(task);
        final Task savedTask = taskManager.getTaskById(taskId);
        assertTrue(success, "Success add task return false flag.");
        assertEquals(task, savedTask, "The returned task do not match.");


        final List<Task> tasks = taskManager.getTasks();
        final int expectedTasksCount = 1;
        final int tasksCount = tasks.size();

        assertNotNull(task, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertTrue(tasks.contains(task), "Tasks do not contain added task.");
    }

    @Test
    void addTaskNotAddTaskWhenAddExistingTask() {
        final int taskId = taskManager.getNextTaskId();
        final Task task = new Task(taskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);

        taskManager.addTask(task);
        final boolean success = taskManager.addTask(task);

        assertFalse(success, "Failed add task return true flag.");

        final List<Task> tasks = taskManager.getTasks();
        final int expectedTasksCount = 1;
        final int tasksCount = tasks.size();

        assertNotNull(task, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
    }

    @Test
    void addTaskNotAddTaskWhenAddNullValue() {
        final boolean success = taskManager.addTask(null);
        assertFalse(success, "Failed add task return true flag.");
    }

    @Test
    void addTaskThrowExceptionWhenAddTaskWithOverlappingExistingTasks() {
        final int firstTaskId = taskManager.getNextTaskId();
        final int secondTaskId = taskManager.getNextTaskId();
        final Task firstTask = new Task(firstTaskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(secondTaskId, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION , FIRST_TASK_START_TIME);

        taskManager.addTask(firstTask);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.addTask(secondTask),
                "Expected addTask() to throw IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("Добавляемая задача пересекается по времени с уже созданными."),
                "Text of exception is incorrect");
    }

    @Test
    void addEpicAddEpicWhenAddNotExistingEpic() {
        final int epicId = taskManager.getNextTaskId();
        final int firstSubtaskId = taskManager.getNextTaskId();
        final int secondSubtaskId = taskManager.getNextTaskId();
        final Epic epic = new Epic(epicId, "Начать заниматься спортом.", "Пойти в спортзал.", FIRST_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(firstSubtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        final Subtask secondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, epicId);

        firstSubtask.setStatus(Status.DONE);
        secondSubtask.setStatus(Status.DONE);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final boolean success = taskManager.addEpic(epic);
        final Epic savedEpic = taskManager.getEpicById(epicId);
        final Status expectedStatus = Status.DONE;
        final Status status = savedEpic.getStatus();

        assertTrue(success, "Success add epic return false flag.");
        assertEquals(epic, savedEpic, "The returned epic do not match.");
        assertEquals(expectedStatus, status, "The status of returned epic is incorrect.");

        final List<Epic> epics = taskManager.getEpics();
        final int expectedEpicsCount = 1;
        final int epicsCount = epics.size();

        assertNotNull(epic, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertTrue(epics.contains(epic), "Epics do not contain added epic.");
    }

    @Test
    void addEpicNotAddEpicWhenAddExistingEpic() {
        final int epicId = taskManager.getNextTaskId();
        final Epic epic = new Epic(epicId, "Сходить в магазин.", "Купить продукты.", FIRST_EPIC_START_TIME);

        taskManager.addEpic(epic);
        final boolean success = taskManager.addEpic(epic);
        assertFalse(success, "Failed add epic return true flag.");

        final List<Epic> epics = taskManager.getEpics();
        final int expectedEpicsCount = 1;
        final int epicsCount = epics.size();

        assertNotNull(epic, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
    }

    @Test
    void addEpicNotAddEpicWhenAddNullValue() {
        final boolean success = taskManager.addEpic(null);
        assertFalse(success, "Failed add epic return true flag.");
    }

    @Test
    void addSubtaskAddSubtaskWhenAddNotExistingSubtask() {
        final int epicId = taskManager.getNextTaskId();
        final int subtaskId = taskManager.getNextTaskId();
        final Subtask subtask = new Subtask(subtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);

        final boolean success = taskManager.addSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertTrue(success, "Success add subtask return false flag.");
        assertEquals(subtask, savedSubtask, "The returned subtask do not match.");
    }

    @Test
    void addSubtaskNotAddSubtaskWhenAddExistingSubtask() {
        final int epicId = taskManager.getNextTaskId();
        final int subtaskId = taskManager.getNextTaskId();
        final Subtask subtask = new Subtask(subtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);

        taskManager.addSubtask(subtask);
        final boolean success = taskManager. addSubtask(subtask);
        assertFalse(success, "Failed add subtask return true flag.");

        final List<Subtask> subtasks = taskManager.getSubtasks();
        final int expectedSubtasksCount = 1;
        final int subtasksCount = subtasks.size();

        assertNotNull(subtask, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
    }

    @Test
    void addSubtaskNotAddSubtaskWhenAddNullValue() {
        final boolean success = taskManager.addSubtask(null);
        assertFalse(success, "Failed add subtask return true flag.");
    }

    @Test
    void addSubtaskThrowExceptionWhenAddTaskWithOverlappingExistingTasks() {
        final int epicId = taskManager.getNextTaskId();
        final int firstSubtaskId = taskManager.getNextTaskId();
        final int secondSubtaskId = taskManager.getNextTaskId();
        final Subtask firstSubtask = new Subtask(firstSubtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        final Subtask secondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);

        taskManager.addTask(firstSubtask);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.addTask(secondSubtask),
                "Expected addSubtask() to throw IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("Добавляемая задача пересекается по времени с уже созданными."),
                "Text of exception is incorrect");
    }

    @Test
    void updateTaskUpdateTaskWhenUpdateExistingTask() {
        final int taskId = taskManager.getNextTaskId();
        final Task task = new Task(taskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        taskManager.addTask(task);

        final Task updatedTask = new Task(taskId, "Сходить в магазин (updated).", "Купить продукты(updated).",
                TASK_DURATION, FIRST_SUBTASK_START_TIME);
        taskManager.updateTask(updatedTask);

        final Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Task is not returned.");
        assertNotEquals(task, savedTask, "The returned task is not updated.");
        assertEquals(updatedTask, savedTask, "The returned task is not updated.");
    }

    @Test
    void updateTaskNotUpdateTaskWhenUpdateNotExistingTask() {
        final int taskId = taskManager.getNextTaskId();
        final Task updatedTask = new Task(taskId, "Сходить в магазин (updated).", "Купить продукты(updated).",
                TASK_DURATION, FIRST_TASK_START_TIME);

        final boolean success = taskManager.updateTask(updatedTask);
        assertFalse(success, "Failed update task return true flag.");
    }

    @Test
    void updateTaskNotUpdateTaskWhenUpdateTaskWithNullValue() {
        final boolean success = taskManager.updateTask(null);
        assertFalse(success, "Failed update task return true flag.");
    }

    @Test
    void updateTaskThrowExceptionWhenAddTaskWithOverlappingExistingTasks() {
        final int firstTaskId = taskManager.getNextTaskId();
        final int secondTaskId = taskManager.getNextTaskId();
        final Task firstTask = new Task(firstTaskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(secondTaskId, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION , SECOND_TASK_START_TIME);

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);

        final Task updatedSecondTask = new Task(secondTaskId, "Убраться в квартире.", "Пропылесосить полы (updated).",
                TASK_DURATION, FIRST_TASK_START_TIME);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateTask(updatedSecondTask),
                "Expected addTask() to throw IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("Обновляемая задача пересекается по времени с уже созданными."),
                "Text of exception is incorrect");
    }

    @Test
    void updateEpicUpdateEpicWhenUpdateExistingEpic() {
        final int epicId = taskManager.getNextTaskId();
        final int firstSubtaskId = taskManager.getNextTaskId();
        final int secondSubtaskId = taskManager.getNextTaskId();
        final Epic epic = new Epic(epicId, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(firstSubtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        final Subtask secondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, epicId);
        epic.setSubtasks(List.of(firstSubtask, secondSubtask));
        taskManager.addEpic(epic);

        final Epic updatedEpic = new Epic(epicId, "Начать заниматься спортом (updated).", "Пойти в спортзал(updated).",
                FIRST_EPIC_START_TIME);
        firstSubtask.setStatus(Status.DONE);
        secondSubtask.setStatus(Status.DONE);
        updatedEpic.setSubtasks(List.of(firstSubtask, secondSubtask));
        taskManager.updateEpic(updatedEpic);

        final Epic savedEpic = taskManager.getEpicById(epicId);
        final Status expectedStatus = Status.DONE;
        final Status status = savedEpic.getStatus();

        assertNotNull(savedEpic, "Epic is not returned.");
        assertNotEquals(epic, savedEpic, "The returned epic is not updated.");
        assertEquals(updatedEpic, savedEpic, "The returned epic is not updated.");
        assertEquals(expectedStatus, status, "The status of returned epic is incorrect.");
    }

    @Test
    void updateEpicNotUpdateEpicWhenUpdateNotExistingEpic() {
        final int epicId = taskManager.getNextTaskId();
        final Epic updatedEpic = new Epic(epicId, "Начать заниматься спортом (updated).", "Пойти в спортзал(updated).",
                FIRST_EPIC_START_TIME);

        final boolean success = taskManager.updateEpic(updatedEpic);
        assertFalse(success, "Failed update epic return true flag.");
    }

    @Test
    void updateEpicNotUpdateEpicWhenUpdateEpicWithNullValue() {
        final boolean success = taskManager.updateEpic(null);
        assertFalse(success, "Failed update epic return true flag.");
    }

    @Test
    void updateSubtaskUpdateSubtaskWhenUpdateExistingSubtask() {
        final int epicId = taskManager.getNextTaskId();
        final int subtaskId = taskManager.getNextTaskId();
        final Subtask subtask = new Subtask(subtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        taskManager.addSubtask(subtask);

        final Subtask updatedSubtask = new Subtask(subtaskId, "Выбрать место тренировок (updated).", "Выбрать спортзал (updated).",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        taskManager.updateSubtask(updatedSubtask);

        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(subtask, "Subtask is not returned.");
        assertNotEquals(subtask, savedSubtask, "The returned subtask is not updated.");
        assertEquals(updatedSubtask, savedSubtask, "The returned subtask is not updated.");
    }

    @Test
    void updateSubtaskNotUpdateSubtaskWhenUpdateNotExistingSubtask() {
        final int epicId = taskManager.getNextTaskId();
        final int subtaskId = taskManager.getNextTaskId();
        final Subtask updatedSubtask = new Subtask(subtaskId, "Выбрать место тренировок (updated).", "Выбрать спортзал (updated).",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);

        final boolean success = taskManager.updateSubtask(updatedSubtask);
        assertFalse(success, "Failed update subtask return true flag.");
    }

    @Test
    void updateSubtaskNotUpdateSubtaskWhenUpdateSubtaskWithNullValue() {
        final boolean success = taskManager.updateSubtask(null);
        assertFalse(success, "Failed update subtask return true flag.");
    }

    @Test
    void updateSubtaskThrowExceptionWhenAddTaskWithOverlappingExistingTasks() {
        final int epicId = taskManager.getNextTaskId();
        final int firstSubtaskId = taskManager.getNextTaskId();
        final int secondSubtaskId = taskManager.getNextTaskId();
        final Subtask firstSubtask = new Subtask(firstSubtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        final Subtask secondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, epicId);

        taskManager.addTask(firstSubtask);
        taskManager.addTask(secondSubtask);

        final Subtask updatedSecondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент (updated).",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.updateSubtask(updatedSecondSubtask),
                "Expected addTask() to throw IllegalArgumentException"
        );

        assertTrue(exception.getMessage().contains("Обновляемая задача пересекается по времени с уже созданными."),
                "Text of exception is incorrect");
    }

    @Test
    void deleteExistingTaskById() {
        final int taskId = taskManager.getNextTaskId();
        final Task task = new Task(taskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        taskManager.addTask(task);

        final boolean success = taskManager.deleteTaskById(taskId);
        assertTrue(success, "Success delete task return false flag.");

        final List<Task> tasks = taskManager.getTasks();
        final int expectedTasksCount = 0;
        final int tasksCount = tasks.size();

        assertNotNull(task, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
    }

    @Test
    void deleteNotExistingTaskById() {
        final int taskId = taskManager.getNextTaskId();
        final boolean success = taskManager.deleteTaskById(taskId);
        assertFalse(success, "Failed delete task return true flag.");
    }

    @Test
    void deleteExistingEpicById() {
        final int epicId = taskManager.getNextTaskId();
        final Epic epic = new Epic(epicId, "Сходить в магазин.", "Купить продукты.", FIRST_EPIC_START_TIME);
        taskManager.addEpic(epic);

        final boolean success = taskManager.deleteEpicById(epicId);
        assertTrue(success, "Success delete epic return false flag.");

        final List<Epic> epics = taskManager.getEpics();
        final int expectedEpicsCount = 0;
        final int epicsCount = epics.size();

        assertNotNull(epic, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
    }

    @Test
    void deleteNotExistingEpicById() {
        final int epicId = taskManager.getNextTaskId();
        final boolean success = taskManager.deleteEpicById(epicId);
        assertFalse(success, "Failed delete epic return true flag.");
    }

    @Test
    void deleteExistingSubtaskById() {
        final int epicId = taskManager.getNextTaskId();
        final int subtaskId = taskManager.getNextTaskId();
        final Subtask subtask = new Subtask(subtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        taskManager.addSubtask(subtask);

        final boolean success = taskManager.deleteSubtaskById(subtaskId);
        assertTrue(success, "Success delete subtask return false flag.");

        final List<Subtask> subtasks = taskManager.getSubtasks();
        final int expectedSubtasksCount = 0;
        final int subtasksCount = subtasks.size();

        assertNotNull(subtask, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
    }

    @Test
    void deleteNotExistingSubtaskById() {
        final int subtaskId = taskManager.getNextTaskId();
        final boolean success = taskManager.deleteSubtaskById(subtaskId);
        assertFalse(success, "Failed delete subtask return true flag.");
    }

    @Test
    void getEpicSubtasksByIdWhenEpicSubtasksListContainsValues() {
        final int epicId = taskManager.getNextTaskId();
        final int firstSubtaskId = taskManager.getNextTaskId();
        final int secondSubtaskId = taskManager.getNextTaskId();
        final Epic epic = new Epic(epicId, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(firstSubtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        final Subtask secondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, epicId);
        final List<Subtask> expectedSubtasks = List.of(firstSubtask, secondSubtask);

        epic.setSubtasks(expectedSubtasks);
        taskManager.addEpic(epic);
        taskManager.addSubtask(firstSubtask);
        taskManager.addSubtask(secondSubtask);

        final List<Subtask> savedSubtasks = taskManager.getEpicSubtasksById(epicId);
        final int expectedSubtasksCount = 2;
        final int subtasksCount = savedSubtasks.size();

        assertNotNull(savedSubtasks, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, savedSubtasks, "The returned subtasks do not match.");
    }

    @Test
    void getEpicSubtasksByIdWhenEpicSubtasksListIsEmpty() {
        final int epicId = taskManager.getNextTaskId();
        final Epic epic = new Epic(epicId, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        taskManager.addEpic(epic);

        final List<Subtask> expectedSubtasks = Collections.emptyList();
        final List<Subtask> savedSubtasks = taskManager.getEpicSubtasksById(epicId);
        final int expectedSubtasksCount = 0;
        final int subtasksCount = savedSubtasks.size();

        assertNotNull(savedSubtasks, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, savedSubtasks, "The returned subtasks do not match.");
    }

    @Test
    void getEpicSubtasksByIdWhenEpicNotExists() {
        final int epicId = taskManager.getNextTaskId();
        final List<Subtask> expectedSubtasks = Collections.emptyList();
        final List<Subtask> savedSubtasks = taskManager.getEpicSubtasksById(epicId);
        final int expectedSubtasksCount = 0;
        final int subtasksCount = savedSubtasks.size();

        assertNotNull(savedSubtasks, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, savedSubtasks, "The returned subtasks do not match.");
    }

    @Test
    void getPrioritizedTasksWhenTasksListAndSubtasksListAreEmpty() {
        final List<Task> expectedPrioritizedTasks = Collections.emptyList();
        final int expectedPrioritizedTasksCount = 0;
        final List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        final int prioritizedTasksCount = prioritizedTasks.size();

        assertNotNull(prioritizedTasks, "Prioritized tasks are not returned.");
        assertEquals(expectedPrioritizedTasksCount, prioritizedTasksCount, "The count of prioritized tasks does not match.");
        assertIterableEquals(expectedPrioritizedTasks, prioritizedTasks , "The returned prioritized tasks do not match.");
    }

    @Test
    void getPrioritizedTasksWhenTasksListAndSubtasksListAreContainValues() {
        final int firstTaskId = taskManager.getNextTaskId();
        final int secondTaskId = taskManager.getNextTaskId();
        final int epicId = taskManager.getNextTaskId();
        final int firstSubtaskId = taskManager.getNextTaskId();
        final int secondSubtaskId = taskManager.getNextTaskId();
        final Task firstTask = new Task(firstTaskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(secondTaskId, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION ,SECOND_TASK_START_TIME);
        final Subtask firstSubtask = new Subtask(firstSubtaskId, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, epicId);
        final Subtask secondSubtask = new Subtask(secondSubtaskId, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, epicId);

        taskManager.addSubtask(secondSubtask);
        taskManager.addSubtask(firstSubtask);
        taskManager.addTask(secondTask);
        taskManager.addTask(firstTask);

        final List<Task> expectedPrioritizedTasks = List.of(firstTask, secondTask, firstSubtask, secondSubtask);
        final int expectedPrioritizedTasksCount = expectedPrioritizedTasks.size();
        final List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        final int prioritizedTasksCount = prioritizedTasks.size();

        assertNotNull(prioritizedTasks, "Prioritized tasks are not returned.");
        assertEquals(expectedPrioritizedTasksCount, prioritizedTasksCount, "The count of prioritized tasks does not match.");
        assertIterableEquals(expectedPrioritizedTasks, prioritizedTasks , "The returned prioritized tasks do not match.");
    }
}
