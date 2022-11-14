package service;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {
    private final int TASK_DURATION = 60;
    private final LocalDateTime TASK_START_DATE = LocalDateTime.now();

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void addToHistoryNoDuplicateTasks() {
        final Task firstTask = new Task(1, "Сходить в магазин.", "Купить продукты.", TASK_DURATION, TASK_START_DATE);
        final Task secondTask = new Task(2, "Убраться в квартире.", "Пропылесосить полы.", TASK_DURATION, TASK_START_DATE);

        historyManager.add(firstTask);
        historyManager.add(secondTask);

        final List<Task> expectedTasks = List.of(firstTask, secondTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = historyManager.getHistory();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void addToHistoryDuplicateTasks() {
        final Task firstTask = new Task(1, "Сходить в магазин.", "Купить продукты.", TASK_DURATION, TASK_START_DATE);
        final Task secondTask = new Task(2, "Убраться в квартире.", "Пропылесосить полы.", TASK_DURATION, TASK_START_DATE);
        final Task thirdTask = new Task(3, "Вакцинировать кошку.", "Отвезти кошку в ветеринарную клинику.", TASK_DURATION, TASK_START_DATE);

        historyManager.add(firstTask);
        historyManager.add(secondTask);
        historyManager.add(thirdTask);
        historyManager.add(secondTask);
        historyManager.add(firstTask);
        historyManager.add(firstTask);
        historyManager.add(firstTask);


        final List<Task> expectedTasks = List.of(thirdTask, secondTask, firstTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = historyManager.getHistory();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void addToHistoryNullValue() {
        historyManager.add(null);

        final List<Task> expectedTasks = Collections.emptyList();
        final int expectedTasksCount = 0;
        final List<Task> tasks = historyManager.getHistory();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void removeTaskOnHeadPositionFromHistory() {
        final int firstTaskId = 1;
        final Task firstTask = new Task(firstTaskId, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, TASK_START_DATE);
        final Task secondTask = new Task(2, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, TASK_START_DATE);
        final Task thirdTask = new Task(3, "Вакцинировать кошку.", "Отвезти кошку в ветеринарную клинику.",
                TASK_DURATION, TASK_START_DATE);

        historyManager.add(firstTask);
        historyManager.add(secondTask);
        historyManager.add(thirdTask);
        historyManager.remove(firstTaskId);

        final List<Task> expectedTasks = List.of(secondTask, thirdTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = historyManager.getHistory();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void removeTaskOnMiddlePositionFromHistory() {
        final int secondTaskId = 2;
        final Task firstTask = new Task(1, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, TASK_START_DATE);
        final Task secondTask = new Task(secondTaskId, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, TASK_START_DATE);
        final Task thirdTask = new Task(3, "Вакцинировать кошку.", "Отвезти кошку в ветеринарную клинику.",
                TASK_DURATION, TASK_START_DATE);

        historyManager.add(firstTask);
        historyManager.add(secondTask);
        historyManager.add(thirdTask);
        historyManager.remove(secondTaskId);

        final List<Task> expectedTasks = List.of(firstTask, thirdTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = historyManager.getHistory();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void removeTaskOnTailPositionFromHistory() {
        final int thirdTaskId = 3;
        final Task firstTask = new Task(1, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, TASK_START_DATE);
        final Task secondTask = new Task(2, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, TASK_START_DATE);
        final Task thirdTask = new Task(thirdTaskId, "Вакцинировать кошку.", "Отвезти кошку в ветеринарную клинику.",
                TASK_DURATION, TASK_START_DATE);

        historyManager.add(firstTask);
        historyManager.add(secondTask);
        historyManager.add(thirdTask);
        historyManager.remove(thirdTaskId);

        final List<Task> expectedTasks = List.of(firstTask, secondTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = historyManager.getHistory();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void removeTaskWithIncorrectIdFromHistory() {
        final Task firstTask = new Task(1, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, TASK_START_DATE);
        final Task secondTask = new Task(2, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, TASK_START_DATE);
        final Task thirdTask = new Task(3, "Вакцинировать кошку.", "Отвезти кошку в ветеринарную клинику.",
                TASK_DURATION, TASK_START_DATE);
        final int notExistingId = 4;

        historyManager.add(firstTask);
        historyManager.add(secondTask);
        historyManager.add(thirdTask);
        historyManager.remove(notExistingId);

        final List<Task> expectedTasks = List.of(firstTask, secondTask, thirdTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = historyManager.getHistory();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void getEmptyHistory() {
        final List<Task> expectedTasks = Collections.emptyList();
        final int expectedTasksCount = 0;
        final List<Task> tasks = historyManager.getHistory();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }
}