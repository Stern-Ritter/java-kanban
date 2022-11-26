package handlers.storage;

import com.sun.net.httpserver.HttpExchange;
import exceptions.BadRequestException;
import exceptions.UnauthorizedException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

public class SaveHandler extends Handler {
    public static final String PATH = "/save";

    public SaveHandler(String apiToken, Map<String, String> storage) {
        super(apiToken, storage);
    }

    @Override
    protected void post(HttpExchange exchange) throws IOException {
        if (hasNotAuth(exchange)) throw new UnauthorizedException();

        String key = exchange.getRequestURI().getPath().substring("/save/".length());
        String value = getBody(exchange);

        if (key.isEmpty() || value.isEmpty()) {
            throw new BadRequestException();
        }

        storage.put(key, value);
        sendResponse(exchange, HttpURLConnection.HTTP_OK);
    }
}
