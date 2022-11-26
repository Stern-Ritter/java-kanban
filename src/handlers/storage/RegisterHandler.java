package handlers.storage;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

public class RegisterHandler extends Handler {
    public static final String PATH = "/register";

    public RegisterHandler(String apiToken, Map<String, String> storage) {
        super(apiToken, storage);
    }

    @Override
    protected void get(HttpExchange exchange) throws IOException {
        sendResponse(exchange, HttpURLConnection.HTTP_OK, apiToken);
    }
}
