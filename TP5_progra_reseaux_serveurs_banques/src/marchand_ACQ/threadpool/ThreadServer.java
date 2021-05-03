package marchand_ACQ.threadpool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ThreadServer extends Thread {
    private LinkedList<Socket> tasks;
    private ServerSocket serverSocket = null;
    private ServerThreadPool master;

    public ThreadServer(LinkedList<Socket> tasks, ServerThreadPool master) {
        this.tasks = tasks;
        this.master = master;
    }

    @Override
    public void run() {
        serverSocket = master.getSocket();
        System.out.println(master.getClass().getName() +" ||  Serveur Thread starting\n");
        // Thread Pool
        for (int i = 0; i < master.getThreadPoolSize(); i++) {
            master.createThread();
        }
        while (!isInterrupted()) {
            try {
                Socket workSocket = serverSocket.accept();
                System.out.println(master.getClass().getName() +"*-> Connexion d'un client reçu\n*-> Passage à un thread\n");
                master.addToTask(workSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
