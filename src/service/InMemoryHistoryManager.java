package service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import model.Task;
import utils.CustomLinkedList;
import utils.Node;

public class InMemoryHistoryManager implements HistoryManager {
    private final static int MAX_HISTORY_LENGTH = 10;
    private CustomLinkedList<Task> history;
    private Map<Integer, Node<Task>> uniqueHistoryElements;

    public InMemoryHistoryManager() {
        history = new CustomLinkedList<>();
        uniqueHistoryElements = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) return;

        int taskId = task.getId();
        Node<Task> historyElement = uniqueHistoryElements.get(taskId);
        if (historyElement != null) {
            history.removeNode(historyElement);
        }
        Node<Task> addedHistoryElement = history.linkLast(task);
        uniqueHistoryElements.put(taskId, addedHistoryElement);

        checkHistoryLength();
    }

    @Override
    public void remove(int id) {
        Node<Task> historyElement = uniqueHistoryElements.get(id);
        if (historyElement != null) {
            history.removeNode(historyElement);
            uniqueHistoryElements.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    private void checkHistoryLength() {
        if (history.size() > MAX_HISTORY_LENGTH) {
            Task removedTask = history.removeFirst();
            int removedTaskId = removedTask.getId();
            uniqueHistoryElements.remove(removedTaskId);
        }
    }
}
