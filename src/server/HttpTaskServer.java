package server;

import com.sun.net.httpserver.HttpServer;
import handlers.tasks.EpicSubtasksHandler;
import handlers.tasks.EpicsHandler;
import handlers.tasks.HistoryHandler;
import handlers.tasks.RootHandler;
import handlers.tasks.SubtasksHandler;
import handlers.tasks.TasksHandler;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer extends Server {
    public HttpTaskServer(int port) throws IOException {
        super(port);

        TaskManager taskManager = Managers.getDefault();

        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), SOCKET_BACKLOG);

        server.createContext(RootHandler.PATH, new RootHandler(taskManager));
        server.createContext(TasksHandler.PATH, new TasksHandler(taskManager));
        server.createContext(SubtasksHandler.PATH, new SubtasksHandler(taskManager));
        server.createContext(EpicSubtasksHandler.PATH, new EpicSubtasksHandler(taskManager));
        server.createContext(EpicsHandler.PATH, new EpicsHandler(taskManager));
        server.createContext(HistoryHandler.PATH, new HistoryHandler(taskManager));
    }
}
