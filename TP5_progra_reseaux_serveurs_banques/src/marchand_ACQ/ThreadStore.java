package marchand_ACQ;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ThreadStore extends Thread {
    private Socket socket;
    private LinkedList<Socket> taskQueue;


    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;

    private boolean running = true;

    public ThreadStore(LinkedList<Socket> taskQueue) {
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

                    switch (request) {
                        case "PAY": {
                            // reçois une liste ? Ou direct le prix total
                            reader.readObject();

                            


                            break;
                        }
                        default: {
                            break;
                        }

                    }
                } catch (IOException | ClassNotFoundException e) {
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
