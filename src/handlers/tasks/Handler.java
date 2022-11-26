package handlers.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Epic;
import model.Subtask;
import model.Task;
import service.TaskManager;
import utils.serializer.EpicSerializer;
import utils.serializer.LocalDateTimeAdapter;
import utils.serializer.SubtaskSerializer;
import utils.serializer.TaskSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class Handler implements HttpHandler {
    private static final int DEFAULT_RESPONSE_LENGTH = 0;
    private static final String QUERY_PARAMS_SEPARATOR = "&";
    private static final String QUERY_VALUES_SEPARATOR = "=";
    protected static final String ID_PARAMETER_NAME = "id";

    protected TaskManager taskManager;
    protected Gson gson;

    public Handler(TaskManager taskManager) {
        this.taskManager = taskManager;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskSerializer(taskManager));
        gsonBuilder.registerTypeAdapter(Subtask.class, new SubtaskSerializer(taskManager));
        gsonBuilder.registerTypeAdapter(Epic.class, new EpicSerializer(taskManager));
        gson = gsonBuilder.create();
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
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (UnsupportedOperationException ex) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD);
        } catch (Error error) {
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR);
            throw new Error(error);
        } finally {
            exchange.close();
        }
    }

    protected void get(HttpExchange exchange) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected void post(HttpExchange exchange) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected void delete(HttpExchange exchange) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected Map<String, String> getQueryParameters(HttpExchange exchange) {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            String[] pairs = query.split(QUERY_PARAMS_SEPARATOR);
            for (String pair : pairs) {
                int separatorIndex = pair.indexOf(QUERY_VALUES_SEPARATOR);
                queryParameters.put(URLDecoder.decode(pair.substring(0, separatorIndex), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(separatorIndex + 1), StandardCharsets.UTF_8));
            }
        }
        return queryParameters;
    }

    protected String getBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        exchange.sendResponseHeaders(status, DEFAULT_RESPONSE_LENGTH);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected void sendResponse(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, DEFAULT_RESPONSE_LENGTH);
    }
}
