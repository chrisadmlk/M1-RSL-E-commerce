package client_ACS;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;

import common.BeanAccessOracle;

public class ThreadACS extends Thread {
    String nom;
    Socket socket;
    DataInputStream dis = null;
    DataOutputStream dos = null;
    BeanAccessOracle beanOracle;
    int threadServeurAppelant;

    public ThreadACS(String n, Socket s, int cas) {
        socket = s;
        nom = n;
        threadServeurAppelant = cas;  // 1 = MONEY, 2 = AUTH
        try {
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            beanOracle = new BeanAccessOracle("ACS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO

    public String queryAndInterpetation(int choix, String s) {

        String rep = "";
        String query = "";
        ResultSet result;

        String[] token = s.split(";");

//     	if (choix == 1) {
//     		query="SELECT * FROM Login WHERE Login='"+token[0]+"' AND Mdp='"+token[1]+"';";
//     		try { 
//     			result = beanOracle.executeQuery(query);
//					if (result.next()) rep="1;OK";
//					else rep = "0;Erreur";
//				} catch (SQLException e) {  e.printStackTrace(); }
//     	}
        return rep;
    }

    public void run() {

        StringBuffer message = new StringBuffer();
        boolean fini = false;
        byte b = 0;
        int cpt = 0;
        String reponse;

        while (!isInterrupted()) {
            message.setLength(0);
            try {
                while ((b = dis.readByte()) != (byte) '\n') {
                    if (b != '\n') message.append((char) b);
                }
            } catch (IOException e) {
                System.out.println("Erreur de lecture = " + e.getMessage());
            }

            String mess = message.toString().trim();
            int choix = Integer.parseInt(message.substring(0, 1));
            mess = mess.substring(2);

            reponse = queryAndInterpetation(choix, mess);

            try {
                if (!fini) dos.write(reponse.getBytes());
                dos.flush();
            } catch (IOException e) {
                System.out.println("Erreur de lecture = " + e.getMessage());
            }

        }

        // TODO : mettre en place protocole clair d'arret d'un thread

        try {
            dis.close();
            dos.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
        System.out.println("Thread + " + nom + " Stopped.");
    }


}
