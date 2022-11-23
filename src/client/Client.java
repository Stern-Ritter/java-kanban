package client;

import java.io.IOException;

public interface Client {

    void put(String key, String json) throws IOException, InterruptedException;

    String load(String key) throws IOException, InterruptedException;
}
