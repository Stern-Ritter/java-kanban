import exceptions.HttpClientException;
import server.HttpTaskServer;
import server.KeyValueStorage;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new KeyValueStorage(8078).start();
        } catch (IOException | HttpClientException ex) {
            System.out.println("Не удалось запустить KVServer.");
        }

        try {
            new HttpTaskServer(8080).start();
        } catch (IOException ex) {
            System.out.println("Не удалось запустить HttpTaskServer.");
        }
    }
}
