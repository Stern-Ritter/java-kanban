package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Epic;
import model.Subtask;
import service.TaskManager;
import utils.ResponseStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class EpicSubtasksHandler extends Handler implements HttpHandler {
    public static final String PATH = "/tasks/subtask/epic";

    public EpicSubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equals(method)) {
                get(exchange);
            } else {
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
            throw new IllegalArgumentException();
        } else {
            int id = Integer.parseInt(idParameterValue);
            Epic epic = taskManager.getEpicById(id);

            if (epic == null) {
                throw new NoSuchElementException();
            }

            List<Subtask> subtasks = taskManager.getEpicSubtasksById(id);
            String body = gson.toJson(subtasks);
            sendResponse(exchange, ResponseStatus.OK.getCode(), body);
        }
    }
}
