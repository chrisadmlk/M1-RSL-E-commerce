package store;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ThreadStoreForClient extends Thread {
    private Socket socket;
    private LinkedList<Socket> taskQueue;


    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;

    private static final String FILE_PROPERTIES = "dao_merchant.properties";
    private static final String URL             = "jdbc:mysql://127.0.0.1:3306/bd_merchant";

    private boolean running = true;

    public ThreadStoreForClient(LinkedList<Socket> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        System.out.println("*-> Lancement du ThreadClient n° : " + Thread.currentThread().getName());
        boolean logged = false;
        while (isRunning()) {
            synchronized (taskQueue) {
                if (!taskQueue.isEmpty()) {
                    socket = taskQueue.removeFirst();
                    // Buffers
                    try {
                        reader = new ObjectInputStream(socket.getInputStream());
                        writer = new ObjectOutputStream(socket.getOutputStream());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("*-> Prise en charge d'une connexion" +
                            "|| Thread : " + Thread.currentThread().getName());
                }
            }
            if (socket != null) {
                while (!socket.isClosed()) try {
                    String request = reader.readUTF();
                    System.out.println("*-> " + currentThread().getName() + " - Type de requete : " + request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void closeConnexion() throws IOException {
        writer = null;
        reader = null;
        System.out.println("*-> Client déconnecté, on ferme la socket..");
        socket.close();
    }

    public boolean isRunning() {
        return running;
    }
}
