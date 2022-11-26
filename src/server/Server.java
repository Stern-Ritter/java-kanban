package server;

import com.sun.net.httpserver.HttpServer;

public abstract class Server {
    protected static final int SOCKET_BACKLOG = 0;
    protected static final int DELAY_SERVER_STOP = 1;
    protected int port;
    protected HttpServer server;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        System.out.printf("Запускаем сервер по адресу http://localhost:%d\n", port);
        server.start();
    }

    public void stop() {
        System.out.printf("Останавливаем сервер по адресу http://localhost:%d\n", port);
        server.stop(DELAY_SERVER_STOP);
    }
}
