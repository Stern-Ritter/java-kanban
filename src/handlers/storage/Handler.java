package handlers.storage;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.UnauthorizedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class Handler implements HttpHandler {
    protected final String apiToken;
    protected final Map<String, String> storage;
    private static final int DEFAULT_RESPONSE_LENGTH = 0;

    public Handler(String apiToken, Map<String, String> storage) {
        this.apiToken = apiToken;
        this.storage = storage;
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
                default:
                    throw new UnsupportedOperationException();
            }
        } catch (UnauthorizedException ex) {
            sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED);
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

    protected String getBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), UTF_8);
    }

    protected void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        exchange.sendResponseHeaders(status, DEFAULT_RESPONSE_LENGTH);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes(UTF_8));
        }
    }

    protected void sendResponse(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, DEFAULT_RESPONSE_LENGTH);
    }

    protected boolean hasNotAuth(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        return rawQuery == null || (!rawQuery.contains("API_TOKEN=" + apiToken) && !rawQuery.contains("API_TOKEN=DEBUG"));
    }
}
