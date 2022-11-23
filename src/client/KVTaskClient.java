package client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient implements Client {
    private static final String REGISTER_PATH = "register";
    private static final String SAVE_PATH = "save";
    private static final String LOAD_PATH = "load";
    private static final String API_TOKEN_QUERY_PARAM = "API_TOKEN";

    private HttpClient client;
    private String baseUrl;
    private String token;

    public KVTaskClient(String url) throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        baseUrl = url;
        token = getToken();
    }

    private String getToken() throws IOException, InterruptedException {
        URI uri = URI.create(String.format("%s/%s", baseUrl, REGISTER_PATH));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        URI uri = URI.create(String.format("%s/%s/%s?%s=%s", baseUrl, SAVE_PATH, key, API_TOKEN_QUERY_PARAM, token));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String load(String key) throws IOException, InterruptedException {
        URI uri = URI.create(String.format("%s/%s/%s?%s=%s", baseUrl, LOAD_PATH, key, API_TOKEN_QUERY_PARAM, token));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
