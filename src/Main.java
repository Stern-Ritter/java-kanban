import service.TaskManager;
import test.ApplicationTest;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        ApplicationTest applicationTest = new ApplicationTest();
        applicationTest.basicTaskManagerTest(taskManager);
    }
}
