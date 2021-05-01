package client_ACS;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMoneyACS extends Thread {

    ServerSocket serverSocket = null;
    int portMONEY = 8086;

    public ServerMoneyACS() {
        try {
            serverSocket = new ServerSocket(portMONEY);
        } catch (IOException e) {
            System.err.println("Erreur de port d'écoute MONEY [" + e + "]");
            System.exit(1);
        }
    }

    public void run() {
        Socket workSocket = null;
        while (!isInterrupted()) {
            try {
                System.out.println("************ Serveur MONEY en attente");
                workSocket = serverSocket.accept();
                System.out.println(workSocket.getRemoteSocketAddress().toString() + "#accept#thread serveur MONEY");
                ThreadConsoACS threadACS = new ThreadConsoACS("NameTemp", workSocket, 1);
                threadACS.start();
            } catch (IOException e) {
                System.err.println("Erreur d'accept ! ? [" + e.getMessage() + "]");
                System.exit(1);
            }
        }
    }
}
