package marchand_ACQ.threadpool;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerThreadPool {
    ServerSocket getSocket();

    int getThreadPoolSize();

    void createThread();

    void addToTask(Socket socketToHandle);
}
