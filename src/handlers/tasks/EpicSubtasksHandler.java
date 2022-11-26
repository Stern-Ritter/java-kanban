package handlers.tasks;

import com.sun.net.httpserver.HttpExchange;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import model.Epic;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

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
            throw new BadRequestException();
        } else {
            int id = parseId(idParameterValue);
            Epic epic = taskManager.getEpicById(id);

            if (epic == null) {
                throw new NotFoundException();
            }

            List<Subtask> subtasks = taskManager.getEpicSubtasksById(id);
            String body = gson.toJson(subtasks);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
        }
    }
}
