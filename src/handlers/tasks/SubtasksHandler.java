package handlers.tasks;

import com.sun.net.httpserver.HttpExchange;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class SubtasksHandler extends Handler {
    public static final String PATH = "/tasks/subtask";

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void get(HttpExchange exchange) throws IOException {
        Map<String, String> queryParameters = getQueryParameters(exchange);
        String idParameterValue = queryParameters.get(ID_PARAMETER_NAME);

        if (idParameterValue == null) {
            List<Subtask> subtasks = taskManager.getSubtasks();
            String body = gson.toJson(subtasks);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
        } else {
            int id = parseId(idParameterValue);
            Subtask subtask = taskManager.getSubtaskById(id);

            if (subtask == null) {
                throw new NotFoundException();
            }

            String body = gson.toJson(subtask);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
        }
    }

    @Override
    protected void post(HttpExchange exchange) throws IOException {
        String body = getBody(exchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        int id = subtask.getId();
        boolean exists = taskManager.getSubtaskById(id) != null;
        boolean success;

        if (exists) {
            success = taskManager.updateSubtask(subtask);
        } else {
            success = taskManager.addSubtask(subtask);
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
            taskManager.deleteAllSubtasks();
        } else {
            int id = parseId(idParameterValue);
            taskManager.deleteSubtaskById(id);
        }

        sendResponse(exchange, HttpURLConnection.HTTP_NO_CONTENT);
    }
}
