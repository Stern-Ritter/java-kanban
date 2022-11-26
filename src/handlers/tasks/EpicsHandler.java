package handlers.tasks;

import com.sun.net.httpserver.HttpExchange;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import model.Epic;
import service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class EpicsHandler extends Handler {
    public static final String PATH = "/tasks/epic";

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void get(HttpExchange exchange) throws IOException {
        Map<String, String> queryParameters = getQueryParameters(exchange);
        String idParameterValue = queryParameters.get(ID_PARAMETER_NAME);

        if (idParameterValue == null) {
            List<Epic> epics = taskManager.getEpics();
            String body = gson.toJson(epics);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
        } else {
            int id = parseId(idParameterValue);
            Epic epic = taskManager.getEpicById(id);

            if (epic == null) {
                throw new NotFoundException();
            }

            String body = gson.toJson(epic);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
        }
    }

    @Override
    protected void post(HttpExchange exchange) throws IOException {
        String body = getBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);
        int id = epic.getId();
        boolean exists = taskManager.getEpicById(id) != null;
        boolean success;

        if (exists) {
            success = taskManager.updateEpic(epic);
        } else {
            success = taskManager.addEpic(epic);
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
            taskManager.deleteAllEpics();
        } else {
            int id = parseId(idParameterValue);
            taskManager.deleteEpicById(id);
        }

        sendResponse(exchange, HttpURLConnection.HTTP_NO_CONTENT);
    }
}
