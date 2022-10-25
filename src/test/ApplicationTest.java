package test;

import model.Epic;
import model.Subtask;
import model.Task;
import service.TaskManager;
import utils.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationTest {
    private void printBreakLine() {
        System.out.println("-".repeat(12));
    }

    public void basicTaskManagerTest(TaskManager taskManager) {
        Task firstTask = new Task(taskManager.getNextTaskId(), "Сходить в магазин.", "Купить продукты.");
        Task secondTask = new Task(taskManager.getNextTaskId(), "Убраться в квартире.", "Пропылесосить полы.");

        Epic firstEpic = new Epic(taskManager.getNextTaskId(), "Начать заниматься спортом.", "Пойти в спортзал.");
        Subtask firstEpicFirstSubtask = new Subtask(taskManager.getNextTaskId(),
                "Выбрать место тренировок.",
                "Выбрать спортзал.",
                firstEpic.getId());
        Subtask firstEpicSecondSubtask = new Subtask(taskManager.getNextTaskId(),
                "Записаться в зал.",
                "Оплатить абонемент.",
                firstEpic.getId());
        firstEpic.setSubtasks(new ArrayList<>(Arrays.asList(firstEpicFirstSubtask, firstEpicSecondSubtask)));

        Epic secondEpic = new Epic(taskManager.getNextTaskId(), "Купить лежак для кошки.", "Купить лежак для кошки на подоконник.");
        Subtask secondEpicFirstSubtask = new Subtask(
                taskManager.getNextTaskId(),
                "Заказать лежак в интернет магазине.",
                "Выбрать и заказать лежак в интернет магазине.",
                secondEpic.getId());
        secondEpic.setSubtasks(new ArrayList<>(List.of(secondEpicFirstSubtask)));

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstEpicFirstSubtask);
        taskManager.addSubtask(firstEpicSecondSubtask);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(secondEpicFirstSubtask);

        List<Task> tasks = taskManager.getTasks();
        List<Epic> epics = taskManager.getEpics();
        List<Subtask> subtasks = taskManager.getSubtasks();

        System.out.println(tasks);
        System.out.println(epics);
        System.out.println(subtasks);


        firstTask.setStatus(Status.IN_PROGRESS);
        secondTask.setStatus(Status.DONE);
        System.out.println(taskManager.getTaskById(firstTask.getId()));
        System.out.println(taskManager.getTaskById(secondTask.getId()));

        firstEpicFirstSubtask.setStatus(Status.DONE);
        firstEpicSecondSubtask.setStatus(Status.DONE);
        firstEpic.setStatus(Status.IN_PROGRESS);
        System.out.println(taskManager.getEpicById(firstEpic.getId()));

        secondEpicFirstSubtask.setStatus(Status.IN_PROGRESS);
        secondEpic.setStatus(Status.NEW);
        System.out.println(taskManager.getEpicById(secondEpic.getId()));

        taskManager.deleteTaskById(firstTask.getId());
        tasks = taskManager.getTasks();
        System.out.println(tasks);

        taskManager.deleteEpicById(firstEpic.getId());
        epics = taskManager.getEpics();
        subtasks = taskManager.getSubtasks();
        System.out.println(epics);
        System.out.println(subtasks);

        printBreakLine();
    }

    public void historyTaskManagerTest(TaskManager taskManager) {
        Task task = new Task(taskManager.getNextTaskId(), "Сходить в магазин.", "Купить продукты.");
        Epic epic = new Epic(taskManager.getNextTaskId(), "Начать заниматься спортом.", "Пойти в спортзал.");
        Subtask subtask = new Subtask(taskManager.getNextTaskId(), "Выбрать место тренировок.", "Выбрать спортзал.", epic.getId());

        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);

        taskManager.getTaskById(task.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epic.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(subtask.getId());
        System.out.println(taskManager.getHistory());

        taskManager.getTaskById(task.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epic.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(subtask.getId());
        System.out.println(taskManager.getHistory());

        taskManager.getTaskById(task.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epic.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(subtask.getId());
        System.out.println(taskManager.getHistory());

        taskManager.getTaskById(task.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epic.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(subtask.getId());
        System.out.println(taskManager.getHistory());

        printBreakLine();
    }

    public void uniqueHistoryElementsTest(TaskManager taskManager) {
        Task firstTask = new Task(taskManager.getNextTaskId(), "Сходить в магазин.", "Купить продукты.");
        Task secondTask = new Task(taskManager.getNextTaskId(), "Убраться в квартире.", "Пропылесосить полы.");
        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);

        Epic firstEpic = new Epic(taskManager.getNextTaskId(), "Начать заниматься спортом.", "Пойти в спортзал.");
        Subtask firstEpicFirstSubtask = new Subtask(
                taskManager.getNextTaskId(),
                "Выбрать место тренировок.",
                "Выбрать спортзал.",
                firstEpic.getId());
        Subtask firstEpicSecondSubtask = new Subtask(
                taskManager.getNextTaskId(),
                "Записаться в зал.",
                "Оплатить абонемент.",
                firstEpic.getId());
        Subtask firstEpicThirdSubtask = new Subtask(
                taskManager.getNextTaskId(),
                "Записаться в зал.",
                "Сходить на пробную тренировку.",
                firstEpic.getId());
        firstEpic.setSubtasks(new ArrayList<>(Arrays.asList(firstEpicFirstSubtask, firstEpicSecondSubtask, firstEpicThirdSubtask)));
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstEpicFirstSubtask);
        taskManager.addSubtask(firstEpicSecondSubtask);
        taskManager.addSubtask(firstEpicThirdSubtask);

        Epic secondEpic = new Epic(taskManager.getNextTaskId(), "Купить лежак для кошки.", "Купить лежак для кошки на подоконник.");
        taskManager.addEpic(secondEpic);

        taskManager.getTaskById(firstTask.getId());
        taskManager.getTaskById(secondTask.getId());
        taskManager.getEpicById(firstEpic.getId());
        taskManager.getEpicById(secondEpic.getId());
        taskManager.getSubtaskById(firstEpicFirstSubtask.getId());
        taskManager.getSubtaskById(firstEpicSecondSubtask.getId());
        taskManager.getSubtaskById(firstEpicThirdSubtask.getId());
        System.out.println(taskManager.getHistory());

        taskManager.getTaskById(firstTask.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getTaskById(secondTask.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(firstEpic.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(firstEpicFirstSubtask.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(firstEpicSecondSubtask.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(firstEpicThirdSubtask.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(secondEpic.getId());
        System.out.println(taskManager.getHistory());

        taskManager.deleteTaskById(secondTask.getId());
        System.out.println(taskManager.getHistory());

        taskManager.deleteEpicById(firstEpic.getId());
        System.out.println(taskManager.getHistory());

        printBreakLine();
    }

    public void fileBackendTaskManagerSaveToFileTest(TaskManager taskManager) {
        Task firstTask = new Task(taskManager.getNextTaskId(), "Сходить в магазин.", "Купить продукты.");
        Task secondTask = new Task(taskManager.getNextTaskId(), "Убраться в квартире.", "Пропылесосить полы.");

        Epic firstEpic = new Epic(taskManager.getNextTaskId(), "Начать заниматься спортом.", "Пойти в спортзал.");
        Subtask firstEpicFirstSubtask = new Subtask(taskManager.getNextTaskId(),
                "Выбрать место тренировок.",
                "Выбрать спортзал.",
                firstEpic.getId());
        Subtask firstEpicSecondSubtask = new Subtask(taskManager.getNextTaskId(),
                "Записаться в зал.",
                "Оплатить абонемент.",
                firstEpic.getId());
        firstEpic.setSubtasks(new ArrayList<>(Arrays.asList(firstEpicFirstSubtask, firstEpicSecondSubtask)));

        Epic secondEpic = new Epic(taskManager.getNextTaskId(), "Купить лежак для кошки.", "Купить лежак для кошки на подоконник.");
        Subtask secondEpicFirstSubtask = new Subtask(
                taskManager.getNextTaskId(),
                "Заказать лежак в интернет магазине.",
                "Выбрать и заказать лежак в интернет магазине.",
                secondEpic.getId());
        secondEpic.setSubtasks(new ArrayList<>(List.of(secondEpicFirstSubtask)));

        taskManager.addTask(firstTask);
        taskManager.addTask(secondTask);
        taskManager.addEpic(firstEpic);
        taskManager.addSubtask(firstEpicFirstSubtask);
        taskManager.addSubtask(firstEpicSecondSubtask);
        taskManager.addEpic(secondEpic);
        taskManager.addSubtask(secondEpicFirstSubtask);

        List<Task> tasks = taskManager.getTasks();
        List<Epic> epics = taskManager.getEpics();
        List<Subtask> subtasks = taskManager.getSubtasks();

        System.out.println(tasks);
        System.out.println(epics);
        System.out.println(subtasks);

        taskManager.getTaskById(firstTask.getId());
        taskManager.getTaskById(secondTask.getId());
        taskManager.getEpicById(firstEpic.getId());
        taskManager.getEpicById(secondEpic.getId());
        taskManager.getSubtaskById(firstEpicFirstSubtask.getId());
        taskManager.getSubtaskById(firstEpicSecondSubtask.getId());
        taskManager.getSubtaskById(secondEpicFirstSubtask.getId());
        System.out.println(taskManager.getHistory());

        printBreakLine();
    }

    public void fileBackendTaskManagerLoadFromFileTest(TaskManager taskManager) {
        List<Task> tasks = taskManager.getTasks();
        List<Epic> epics = taskManager.getEpics();
        List<Subtask> subtasks = taskManager.getSubtasks();

        System.out.println(tasks);
        System.out.println(epics);
        System.out.println(subtasks);
        System.out.println(taskManager.getHistory());

        printBreakLine();
    }
}
