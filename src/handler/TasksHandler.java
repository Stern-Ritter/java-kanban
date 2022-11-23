package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import service.TaskManager;
import utils.ResponseStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class TasksHandler extends Handler implements HttpHandler {
    public static final String PATH = "/tasks/task";

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    get(exchange);
                    break;
                case "POST":
                    post(exchange);
                    break;
                case "DELETE":
                    delete(exchange);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        } catch (NoSuchElementException ex) {
            sendResponse(exchange, ResponseStatus.NOT_FOUND.getCode());
        } catch (IllegalArgumentException ex) {
            sendResponse(exchange, ResponseStatus.BAD_REQUEST.getCode());
        } catch (UnsupportedOperationException ex) {
            sendResponse(exchange, ResponseStatus.METHOD_NOT_ALLOWED.getCode());
        } catch (Error error) {
            sendResponse(exchange, ResponseStatus.INTERNAL_SERVER_ERROR.getCode());
            throw new Error(error);
        } finally {
            exchange.close();
        }
    }

    private void get(HttpExchange exchange) throws IOException {
        Map<String, String> queryParameters = getQueryParameters(exchange);
        String idParameterValue = queryParameters.get(ID_PARAMETER_NAME);

        if (idParameterValue == null) {
            List<Task> tasks = taskManager.getTasks();
            String body = gson.toJson(tasks);
            sendResponse(exchange, ResponseStatus.OK.getCode(), body);
        } else {
            int id = Integer.parseInt(idParameterValue);
            Task task = Optional.ofNullable(taskManager.getTaskById(id)).orElseThrow();
            String body = gson.toJson(task);
            sendResponse(exchange, ResponseStatus.OK.getCode(), body);
        }
    }

    private void post(HttpExchange exchange) throws IOException {
        String body = getBody(exchange);
        Task task = gson.fromJson(body, Task.class);
        int id = task.getId();
        boolean exists = taskManager.getTaskById(id) != null;
        boolean success;

        if (exists) {
            success = taskManager.updateTask(task);
        } else {
            success = taskManager.addTask(task);
        }

        if (success) {
            sendResponse(exchange, ResponseStatus.CREATED.getCode());
        } else {
            sendResponse(exchange, ResponseStatus.BAD_REQUEST.getCode());
        }
    }

    private void delete(HttpExchange exchange) throws IOException {
        Map<String, String> queryParameters = getQueryParameters(exchange);
        String idParameterValue = queryParameters.get(ID_PARAMETER_NAME);

        if (idParameterValue == null) {
            taskManager.deleteAllTasks();
        } else {
            int id = Integer.parseInt(idParameterValue);
            taskManager.deleteTaskById(id);
        }

        sendResponse(exchange, ResponseStatus.NO_CONTENT.getCode());
    }
}
