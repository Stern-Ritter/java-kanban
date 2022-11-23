package server;

import com.sun.net.httpserver.HttpServer;
import handler.EpicSubtasksHandler;
import handler.EpicsHandler;
import handler.HistoryHandler;
import handler.RootHandler;
import handler.SubtasksHandler;
import handler.TasksHandler;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final int SOCKET_BACKLOG = 0;
    private static final int DELAY_SERVER_STOP = 1;
    private final HttpServer server;

    public HttpTaskServer() throws IOException {
        TaskManager taskManager = Managers.getDefault();

        server = HttpServer.create();
        server.bind(new InetSocketAddress(PORT), SOCKET_BACKLOG);

        server.createContext(RootHandler.PATH, new RootHandler(taskManager));
        server.createContext(TasksHandler.PATH, new TasksHandler(taskManager));
        server.createContext(SubtasksHandler.PATH, new SubtasksHandler(taskManager));
        server.createContext(EpicSubtasksHandler.PATH, new EpicSubtasksHandler(taskManager));
        server.createContext(EpicsHandler.PATH, new EpicsHandler(taskManager));
        server.createContext(HistoryHandler.PATH, new HistoryHandler(taskManager));
    }

    public void start() {
        System.out.printf("Запускаем сервер по адресу http://localhost:%d\n", PORT);
        server.start();
    }

    public void stop() {
        System.out.printf("Останавливаем сервер по адресу http://localhost:%d\n", PORT);
        server.stop(DELAY_SERVER_STOP);
    }
}
