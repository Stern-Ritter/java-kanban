package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Subtask;
import service.TaskManager;
import utils.ResponseStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SubtasksHandler extends Handler implements HttpHandler {
    public static final String PATH = "/tasks/subtask";

    public SubtasksHandler(TaskManager taskManager) {
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
            List<Subtask> subtasks = taskManager.getSubtasks();
            String body = gson.toJson(subtasks);
            sendResponse(exchange, ResponseStatus.OK.getCode(), body);
        } else {
            int id = Integer.parseInt(idParameterValue);
            Subtask subtask = Optional.ofNullable(taskManager.getSubtaskById(id)).orElseThrow();
            String body = gson.toJson(subtask);
            sendResponse(exchange, ResponseStatus.OK.getCode(), body);
        }
    }

    private void post(HttpExchange exchange) throws IOException {
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
            taskManager.deleteAllSubtasks();
        } else {
            int id = Integer.parseInt(idParameterValue);
            taskManager.deleteSubtaskById(id);
        }

        sendResponse(exchange, ResponseStatus.NO_CONTENT.getCode());
    }
}
