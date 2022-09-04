import model.Epic;
import model.Subtask;
import model.Task;
import service.TaskManager;
import utils.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task firstTask = new Task("Сходить в магазин.", "Купить продукты.");
        Task secondTask = new Task("Убраться в квартире.", "Пропылесосить полы.");

        Epic firstEpic = new Epic("Начать заниматься спортом.", "Пойти в спортзал.");
        Subtask firstEpicFirstSubtask = new Subtask(
                "Выбрать место тренировок.",
                "Выбрать спортзал.",
                firstEpic);
        Subtask firstEpicSecondSubtask = new Subtask(
                "Записаться в зал.",
                "Оплатить абонемент.",
                firstEpic);
        firstEpic.setSubtasks(new ArrayList<>(Arrays.asList(firstEpicFirstSubtask, firstEpicSecondSubtask)));

        Epic secondEpic = new Epic("Купить лежак для кошки.", "Купить лежак для кошки на подоконник.");
        Subtask secondEpicFirstSubtask = new Subtask(
                "Заказать лежак в интернет магазине.",
                "Выбрать и заказать лежак в интернет магазине.",
                secondEpic);
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
    }
}
