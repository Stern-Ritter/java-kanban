package handlers.storage;

import com.sun.net.httpserver.HttpExchange;
import exceptions.UnauthorizedException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Optional;

public class LoadHandler extends Handler {
    public static final String PATH = "/load";

    public LoadHandler(String apiToken, Map<String, String> storage) {
        super(apiToken, storage);
    }

    @Override
    protected void get(HttpExchange exchange) throws IOException {
        if (hasNotAuth(exchange)) throw new UnauthorizedException();
        String key = exchange.getRequestURI().getPath().substring("/load/".length());
        if (key.isEmpty()) {
            throw new IllegalArgumentException();
        } else {
            String value = Optional.ofNullable(storage.get(key)).orElseThrow();
            sendResponse(exchange, HttpURLConnection.HTTP_OK, value);
        }
    }
}
