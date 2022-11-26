package handlers.storage;

import com.sun.net.httpserver.HttpExchange;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.UnauthorizedException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

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
            throw new BadRequestException();
        } else {
            String value = storage.get(key);
            if (value == null) {
                throw new NotFoundException();
            }

            sendResponse(exchange, HttpURLConnection.HTTP_OK, value);
        }
    }
}
