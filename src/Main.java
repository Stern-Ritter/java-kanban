import service.FileBackendTaskManager;
import service.HistoryManager;
import service.InMemoryHistoryManager;
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
        HistoryManager firstHistoryManager = new InMemoryHistoryManager();
        TaskManager firstFileBackendTaskManager = FileBackendTaskManager.loadFromFile(firstHistoryManager, file);
        applicationTest.fileBackendTaskManagerSaveToFileTest(firstFileBackendTaskManager);

        HistoryManager secondHistoryManager = new InMemoryHistoryManager();
        TaskManager secondFileBackendTaskManager = FileBackendTaskManager.loadFromFile(secondHistoryManager, file);
        applicationTest.fileBackendTaskManagerLoadFromFileTest(secondFileBackendTaskManager);
    }
}
