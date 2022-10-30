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
import java.util.function.Function;
import java.util.stream.Stream;

public class FileBackendTaskManager extends InMemoryTaskManager implements TaskManager {
    private static final String LINE_SEPARATOR = "\\r?\\n";
    private File file;

    private FileBackendTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    private static String[] getLinesFromFile(File file) throws IOException {
        String filePath = file.getAbsolutePath();
        String data = Files.readString(Path.of(filePath));
        return data.split(LINE_SEPARATOR);
    }

    public static FileBackendTaskManager loadFromFile(File file) {
        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackendTaskManager tasksManager = new FileBackendTaskManager(historyManager, file);

        try {
            String[] lines = getLinesFromFile(file);
            tasksManager.loadTasks(lines);
            tasksManager.updateEpicsSubtasks();
            tasksManager.loadHistory(lines);
            tasksManager.updateTaskIdSequence();
            tasksManager.save();
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException ex) {
            throw new ManagerSaveException("Ошибка чтения файла данных задач и истории просмотра.", ex);
        }

        return tasksManager;
    }

    private void loadTasks(String[] lines) throws IndexOutOfBoundsException, IllegalArgumentException {
        for (String line : lines) {
            if (line.isEmpty()) { break; }
            Task task = TaskParser.fromString(line);
            addSavedTask(task);
        }
    }

    private void updateEpicsSubtasks() {
        Map<Integer, Epic> epics = getEpicsStorage();
        List<Subtask> subtasks = getSubtasks();
        Map<Integer, List<Subtask>> epicsSubtasks = new HashMap<>();

        for (Subtask subtask : subtasks) {
            int epicId = subtask.getEpicId();
            List<Subtask> currentSubtasks = epicsSubtasks.getOrDefault(epicId, new ArrayList<>());
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

    private void loadHistory(String[] lines) {
        try {
            String lastLine = lines[lines.length - 1];
            List<Integer> tasksId = HistoryManagerParser.historyFromString(lastLine);
            HistoryManager historyManager = getHistoryManager();
            Map<Integer, Task> tasks = getAllTasksStorage();

            for(Integer taskId : tasksId) {
                Task task = tasks.get(taskId);
                historyManager.add(task);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            System.out.println("История просмотра задач не найдена.");
        }
    }

    private void updateTaskIdSequence() {
        Stream<Integer> tasksId = getTasks().stream().map(Task::getId);
        Stream<Integer> epicsId = getEpics().stream().map(Task::getId);
        Stream<Integer> subtasksId = getSubtasks().stream().map(Task::getId);
        int maxCurrentTaskId = Stream.of(tasksId, epicsId, subtasksId)
                .flatMap(Function.identity())
                .max(Integer::compare)
                .orElse(TASK_ID_INITIAL_VALUE);

        setTaskIdSequence(maxCurrentTaskId);
    }

    private void save() {
        String filePath = file.getAbsolutePath();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
            List<Task> allTasks = getAllTasks();
            String historyLine = HistoryManagerParser.historyToString(getHistoryManager());
            
            for (Task task : allTasks) {
                String taskLine = TaskParser.toString(task);
                bufferedWriter.write(taskLine);
                bufferedWriter.newLine();
            }
            bufferedWriter.newLine();
            bufferedWriter.write(historyLine);
        } catch (IOException | IllegalArgumentException ex) {
            throw new ManagerSaveException("Ошибка записи файла данных задач и истории просмотра.", ex);
        }
    }

    private void addSavedTask(Task task) {
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            addEpic(epic);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            addSubtask(subtask);
        } else {
            addTask(task);
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
