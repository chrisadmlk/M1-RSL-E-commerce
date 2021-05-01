package marchand_ACQ;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerACQ extends Thread {

    private int PORT_REQPAY = 8085;
    int NB_MAX_THEADCONSO = 3;
    private ServerSocket SSocket = null;
    private I_SourceTaches tachesAExecuter;


    public ServerACQ(I_SourceTaches st) {
        tachesAExecuter = st;
        // Démarrage du pool de threads
        for (int i = 0; i < NB_MAX_THEADCONSO; i++) {
            ThreadACQ thr = new ThreadACQ(tachesAExecuter, "Thread du pool n°" + String.valueOf(i));
            thr.start();
        }
    }

    public void run() {
        // Mise en attente du serveur
        Socket CSocket = null;

        while (!isInterrupted()) {
            try {
                System.out.println("************ Serveur en attente");
                CSocket = SSocket.accept();
                System.out.println(CSocket.getRemoteSocketAddress().toString() + "#accept#thread serveur");
            } catch (IOException e) {
                System.err.println("Erreur d'accept ! ? [" + e.getMessage() + "]");
                System.exit(1);
            }

            DataInputStream dis = null;
            StringBuffer message = new StringBuffer();
            RequeteACQ req = null;
            boolean fini = false;
            byte b = 0;
            int cpt = 0;


            Runnable travail = null;
            try {
                dis = new DataInputStream(new BufferedInputStream(CSocket.getInputStream()));
                message.setLength(0);
                while ((b = dis.readByte()) != (byte) '\n') {
                    if (b != '\n') message.append((char) b);
                }
                String mess = message.toString().trim();
                System.out.println("Message recu dans le ThreadServeur Banque ACQ (marchand) : " + mess);


                // ECHANGE DE CLES INITIAL

//		            if ( mess.equals("+") ) {
//		            	try {
//		    				DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(CSocket.getOutputStream())); dos.flush();
//		    				dos.write(StringCryptedKIR.getBytes()); dos.flush();
//		    				dos.write(StringCryptedKLO.getBytes()); dos.flush();
//		    				System.out.println(StringCryptedKIR );
//		    				System.out.println(StringCryptedKLO);
//		    			} catch (IOException e) {e.printStackTrace(); }
//		            	knowedClients.add(CSocket.getRemoteSocketAddress().toString());
//		            }

                // else {


                int protocoleTag = Integer.parseInt(message.substring(0, 1));
                int choix = Integer.parseInt(message.substring(2, 3));
                mess = mess.substring(4);

                req = new RequeteACQ(protocoleTag, choix, mess, CSocket);

                travail = req.createRunnable(CSocket);
                // }
            } catch (IOException e) {
                System.err.println("Erreur ? [" + e.getMessage() + "]");
            }

            if (travail != null) {
                tachesAExecuter.recordTache(travail);
                System.out.println("Bien recu : Travail mis dans la file (MyThreadServeur)");
            } else System.out.println("Pas de mise en file");

        }

    }


}
