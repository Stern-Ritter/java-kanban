package service;

import java.io.File;

public class Managers {
    private final static String FILE_PATH = "data/TasksData.csv";
    private final static String HOST = "http://localhost:8078";
    private final static String API_KEY = "history";

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefault() {
        return HTTPTaskManager.loadFromServer(HOST, API_KEY);
    }

    public static TaskManager getStateFull() {
        File file = new File(FILE_PATH);
        return FileBackendTaskManager.loadFromFile(file);
    }
}
