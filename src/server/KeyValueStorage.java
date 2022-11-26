package server;

import com.sun.net.httpserver.HttpServer;
import handlers.storage.LoadHandler;
import handlers.storage.RegisterHandler;
import handlers.storage.SaveHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class KeyValueStorage extends Server {
    private final String apiToken;

    private static String generateApiToken() {
        return Long.toString(System.currentTimeMillis());
    }

    public KeyValueStorage(int port) throws IOException {
        super(port);

        apiToken = generateApiToken();
        Map<String, String> storage = new HashMap<>();

        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        server.createContext(RegisterHandler.PATH, new RegisterHandler(apiToken, storage));
        server.createContext(SaveHandler.PATH, new SaveHandler(apiToken, storage));
        server.createContext(LoadHandler.PATH, new LoadHandler(apiToken, storage));
    }
}
