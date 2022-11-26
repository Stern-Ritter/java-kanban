import exceptions.ServerException;
import server.HttpTaskServer;
import server.KeyValueStorage;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new KeyValueStorage(8078).start();
        } catch (IOException | ServerException ex) {
            System.out.printf("Не удалось запустить KeyValueStorage: %s.\n", ex.getMessage());
        }

        try {
            new HttpTaskServer(8080).start();
        } catch (IOException | ServerException ex) {
            System.out.printf("Не удалось запустить HttpTaskServer: %s.\n", ex.getMessage());
        }
    }
}
