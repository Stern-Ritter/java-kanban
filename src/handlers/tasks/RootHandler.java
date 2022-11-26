package handlers.tasks;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

public class RootHandler extends Handler {
    public static final String PATH = "/tasks";

    public RootHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void get(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getPrioritizedTasks();
        String body = gson.toJson(tasks);
        sendResponse(exchange, HttpURLConnection.HTTP_OK, body);
    }
}
