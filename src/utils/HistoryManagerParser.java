package utils;

import model.Task;
import service.HistoryManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryManagerParser {
    public static String historyToString(HistoryManager manager) {
        return manager.getHistory().stream()
                .map(Task::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    public static List<Integer> historyFromString(String value) throws IllegalArgumentException {
        String[] parts = value.split(",");
        return Arrays.stream(parts)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
