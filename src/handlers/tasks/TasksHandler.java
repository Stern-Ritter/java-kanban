package handlers.tasks;

import com.sun.net.httpserver.HttpExchange;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class TasksHandler extends Handler {
    public static final String PATH = "/tasks/task";

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void get(HttpExchange exchange) throws IOException {
        Map<String, String> queryParameters = getQueryParameters(exchange);
        String idParameterValue = queryParameters.get(ID_PARAMETER_NAME);

        if (idParameterValue == null) {
            List<Task> tasks = taskManager.getTasks();
            String body = gson.toJson(tasks);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
        } else {
            int id = parseId(idParameterValue);
            Task task = taskManager.getTaskById(id);

            if (task == null) {
                throw new NotFoundException();
            }

            String body = gson.toJson(task);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
        }
    }

    @Override
    protected void post(HttpExchange exchange) throws IOException {
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

        if (!success) {
            throw new BadRequestException();
        }

        sendResponse(exchange, HttpURLConnection.HTTP_CREATED);
    }

    @Override
    protected void delete(HttpExchange exchange) throws IOException {
        Map<String, String> queryParameters = getQueryParameters(exchange);
        String idParameterValue = queryParameters.get(ID_PARAMETER_NAME);

        if (idParameterValue == null) {
            taskManager.deleteAllTasks();
        } else {
            int id = parseId(idParameterValue);
            taskManager.deleteTaskById(id);
        }

        sendResponse(exchange, HttpURLConnection.HTTP_NO_CONTENT);
    }
}
