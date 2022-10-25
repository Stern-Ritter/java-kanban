package service;

import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import utils.HistoryManagerParser;
import utils.TaskParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackendTaskManager extends InMemoryTaskManager implements TaskManager {
    private static final String LINE_SEPARATOR = "\\r?\\n";
    private File file;

    private FileBackendTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    public static FileBackendTaskManager loadFromFile(HistoryManager historyManager, File file) {
        FileBackendTaskManager fileBackedTasksManager = new FileBackendTaskManager(historyManager, file);

        try {
            String filePath = file.getAbsolutePath();
            String data = Files.readString(Path.of(filePath));
            String[] lines = data.split(LINE_SEPARATOR);

            Map<Integer, Task> savedElements = new HashMap<>();
            int maxCurrentTaskId = 0;

            for (String line : lines) {
                if (line.isEmpty()) {
                    break;
                }

                Task task = TaskParser.fromString(line);
                if (task instanceof Epic) {
                    Epic epic = (Epic) task;
                    fileBackedTasksManager.addEpic(epic);
                    int epicId = epic.getId();
                    savedElements.put(epicId, epic);
                    maxCurrentTaskId = Math.max(maxCurrentTaskId, epicId);
                } else if (task instanceof Subtask) {
                    Subtask subtask = (Subtask) task;
                    fileBackedTasksManager.addSubtask(subtask);
                    int subtaskId = subtask.getId();
                    savedElements.put(subtaskId, subtask);
                    maxCurrentTaskId = Math.max(maxCurrentTaskId, subtaskId);
                } else {
                    fileBackedTasksManager.addTask(task);
                    int taskId = task.getId();
                    savedElements.put(taskId, task);
                    maxCurrentTaskId = Math.max(maxCurrentTaskId, taskId);
                }
            }

            fileBackedTasksManager.setTaskIdSequence(maxCurrentTaskId);

            updateHistory(historyManager, lines, savedElements);
            fileBackedTasksManager.save();

            Map<Integer, Epic> epics = fileBackedTasksManager.getEpicsStorage();
            List<Subtask> subtasks = fileBackedTasksManager.getSubtasks();
            updateEpicsSubtasks(epics, subtasks);
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException ex) {
            throw new ManagerSaveException("Ошибка чтения файла данных задач и истории просмотра.", ex);
        }

        return fileBackedTasksManager;
    }

    private static void updateHistory(HistoryManager historyManager, String[] lines, Map<Integer, Task> savedElements) {
        try {
            String lastLine = lines[lines.length - 1];
            HistoryManagerParser.historyFromString(lastLine).stream()
                    .map(savedElements::get)
                    .forEach(historyManager::add);
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            System.out.println("История просмотра задач не найдена.");
        }
    }

    private static void updateEpicsSubtasks(Map<Integer, Epic> epics, List<Subtask> subtasks) {
        Map<Integer, List<Subtask>> epicsSubtasks = new HashMap<>();

        for (Subtask subtask : subtasks) {
            int epicId = subtask.getEpicId();
            List<Subtask> currentSubtasks = epicsSubtasks.getOrDefault(epicId, new ArrayList<Subtask>());
            currentSubtasks.add(subtask);
            epicsSubtasks.put(epicId, currentSubtasks);
        }

        for (Map.Entry<Integer, List<Subtask>> entry : epicsSubtasks.entrySet()) {
            int epicId = entry.getKey();
            List<Subtask> epicSubtasks = entry.getValue();
            Epic epic = epics.get(epicId);
            epic.setSubtasks(epicSubtasks);
        }
    }

    private void save() {
        String filePath = file.getAbsolutePath();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            List<Task> tasks = getTasks();
            for (Task task : tasks) {
                String line = TaskParser.toString(task);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            List<Epic> epics = getEpics();
            for (Epic epic : epics) {
                String line = TaskParser.toString(epic);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            List<Subtask> subtasks = getSubtasks();
            for (Subtask subtask : subtasks) {
                String line = TaskParser.toString(subtask);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            bufferedWriter.newLine();

            HistoryManager historyManager = getHistoryManager();
            String line = HistoryManagerParser.historyToString(historyManager);
            bufferedWriter.write(line);
        } catch (IOException | IllegalArgumentException ex) {
            throw new ManagerSaveException("Ошибка записи файла данных задач и истории просмотра.", ex);
        }
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public boolean addTask(Task task) {
        boolean addStatus = super.addTask(task);
        save();
        return addStatus;
    }

    @Override
    public boolean addEpic(Epic epic) {
        boolean addStatus = super.addEpic(epic);
        save();
        return addStatus;
    }

    @Override
    public boolean addSubtask(Subtask subtask) {
        boolean addStatus = super.addSubtask(subtask);
        save();
        return addStatus;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean updateStatus = super.updateTask(task);
        save();
        return updateStatus;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean updateStatus = super.updateEpic(epic);
        save();
        return updateStatus;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean updateStatus = super.updateSubtask(subtask);
        save();
        return updateStatus;
    }

    @Override
    public boolean deleteTaskById(int id) {
        boolean deleteStatus = super.deleteTaskById(id);
        save();
        return deleteStatus;
    }

    @Override
    public boolean deleteEpicById(int id) {
        boolean deleteStatus = super.deleteEpicById(id);
        save();
        return deleteStatus;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        boolean deleteStatus = deleteEpicById(id);
        save();
        return deleteStatus;
    }
}
