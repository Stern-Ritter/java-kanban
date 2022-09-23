package service;

import model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private final static int MAX_HISTORY_LENGTH = 10;
    private final static int FIRST_HISTORY_ELEMENT_INDEX = 0;
    private List<Task> history;

    public InMemoryHistoryManager() {
        this.history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            if (history.size() > MAX_HISTORY_LENGTH) {
                history.remove(FIRST_HISTORY_ELEMENT_INDEX);
            }
            history.add(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return this.history;
    }
}
