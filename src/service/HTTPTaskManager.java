package service;

import client.Client;
import client.KeyValueStorageClient;
import exceptions.HttpClientException;
import model.Task;
import utils.HistoryManagerParser;
import utils.TaskParser;

import java.io.IOException;
import java.util.List;

public class HTTPTaskManager extends FileBackendTaskManager {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public String apiKey;
    private Client client;

    public HTTPTaskManager(HistoryManager historyManager, String apiKey, Client client) {
        super(historyManager);
        this.apiKey = apiKey;
        this.client = client;
    }

    private String[] getLinesFromServer(Client client) throws IOException, InterruptedException {
        String data = client.load(apiKey);
        return data.split(LINE_SEPARATOR);
    }

    @Override
    protected void save() {
        StringBuilder state = new StringBuilder();
        List<Task> allTasks = getAllTasks();
        String historyLine = HistoryManagerParser.historyToString(getHistoryManager());
        for (Task task : allTasks) {
            String taskLine = TaskParser.toString(task);
            state.append(taskLine);
            state.append(LINE_SEPARATOR);
        }
        state.append(LINE_SEPARATOR);
        state.append(historyLine);

        try {
            client.put(apiKey, state.toString());
        } catch (IOException | InterruptedException ex) {
            throw new HttpClientException("Ошибка сохранения данных на сервер.", ex);
        }
    }

    public static HTTPTaskManager loadFromServer(String url, String apiKey) {
        try {
            HistoryManager historyManager = Managers.getDefaultHistory();
            Client client = new KeyValueStorageClient(url);
            HTTPTaskManager tasksManager = new HTTPTaskManager(historyManager, apiKey, client);

            String[] lines = tasksManager.getLinesFromServer(client);
            tasksManager.loadTasks(lines);
            tasksManager.updateEpicsSubtasks();
            tasksManager.loadHistory(lines);
            tasksManager.updateTaskIdSequence();
            tasksManager.save();

            return tasksManager;
        } catch (IOException | InterruptedException | IndexOutOfBoundsException | IllegalArgumentException ex) {
            throw new HttpClientException("Ошибка чтения данных задач и истории просмотра c сервера.", ex);
        }
    }
}
