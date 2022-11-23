import exceptions.HttpClientException;
import server.HttpTaskServer;
import server.KVServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new KVServer().start();
        } catch (IOException | HttpClientException ex) {
            System.out.println("Не удалось запустить KVServer.");
        }

        try {
            new HttpTaskServer().start();
        } catch (IOException ex) {
            System.out.println("Не удалось запустить HttpTaskServer.");
        }
    }
}
