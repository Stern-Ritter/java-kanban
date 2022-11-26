package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class FileBackendTaskManagerTest extends TaskManagerTest<FileBackendTaskManager> {
    private static final String FILE_PATH = "data/TestTasksData.csv";
    private static final int FIRST_TASK_ID = 1;
    private static final int SECOND_TASK_ID = 2;
    private static final int FIRST_EPIC_ID = 3;
    private static final int SECOND_EPIC_ID = 4;
    private static final int FIRST_SUBTASK_ID = 5;
    private static final int SECOND_SUBTASK_ID = 6;
    private static final int THIRD_SUBTASK_ID = 7;

    private static final String FIRST_TASK_START_TIME_STRING = "08.11.2022 11:00";
    private static final String SECOND_TASK_START_TIME_STRING = "08.11.2022 12:01";
    private static final String FIRST_EPIC_START_TIME_STRING = "08.11.2022 13:20";
    private static final String SECOND_EPIC_START_TIME_STRING = "08.11.2022 15:00";
    private static final String FIRST_SUBTASK_START_TIME_STRING = "08.11.2022 13:20";
    private static final String SECOND_SUBTASK_START_TIME_STRING = "08.11.2022 13:50";
    private static final String THIRD_SUBTASK_START_TIME_STRING = "08.11.2022 15:00";

    private static final LocalDateTime FIRST_TASK_START_TIME = LocalDateTime.parse(FIRST_TASK_START_TIME_STRING,
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime SECOND_TASK_START_TIME = LocalDateTime.parse(SECOND_TASK_START_TIME_STRING,
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime FIRST_EPIC_START_TIME = LocalDateTime.parse(FIRST_EPIC_START_TIME_STRING,
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime SECOND_EPIC_START_TIME = LocalDateTime.parse(SECOND_EPIC_START_TIME_STRING,
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime FIRST_SUBTASK_START_TIME = LocalDateTime.parse(FIRST_SUBTASK_START_TIME_STRING,
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime SECOND_SUBTASK_START_TIME = LocalDateTime.parse(SECOND_SUBTASK_START_TIME_STRING,
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    private static final LocalDateTime THIRD_SUBTASK_START_TIME = LocalDateTime.parse(THIRD_SUBTASK_START_TIME_STRING,
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

    private File file;
    private Task firstTask;
    private Task secondTask;
    private Epic firstEpic;
    private Epic secondEpic;
    private Subtask firstEpicFirstSubtask;
    private Subtask firstEpicSecondSubtask;
    private Subtask secondEpicFirstSubtask;

    @Override
    public FileBackendTaskManager getTaskManager() {
        file = new File(FILE_PATH);
        try {
            if (file.delete()) {
                file.createNewFile();
            }
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Ошибка чтения файла: '%s'.\n", FILE_PATH), ex);
        }

        return FileBackendTaskManager.loadFromFile(file);
    }

    protected TaskManager createTaskManagerInstance() {
        return FileBackendTaskManager.loadFromFile(file);
    }

    protected String getState() {
        try {
            String filePath = file.getAbsolutePath();
            return Files.readString(Path.of(filePath));
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Ошибка чтения файла: '%s'.\n", FILE_PATH), ex);
        }
    }

    @BeforeEach
    void setUp() {
        firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.", 60, FIRST_TASK_START_TIME);
        secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.", 70, SECOND_TASK_START_TIME);
        firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.", FIRST_EPIC_START_TIME);
        secondEpic = new Epic(SECOND_EPIC_ID, "Купить лежак для кошки.", "Купить лежак для кошки на подоконник.", SECOND_EPIC_START_TIME);
        firstEpicFirstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.", 10, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpicSecondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.", 15, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        secondEpicFirstSubtask = new Subtask(THIRD_SUBTASK_ID, "Заказать лежак в интернет магазине.", "Выбрать и заказать лежак в интернет магазине.", 40, THIRD_SUBTASK_START_TIME, SECOND_EPIC_ID);
    }

    @Test
    void saveStateWithHistoryLine() {
        firstEpic.setSubtasks(List.of(firstEpicFirstSubtask, firstEpicSecondSubtask));
        secondEpic.setSubtasks(List.of(secondEpicFirstSubtask));

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstEpicFirstSubtask);
        taskManager.addSubtask(firstEpicSecondSubtask);
        taskManager.addSubtask(secondEpicFirstSubtask);

        taskManager.getTaskById(FIRST_TASK_ID);
        taskManager.getEpicById(FIRST_EPIC_ID);
        taskManager.getSubtaskById(THIRD_SUBTASK_ID);
        taskManager.getTaskById(SECOND_TASK_ID);
        taskManager.getEpicById(FIRST_EPIC_ID);
        taskManager.getEpicById(SECOND_EPIC_ID);

        final String expectedData = String.format("1,TASK,Сходить в магазин.,NEW,Купить продукты.,60,%s,\r\n", FIRST_TASK_START_TIME_STRING) +
                String.format("2,TASK,Убраться в квартире.,NEW,Пропылесосить полы.,70,%s,\r\n", SECOND_TASK_START_TIME_STRING) +
                String.format("3,EPIC,Начать заниматься спортом.,NEW,Пойти в спортзал.,25,%s,\r\n", FIRST_EPIC_START_TIME_STRING) +
                String.format("4,EPIC,Купить лежак для кошки.,NEW,Купить лежак для кошки на подоконник.,40,%s,\r\n", SECOND_EPIC_START_TIME_STRING) +
                String.format("5,SUBTASK,Выбрать место тренировок.,NEW,Выбрать спортзал.,10,%s,3\r\n", FIRST_SUBTASK_START_TIME_STRING) +
                String.format("6,SUBTASK,Записаться в зал.,NEW,Оплатить абонемент.,15,%s,3\r\n", SECOND_SUBTASK_START_TIME_STRING) +
                String.format("7,SUBTASK,Заказать лежак в интернет магазине.,NEW,Выбрать и заказать лежак в интернет магазине.,40,%s,4\r\n", THIRD_SUBTASK_START_TIME_STRING) +
                "\r\n" +
                "1,7,2,3,4";
        String data = getState();
        assertEquals(expectedData, data, "Saved in file state does not match");

        final TaskManager newTaskManagerInstance = createTaskManagerInstance();

        final List<Task> expectedTasks = List.of(firstTask, secondTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = newTaskManagerInstance.getTasks();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");

        final List<Epic> expectedEpics = List.of(firstEpic, secondEpic);
        final int expectedEpicsCount = expectedEpics.size();
        final List<Epic> epics = newTaskManagerInstance.getEpics();
        final int epicsCount = epics.size();

        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");

        final List<Subtask> expectedSubtasks = List.of(firstEpicFirstSubtask, firstEpicSecondSubtask, secondEpicFirstSubtask);
        final int expectedSubtasksCount = expectedSubtasks.size();
        final List<Subtask> subtasks = newTaskManagerInstance.getSubtasks();
        final int subtasksCount = subtasks.size();

        assertNotNull(subtasks, "Subtask are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");

        final List<Task> expectedHistoryTasks = List.of(firstTask, secondEpicFirstSubtask, secondTask, firstEpic, secondEpic);
        final int expectedHistoryTasksCount = expectedHistoryTasks.size();
        final List<Task> historyTasks = newTaskManagerInstance.getHistory();
        final int historyTasksCount = historyTasks.size();

        assertNotNull(historyTasks, "History tasks are not returned.");
        assertEquals(expectedHistoryTasksCount, historyTasksCount, "The count of history tasks does not match.");
        assertIterableEquals(expectedHistoryTasks, historyTasks, "The returned history tasks do not match.");
    }

    @Test
    void saveStateWithEmptyTasks() {
        firstEpic.setSubtasks(List.of(firstEpicFirstSubtask, firstEpicSecondSubtask));
        secondEpic.setSubtasks(List.of(secondEpicFirstSubtask));

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstEpicFirstSubtask);
        taskManager.addSubtask(firstEpicSecondSubtask);
        taskManager.addSubtask(secondEpicFirstSubtask);

        taskManager.getTaskById(FIRST_TASK_ID);
        taskManager.getEpicById(FIRST_EPIC_ID);
        taskManager.getSubtaskById(THIRD_SUBTASK_ID);
        taskManager.getTaskById(SECOND_TASK_ID);
        taskManager.getEpicById(FIRST_EPIC_ID);
        taskManager.getEpicById(SECOND_EPIC_ID);

        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubtasks();

        final String expectedData = "\r\n";
        String data = getState();
        assertEquals(expectedData, data, "Saved in file state does not match");

        final TaskManager newTaskManagerInstance = createTaskManagerInstance();

        final List<Task> expectedTasks = Collections.emptyList();
        final int expectedTasksCount = 0;
        final List<Task> tasks = newTaskManagerInstance.getTasks();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");

        final List<Epic> expectedEpics = Collections.emptyList();
        final int expectedEpicsCount = 0;
        final List<Epic> epics = newTaskManagerInstance.getEpics();
        final int epicsCount = epics.size();

        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");

        final List<Subtask> expectedSubtasks = Collections.emptyList();
        final int expectedSubtasksCount = 0;
        final List<Subtask> subtasks = newTaskManagerInstance.getSubtasks();
        final int subtasksCount = subtasks.size();

        assertNotNull(subtasks, "Subtask are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");

        final List<Task> expectedHistoryTasks = Collections.emptyList();
        final int expectedHistoryTasksCount = 0;
        final List<Task> historyTasks = newTaskManagerInstance.getHistory();
        final int historyTasksCount = historyTasks.size();

        assertNotNull(historyTasks, "History task are not returned.");
        assertEquals(expectedHistoryTasksCount, historyTasksCount, "The count of history tasks does not match.");
        assertIterableEquals(expectedHistoryTasks, historyTasks, "The returned history tasks do not match.");
    }

    @Test
    void saveStateWithEmptyHistoryLine() {
        firstEpic.setSubtasks(List.of(firstEpicFirstSubtask, firstEpicSecondSubtask));
        secondEpic.setSubtasks(List.of(secondEpicFirstSubtask));

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(firstEpicFirstSubtask);
        taskManager.addSubtask(firstEpicSecondSubtask);
        taskManager.addSubtask(secondEpicFirstSubtask);

        final String expectedData = String.format("1,TASK,Сходить в магазин.,NEW,Купить продукты.,60,%s,\r\n", FIRST_TASK_START_TIME_STRING) +
                String.format("2,TASK,Убраться в квартире.,NEW,Пропылесосить полы.,70,%s,\r\n", SECOND_TASK_START_TIME_STRING) +
                String.format("3,EPIC,Начать заниматься спортом.,NEW,Пойти в спортзал.,25,%s,\r\n", FIRST_EPIC_START_TIME_STRING) +
                String.format("4,EPIC,Купить лежак для кошки.,NEW,Купить лежак для кошки на подоконник.,40,%s,\r\n", SECOND_EPIC_START_TIME_STRING) +
                String.format("5,SUBTASK,Выбрать место тренировок.,NEW,Выбрать спортзал.,10,%s,3\r\n", FIRST_SUBTASK_START_TIME_STRING) +
                String.format("6,SUBTASK,Записаться в зал.,NEW,Оплатить абонемент.,15,%s,3\r\n", SECOND_SUBTASK_START_TIME_STRING) +
                String.format("7,SUBTASK,Заказать лежак в интернет магазине.,NEW,Выбрать и заказать лежак в интернет магазине.,40,%s,4\r\n", THIRD_SUBTASK_START_TIME_STRING) +
                "\r\n";
        String data = getState();
        assertEquals(expectedData, data, "Saved in file state does not match");

        final TaskManager newTaskManagerInstance = createTaskManagerInstance();

        final List<Task> expectedTasks = List.of(firstTask, secondTask);
        final int expectedTasksCount = expectedTasks.size();
        final List<Task> tasks = newTaskManagerInstance.getTasks();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");

        final List<Epic> expectedEpics = List.of(firstEpic, secondEpic);
        final int expectedEpicsCount = expectedEpics.size();
        final List<Epic> epics = newTaskManagerInstance.getEpics();
        final int epicsCount = epics.size();

        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");

        final List<Subtask> expectedSubtasks = List.of(firstEpicFirstSubtask, firstEpicSecondSubtask, secondEpicFirstSubtask);
        final int expectedSubtasksCount = expectedSubtasks.size();
        final List<Subtask> subtasks = newTaskManagerInstance.getSubtasks();
        final int subtasksCount = subtasks.size();

        assertNotNull(subtasks, "Subtask are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");

        final List<Task> expectedHistoryTasks = Collections.emptyList();
        final int expectedHistoryTasksCount = 0;
        final List<Task> historyTasks = newTaskManagerInstance.getHistory();
        final int historyTasksCount = historyTasks.size();

        assertNotNull(historyTasks, "History task are not returned.");
        assertEquals(expectedHistoryTasksCount, historyTasksCount, "The count of history tasks does not match.");
        assertIterableEquals(expectedHistoryTasks, historyTasks, "The returned history tasks do not match.");
    }

    @Test
    void saveStateWithEpicsWithoutSubtasks() {
        taskManager.addEpic(firstEpic);
        taskManager.addEpic(secondEpic);

        final String expectedData = String.format("3,EPIC,Начать заниматься спортом.,NEW,Пойти в спортзал.,0,%s,\r\n", FIRST_EPIC_START_TIME_STRING) +
                String.format("4,EPIC,Купить лежак для кошки.,NEW,Купить лежак для кошки на подоконник.,0,%s,\r\n", SECOND_EPIC_START_TIME_STRING) +
                "\r\n";
        String data = getState();
        assertEquals(expectedData, data, "Saved in file state does not match");

        final TaskManager newTaskManagerInstance = createTaskManagerInstance();

        final List<Task> expectedTasks = Collections.emptyList();
        final int expectedTasksCount = 0;
        final List<Task> tasks = newTaskManagerInstance.getTasks();
        final int tasksCount = tasks.size();

        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");

        final List<Epic> expectedEpics = List.of(firstEpic, secondEpic);
        final int expectedEpicsCount = expectedEpics.size();
        final List<Epic> epics = newTaskManagerInstance.getEpics();
        final int epicsCount = epics.size();

        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");

        final List<Subtask> expectedSubtasks = Collections.emptyList();
        final int expectedSubtasksCount = 0;
        final List<Subtask> subtasks = newTaskManagerInstance.getSubtasks();
        final int subtasksCount = subtasks.size();

        assertNotNull(subtasks, "Subtask are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");

        final List<Task> expectedHistoryTasks = Collections.emptyList();
        final int expectedHistoryTasksCount = 0;
        final List<Task> historyTasks = newTaskManagerInstance.getHistory();
        final int historyTasksCount = historyTasks.size();

        assertNotNull(historyTasks, "History task are not returned.");
        assertEquals(expectedHistoryTasksCount, historyTasksCount, "The count of history tasks does not match.");
        assertIterableEquals(expectedHistoryTasks, historyTasks, "The returned history tasks do not match.");
    }
}
