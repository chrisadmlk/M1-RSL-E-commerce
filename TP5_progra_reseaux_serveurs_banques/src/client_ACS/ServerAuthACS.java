package client_ACS;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAuthACS extends Thread {

    ServerSocket serverSocket = null;
    int portAUTH = 8087;

    public ServerAuthACS() {
        try {
            serverSocket = new ServerSocket(portAUTH);
        } catch (IOException e) {
            System.err.println("Erreur de port d'écoute AUTH [" + e + "]");
            System.exit(1);
        }
    }

    public void run() {
        Socket workSocket = null;
        while (!isInterrupted()) {
            try {
                System.out.println("************ Serveur AUTH en attente");
                workSocket = serverSocket.accept();
                System.out.println(workSocket.getRemoteSocketAddress().toString() + "#accept#thread serveur AUTH");
                ThreadConsoACS oneThreadConso = new ThreadConsoACS("NameTemp", workSocket, 2);
                oneThreadConso.start();
            } catch (IOException e) {
                System.err.println("Erreur d'accept ! ? [" + e.getMessage() + "]");
                System.exit(1);
            }
        }
    }
}