import service.TaskManager;
import test.ApplicationTest;
import service.Managers;

public class Main {

    public static void main(String[] args) {
        ApplicationTest applicationTest = new ApplicationTest();

        TaskManager firstTestTaskManager = Managers.getDefault();
        applicationTest.basicTaskManagerTest(firstTestTaskManager);

        TaskManager secondTestTaskManager = Managers.getDefault();
        applicationTest.historyTaskManagerTest(secondTestTaskManager);

        TaskManager thirdTestTaskManager = Managers.getDefault();
        applicationTest.uniqueHistoryElementsTest(thirdTestTaskManager);
    }
}
