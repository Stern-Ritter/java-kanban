package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exceptions.HttpClientException;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;
import utils.Status;
import utils.serializer.EpicSerializer;
import utils.serializer.LocalDateTimeAdapter;
import utils.serializer.SubtaskSerializer;
import utils.serializer.TaskSerializer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private static final String URL = "http://localhost:8080";
    private static final String ROOT_PATH = "tasks";
    private static final String TASK_PATH = "task";
    private static final String SUBTASK_PATH = "subtask";
    private static final String EPIC_PATH = "epic";
    private static final String HISTORY_PATH = "history";
    private static final String NOT_EXISTING_ID = "999";
    private static final String INCORRECT_ID = "f?!a?";
    private static final String EMPTY_BODY = "";

    private static final int FIRST_TASK_ID = 1;
    private static final int SECOND_TASK_ID = 2;
    private static final int FIRST_EPIC_ID = 3;
    private static final int SECOND_EPIC_ID = 4;
    private static final int FIRST_SUBTASK_ID = 5;
    private static final int SECOND_SUBTASK_ID = 6;
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

    private HttpTaskServer taskServer;
    private KVServer kvServer;
    private TaskManager taskManager;
    private HttpClient client;
    private Gson gson;

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        URI uri = URI.create(String.format(path));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws IOException, InterruptedException {
        URI uri = URI.create(String.format(path));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        URI uri = URI.create(String.format(path));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    @BeforeEach
    void setUp() {
        try {
            kvServer = new KVServer();
            kvServer.start();
        } catch (IOException | HttpClientException ex) {
            System.out.println("Не удалось запустить KVServer.");
        }

        try {
            taskServer = new HttpTaskServer();
            taskServer.start();
        } catch (IOException ex) {
            System.out.println("Не удалось запустить HttpTaskServer.");
        }

        HistoryManager historyManager = Managers.getDefaultHistory();
        taskManager = new InMemoryTaskManager(historyManager);

        client = HttpClient.newHttpClient();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskSerializer(taskManager));
        gsonBuilder.registerTypeAdapter(Subtask.class, new SubtaskSerializer(taskManager));
        gsonBuilder.registerTypeAdapter(Epic.class, new EpicSerializer(taskManager));
        gson = gsonBuilder.create();
    }

    @AfterEach
    void shutDown() {
        kvServer.stop();
        taskServer.stop();
    }

    @Test
    void getAllTasks() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        final String body = response.body();
        final List<Task> tasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Task[].class)));
        final int tasksCount = tasks.size();

        final List<Task> expectedTasks = List.of(firstTask, secondTask);
        final int expectedTasksCount = expectedTasks.size();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void getExistingTaskById() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + SECOND_TASK_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        final String body = response.body();
        final Task savedTask = gson.fromJson(body, Task.class);

        assertEquals(expectedStatusCode, statusCode, "The response status code  does not match.");
        assertNotNull(savedTask, "Task is not returned.");
        assertEquals(secondTask, savedTask, "The returned task does not match.");
    }

    @Test
    void getNotExistingTaskById() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + NOT_EXISTING_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 404;

        final String body = response.body();

        assertEquals(expectedStatusCode, statusCode, "The response status code  does not match.");
        assertEquals(EMPTY_BODY, body, "The returned response body is not empty.");
    }

    @Test
    void getTaskByIncorrectId() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + INCORRECT_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 400;

        final String body = response.body();

        assertEquals(expectedStatusCode, statusCode, "The response status code  does not match.");
        assertEquals(EMPTY_BODY, body, "The returned response body is not empty.");
    }

    @Test
    void deleteExistingTaskById() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + SECOND_TASK_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 204;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code  does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH);
        final String body = getResponse.body();
        final List<Task> tasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Task[].class)));
        final int tasksCount = tasks.size();
        final List<Task> expectedTasks = List.of(firstTask);
        final int expectedTasksCount = expectedTasks.size();

        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void deleteNotExistingTaskById() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + NOT_EXISTING_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 204;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH);
        final String body = getResponse.body();
        final List<Task> tasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Task[].class)));
        final int tasksCount = tasks.size();
        final List<Task> expectedTasks = List.of(firstTask, secondTask);
        final int expectedTasksCount = expectedTasks.size();

        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void deleteTaskByIncorrectId() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + INCORRECT_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 400;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH);
        final String body = getResponse.body();
        final List<Task> tasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Task[].class)));
        final int tasksCount = tasks.size();
        final List<Task> expectedTasks = List.of(firstTask, secondTask);
        final int expectedTasksCount = expectedTasks.size();

        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void deleteAllTasks() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + TASK_PATH);
        final int statusCode = deleteResponse.statusCode();
        final int expectedStatusCode = 204;

        assertEquals(expectedStatusCode, statusCode, "The response status code  does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH);
        final String body = getResponse.body();
        final List<Task> tasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Task[].class)));
        final int tasksCount = tasks.size();
        final List<Task> expectedTasks = Collections.emptyList();
        final int expectedTasksCount = 0;

        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void postTask() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        final HttpResponse<String> firstTaskPostResponse = post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        final HttpResponse<String> secondTaskPostResponse = post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final int firstTaskPostStatusCode = firstTaskPostResponse.statusCode();
        final int secondTaskPostStatusCode = secondTaskPostResponse.statusCode();
        final int expectedStatusCode = 201;

        assertEquals(expectedStatusCode, firstTaskPostStatusCode, "The response status code  does not match.");
        assertEquals(expectedStatusCode, secondTaskPostStatusCode, "The response status code  does not match.");

        final HttpResponse<String> firstTaskGetResponse = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + FIRST_TASK_ID);
        final HttpResponse<String> secondTaskGetResponse = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + SECOND_TASK_ID);
        final String firstTaskGetResponseBody = firstTaskGetResponse.body();
        final String secondTaskGetResponseBody = secondTaskGetResponse.body();

        final Task firstSavedTask = gson.fromJson(firstTaskGetResponseBody, Task.class);
        final Task secondSavedTask = gson.fromJson(secondTaskGetResponseBody, Task.class);

        assertNotNull(firstSavedTask, "Task is not returned.");
        assertEquals(firstTask, firstSavedTask, "The returned task does not match.");
        assertNotNull(secondSavedTask, "Task is not returned.");
        assertEquals(secondTask, secondSavedTask, "The returned task does not match.");
    }

    @Test
    void putTask() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        secondTask.setStatus(Status.DONE);
        secondTask.setDescription("Пропылесосить и помыть полы.");
        final String updatedSecondTaskJson = gson.toJson(secondTask);

        final HttpResponse<String> putTaskResponse = post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, updatedSecondTaskJson);
        final int statusCode = putTaskResponse.statusCode();
        final int expectedStatusCode = 201;

        assertEquals(expectedStatusCode, statusCode, "The response status code  does not match.");

        final HttpResponse<String> getTaskResponse = get(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + SECOND_TASK_ID);
        final String body = getTaskResponse.body();
        final Task updatedTask = gson.fromJson(body, Task.class);

        assertNotNull(updatedTask, "Task is not returned.");
        assertEquals(secondTask, updatedTask, "The returned task does not match.");
    }

    @Test
    void getAllSubtask() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        final String body = response.body();
        final List<Subtask> subtasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Subtask[].class)));
        final int subtasksCount = subtasks.size();

        final List<Subtask> expectedSubtasks = List.of(firstSubtask, secondSubtask);
        final int expectedSubtasksCount = expectedSubtasks.size();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertNotNull(subtasks, "Subtasks are not returned.");
        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }


    @Test
    void getExistingSubtaskById() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + SECOND_SUBTASK_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        final String body = response.body();
        final Subtask savedSubtask = gson.fromJson(body, Subtask.class);

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertNotNull(savedSubtask, "Subtask is not returned.");
        assertEquals(secondSubtask, savedSubtask, "The returned subtask does not match.");
    }

    @Test
    void getNotExistingSubtaskById() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + NOT_EXISTING_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 404;

        final String body = response.body();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertEquals(EMPTY_BODY, body, "The returned response body is not empty.");
    }

    @Test
    void getSubtaskByIncorrectId() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + INCORRECT_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 400;

        final String body = response.body();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertEquals(EMPTY_BODY, body, "The returned response body is not empty.");
    }

    @Test
    void deleteExistingSubtaskById() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + SECOND_SUBTASK_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 204;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH);
        final String body = getResponse.body();
        final List<Subtask> subtasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Subtask[].class)));
        final int subtasksCount = subtasks.size();
        final List<Subtask> expectedSubtasks = List.of(firstSubtask);
        final int expectedSubtasksCount = expectedSubtasks.size();

        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void deleteNotExistingSubtaskById() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + NOT_EXISTING_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 204;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH);
        final String body = getResponse.body();
        final List<Subtask> subtasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Subtask[].class)));
        final int subtasksCount = subtasks.size();
        final List<Task> expectedSubtasks = List.of(firstSubtask, secondSubtask);
        final int expectedSubtasksCount = expectedSubtasks.size();

        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void deleteSubtaskByIncorrectId() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + INCORRECT_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 400;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH);
        final String body = getResponse.body();
        final List<Subtask> subtasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Subtask[].class)));
        final int subtasksCount = subtasks.size();
        final List<Task> expectedSubtasks = List.of(firstSubtask, secondSubtask);
        final int expectedSubtasksCount = expectedSubtasks.size();

        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void deleteAllSubtasks() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH);
        final int statusCode = deleteResponse.statusCode();
        final int expectedStatusCode = 204;

        assertEquals(expectedStatusCode, statusCode, "The response status code  does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH);
        final String body = getResponse.body();
        final List<Subtask> subtasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Subtask[].class)));
        final int subtasksCount = subtasks.size();
        final List<Subtask> expectedSubtasks = Collections.emptyList();
        final int expectedSubtasksCount = 0;

        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void postSubtask() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        final HttpResponse<String> firstSubtaskPostResponse = post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        final HttpResponse<String> secondSubtaskPostResponse = post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final int firstSubtaskPostStatusCode = firstSubtaskPostResponse.statusCode();
        final int secondSubtaskPostStatusCode = secondSubtaskPostResponse.statusCode();
        final int expectedStatusCode = 201;

        assertEquals(expectedStatusCode, firstSubtaskPostStatusCode, "The response status code  does not match.");
        assertEquals(expectedStatusCode, secondSubtaskPostStatusCode, "The response status code  does not match.");

        final HttpResponse<String> firstSubtaskGetResponse = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + FIRST_SUBTASK_ID);
        final HttpResponse<String> secondSubtaskGetResponse = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + SECOND_SUBTASK_ID);
        final String firstSubtaskGetResponseBody = firstSubtaskGetResponse.body();
        final String secondSubtaskGetResponseBody = secondSubtaskGetResponse.body();

        final Subtask firstSavedSubtask = gson.fromJson(firstSubtaskGetResponseBody, Subtask.class);
        final Subtask secondSavedSubtask = gson.fromJson(secondSubtaskGetResponseBody, Subtask.class);

        assertNotNull(firstSavedSubtask, "Subtask is not returned.");
        assertEquals(firstSubtask, firstSavedSubtask, "The returned subtask does not match.");
        assertNotNull(secondSavedSubtask, "Subtask is not returned.");
        assertEquals(secondSubtask, secondSavedSubtask, "The returned subtask does not match.");
    }

    @Test
    void putSubtask() throws IOException, InterruptedException {
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        secondSubtask.setStatus(Status.DONE);
        secondSubtask.setDescription("Оплатить абонемент со скидкой.");
        final String updatedSecondSubtaskJson = gson.toJson(secondSubtask);

        final HttpResponse<String> putTaskResponse = post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, updatedSecondSubtaskJson);
        final int statusCode = putTaskResponse.statusCode();
        final int expectedStatusCode = 201;

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");

        final HttpResponse<String> getSubtaskResponse = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + SECOND_SUBTASK_ID);
        final String body = getSubtaskResponse.body();
        final Subtask updatedSubtask = gson.fromJson(body, Subtask.class);

        assertNotNull(updatedSubtask, "Subtask is not returned.");
        assertEquals(secondSubtask, updatedSubtask, "The returned subtask does not match.");
    }

    @Test
    void getAllEpics() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        final String body = response.body();
        final List<Epic> epics = new ArrayList<>(Arrays.asList(gson.fromJson(body, Epic[].class)));
        final int epicsCount = epics.size();

        final List<Epic> expectedEpics = List.of(firstEpic, secondEpic);
        final int expectedEpicsCount = expectedEpics.size();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertNotNull(epics, "Epics are not returned.");
        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }


    @Test
    void getExistingEpicById() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + SECOND_EPIC_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        final String body = response.body();
        final Epic savedEpic = gson.fromJson(body, Epic.class);

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertNotNull(savedEpic, "Epic is not returned.");
        assertEquals(secondEpic, savedEpic, "The returned epic does not match.");
    }

    @Test
    void getNotExistingEpicById() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + NOT_EXISTING_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 404;

        final String body = response.body();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertEquals(EMPTY_BODY, body, "The returned response body is not empty.");
    }

    @Test
    void getEpicByIncorrectId() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + INCORRECT_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 400;

        final String body = response.body();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertEquals(EMPTY_BODY, body, "The returned response body is not empty.");
    }

    @Test
    void deleteExistingEpicById() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + SECOND_EPIC_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 204;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH);
        final String body = getResponse.body();
        final List<Epic> epics = new ArrayList<>(Arrays.asList(gson.fromJson(body, Epic[].class)));
        final int epicsCount = epics.size();
        final List<Epic> expectedEpics = List.of(firstEpic);
        final int expectedEpicsCount = expectedEpics.size();

        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }

    @Test
    void deleteNotExistingEpicById() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + NOT_EXISTING_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 204;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH);
        final String body = getResponse.body();
        final List<Epic> epics = new ArrayList<>(Arrays.asList(gson.fromJson(body, Epic[].class)));
        final int epicsCount = epics.size();
        final List<Epic> expectedEpics = List.of(firstEpic, secondEpic);
        final int expectedEpicsCount = expectedEpics.size();

        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }

    @Test
    void deleteEpicByIncorrectId() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + INCORRECT_ID);
        final int deleteStatusCode = deleteResponse.statusCode();
        final int deleteExpectedStatusCode = 400;

        assertEquals(deleteExpectedStatusCode, deleteStatusCode, "The response status code does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH);
        final String body = getResponse.body();
        final List<Epic> epics = new ArrayList<>(Arrays.asList(gson.fromJson(body, Epic[].class)));
        final int epicsCount = epics.size();
        final List<Epic> expectedEpics = List.of(firstEpic, secondEpic);
        final int expectedEpicsCount = expectedEpics.size();

        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }

    @Test
    void deleteAllEpics() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> deleteResponse = delete(URL + "/" + ROOT_PATH + "/" + EPIC_PATH);
        final int statusCode = deleteResponse.statusCode();
        final int expectedStatusCode = 204;

        assertEquals(expectedStatusCode, statusCode, "The response status code  does not match.");

        final HttpResponse<String> getResponse = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH);
        final String body = getResponse.body();
        final List<Epic> epics = new ArrayList<>(Arrays.asList(gson.fromJson(body, Epic[].class)));
        final int epicsCount = epics.size();
        final List<Epic> expectedEpics = Collections.emptyList();
        final int expectedEpicsCount = 0;

        assertEquals(expectedEpicsCount, epicsCount, "The count of epics does not match.");
        assertIterableEquals(expectedEpics, epics, "The returned epics do not match.");
    }

    @Test
    void postEpic() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        final HttpResponse<String> firstEpicPostResponse = post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        final HttpResponse<String> secondEpicPostResponse = post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final int firstEpicPostStatusCode = firstEpicPostResponse.statusCode();
        final int secondEpicPostStatusCode = secondEpicPostResponse.statusCode();
        final int expectedStatusCode = 201;

        assertEquals(expectedStatusCode, firstEpicPostStatusCode, "The response status code does not match.");
        assertEquals(expectedStatusCode, secondEpicPostStatusCode, "The response status code does not match.");

        final HttpResponse<String> firstEpicGetResponse = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + FIRST_EPIC_ID);
        final HttpResponse<String> secondEpicGetResponse = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + SECOND_EPIC_ID);
        final String firstEpicGetResponseBody = firstEpicGetResponse.body();
        final String secondEpicGetResponseBody = secondEpicGetResponse.body();

        final Epic firstSavedEpic = gson.fromJson(firstEpicGetResponseBody, Epic.class);
        final Epic secondSavedEpic = gson.fromJson(secondEpicGetResponseBody, Epic.class);

        assertNotNull(firstSavedEpic, "Epic is not returned.");
        assertEquals(firstEpic, firstSavedEpic, "The returned epic does not match.");
        assertNotNull(secondSavedEpic, "Epic is not returned.");
        assertEquals(secondEpic, secondSavedEpic, "The returned epic does not match.");
    }

    @Test
    void putEpic() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        secondEpic.setDescription("Купить продукты и корм для кошек.");
        final String updatedSecondEpicJson = gson.toJson(secondEpic);

        final HttpResponse<String> putEpicResponse = post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, updatedSecondEpicJson);
        final int statusCode = putEpicResponse.statusCode();
        final int expectedStatusCode = 201;

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");

        final HttpResponse<String> getEpicResponse = get(URL + "/" + ROOT_PATH + "/" + EPIC_PATH + "/?id=" + SECOND_EPIC_ID);
        final String body = getEpicResponse.body();
        final Epic updatedEpic = gson.fromJson(body, Epic.class);

        assertNotNull(updatedEpic, "Epic is not returned.");
        assertEquals(secondEpic, updatedEpic, "The returned epic does not match.");
    }

    @Test
    void getEpicSubtaskByExistingId() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/" + EPIC_PATH + "/?id=" + FIRST_EPIC_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");

        final String body = response.body();
        final List<Subtask> subtasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Subtask[].class)));
        final int subtasksCount = subtasks.size();
        final List<Subtask> expectedSubtasks = List.of(firstSubtask, secondSubtask);
        final int expectedSubtasksCount = expectedSubtasks.size();

        assertEquals(expectedSubtasksCount, subtasksCount, "The count of subtasks does not match.");
        assertIterableEquals(expectedSubtasks, subtasks, "The returned subtasks do not match.");
    }

    @Test
    void getEpicSubtaskByNotExistingId() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/" + EPIC_PATH + "/?id=" + NOT_EXISTING_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 404;

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");

        final String body = response.body();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertEquals(EMPTY_BODY, body, "The returned response body is not empty.");
    }

    @Test
    void getEpicSubtaskByIncorrectId() throws IOException, InterruptedException {
        final Epic firstEpic = new Epic(FIRST_EPIC_ID, "Начать заниматься спортом.", "Пойти в спортзал.",
                FIRST_EPIC_START_TIME);
        final Epic secondEpic = new Epic(SECOND_EPIC_ID, "Сходить в магазин.", "Купить продукты.", SECOND_EPIC_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);
        firstEpic.setSubtasks(List.of(firstSubtask, secondSubtask));

        final String firstEpicJson = gson.toJson(firstEpic);
        final String secondEpicJson = gson.toJson(secondEpic);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, firstEpicJson);
        post(URL + "/" + ROOT_PATH + "/" + EPIC_PATH, secondEpicJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/" + EPIC_PATH + "/?id=" + INCORRECT_ID);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 400;

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");

        final String body = response.body();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertEquals(EMPTY_BODY, body, "The returned response body is not empty.");
    }

    @Test
    void getPrioritizedTasks() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        final String body = response.body();
        final List<Task> tasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Task[].class)));
        final int tasksCount = tasks.size();

        final List<Task> expectedTasks = List.of(firstTask, secondTask, firstSubtask, secondSubtask);
        final int expectedTasksCount = expectedTasks.size();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }

    @Test
    void getHistory() throws IOException, InterruptedException {
        final Task firstTask = new Task(FIRST_TASK_ID, "Сходить в магазин.", "Купить продукты.",
                TASK_DURATION, FIRST_TASK_START_TIME);
        final Task secondTask = new Task(SECOND_TASK_ID, "Убраться в квартире.", "Пропылесосить полы.",
                TASK_DURATION, SECOND_TASK_START_TIME);
        final Subtask firstSubtask = new Subtask(FIRST_SUBTASK_ID, "Выбрать место тренировок.", "Выбрать спортзал.",
                TASK_DURATION, FIRST_SUBTASK_START_TIME, FIRST_EPIC_ID);
        final Subtask secondSubtask = new Subtask(SECOND_SUBTASK_ID, "Записаться в зал.", "Оплатить абонемент.",
                TASK_DURATION, SECOND_SUBTASK_START_TIME, FIRST_EPIC_ID);

        final String firstTaskJson = gson.toJson(firstTask);
        final String secondTaskJson = gson.toJson(secondTask);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, firstTaskJson);
        post(URL + "/" + ROOT_PATH + "/" + TASK_PATH, secondTaskJson);

        final String firstSubtaskJson = gson.toJson(firstSubtask);
        final String secondSubtaskJson = gson.toJson(secondSubtask);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, firstSubtaskJson);
        post(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH, secondSubtaskJson);

        get(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + SECOND_TASK_ID);
        get(URL + "/" + ROOT_PATH + "/" + TASK_PATH + "/?id=" + FIRST_TASK_ID);
        get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + SECOND_SUBTASK_ID);
        get(URL + "/" + ROOT_PATH + "/" + SUBTASK_PATH + "/?id=" + FIRST_SUBTASK_ID);

        final HttpResponse<String> response = get(URL + "/" + ROOT_PATH + "/" + HISTORY_PATH);

        final int statusCode = response.statusCode();
        final int expectedStatusCode = 200;

        final String body = response.body();
        final List<Task> tasks = new ArrayList<>(Arrays.asList(gson.fromJson(body, Task[].class)));
        final int tasksCount = tasks.size();

        final List<Task> expectedTasks = List.of(secondTask, firstTask, secondSubtask, firstSubtask);
        final int expectedTasksCount = expectedTasks.size();

        assertEquals(expectedStatusCode, statusCode, "The response status code does not match.");
        assertNotNull(tasks, "Tasks are not returned.");
        assertEquals(expectedTasksCount, tasksCount, "The count of tasks does not match.");
        assertIterableEquals(expectedTasks, tasks, "The returned tasks do not match.");
    }
}