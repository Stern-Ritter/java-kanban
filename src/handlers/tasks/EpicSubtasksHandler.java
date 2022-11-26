package handlers.tasks;

import com.sun.net.httpserver.HttpExchange;
import model.Epic;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class EpicSubtasksHandler extends Handler {
    public static final String PATH = "/tasks/subtask/epic";

    public EpicSubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void get(HttpExchange exchange) throws IOException {
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
            sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
        }
    }
}
