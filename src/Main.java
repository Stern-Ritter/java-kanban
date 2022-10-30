import service.FileBackendTaskManager;
import service.Managers;
import service.TaskManager;
import test.ApplicationTest;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        ApplicationTest applicationTest = new ApplicationTest();

        TaskManager firstTestTaskManager = Managers.getDefault();
        applicationTest.basicTaskManagerTest(firstTestTaskManager);

        TaskManager secondTestTaskManager = Managers.getDefault();
        applicationTest.historyTaskManagerTest(secondTestTaskManager);

        TaskManager thirdTestTaskManager = Managers.getDefault();
        applicationTest.uniqueHistoryElementsTest(thirdTestTaskManager);

        File file = new File("data/TasksData.csv");
        TaskManager firstFileBackendTaskManager = FileBackendTaskManager.loadFromFile(file);
        applicationTest.fileBackendTaskManagerSaveToFileTest(firstFileBackendTaskManager);

        TaskManager secondFileBackendTaskManager = FileBackendTaskManager.loadFromFile(file);
        applicationTest.fileBackendTaskManagerLoadFromFileTest(secondFileBackendTaskManager);
    }
}
