package service;

import client.Client;
import client.KeyValueStorageClient;
import exceptions.HttpClientException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import server.HttpTaskServer;
import server.KeyValueStorage;

import java.io.IOException;

public class HttpTaskManagerTest extends FileBackendTaskManagerTest {
    private static final int HTTP_TASK_SERVER_PORT = 8080;
    private static final int KEY_VALUE_STORAGE_PORT = 8078;
    private static final String TEST_URL = String.format("http://localhost:%d", KEY_VALUE_STORAGE_PORT);
    private static final String TEST_API_KEY = "test";
    private Client client;

    @BeforeAll
    static void startServer() {
        try {
            new KeyValueStorage(KEY_VALUE_STORAGE_PORT).start();
        } catch (IOException | HttpClientException ex) {
            System.out.println("Не удалось запустить KVServer.");
        }

        try {
            new HttpTaskServer(HTTP_TASK_SERVER_PORT).start();
        } catch (IOException ex) {
            System.out.println("Не удалось запустить HttpTaskServer.");
        }
    }

    @BeforeEach
    void clearStateBetweenTest() {
        try {
            client.put(TEST_API_KEY, "");
        } catch (IOException | InterruptedException ex) {
            System.out.println("Ошибка удаления всех тестовых данных на сервере перед тестом.");
        }
    }

    @Override
    public FileBackendTaskManager getTaskManager() {
        try {
            HistoryManager historyManager = Managers.getDefaultHistory();
            client = new KeyValueStorageClient(TEST_URL);
            return new HTTPTaskManager(historyManager, TEST_API_KEY, client);
        } catch (IOException | InterruptedException ex) {
            throw new HttpClientException("Ошибка запуска сервера.", ex);
        }
    }

    @Override
    protected TaskManager createTaskManagerInstance() {
        return taskManager;
    }

    @Override
    protected String getState() {
        try {
            return client.load(TEST_API_KEY);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(String.format("Ошибка чтения данных с сервера: '%s'.\n", TEST_URL), ex);
        }
    }
}
