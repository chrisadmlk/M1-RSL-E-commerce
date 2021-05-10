package client_ACS;

import client_ACS.obj.AuthServerResponse;
import common.BeanAccessOracle;
import marchand_ACQ.obj.DebitRequest;
import mysecurity.encryption.AsymmetricCryptTool;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ThreadACSMoney extends Thread {
    private SSLSocket socket;
    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;
//    BufferedReader reader = null;
//    BufferedWriter writer = null;

    private BeanAccessOracle beanOracle;

    private AsymmetricCryptTool acsKeys;

    public ThreadACSMoney(SSLSocket workSocket, AsymmetricCryptTool acsKeys) {
        this.socket = workSocket;
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());

//            reader = new BufferedReader (new InputStreamReader
//                    (socket.getInputStream()));
//            writer = new BufferedWriter (new OutputStreamWriter
//                    (socket.getOutputStream()));

            beanOracle = new BeanAccessOracle("ACS");
            this.acsKeys = acsKeys;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("*MONEY*-> Lancement ThreadMoney n° : " + Thread.currentThread().getName());

        if (socket != null) {
            while (!socket.isClosed()) try {
                String request = reader.readUTF();
//                String request = reader.readLine();
                System.out.println("*MONEY* -> " + currentThread().getName() + " - Type de requete : " + request);
                if (request.equals("MONEY")) {
                    DebitRequest debitRequest = (DebitRequest) reader.readObject();
//                    DebitRequest debitRequest = new DebitRequest(5000,new AuthServerResponse("test0","test1",1));
                    System.out.println(":::ACS:MONEY::: -> " + debitRequest.toString());

//                    DebitRequest debitRequest = new DebitRequest(1000000, new AuthServerResponse("test","test",100));
                    AuthServerResponse authServerResponse = debitRequest.getAuthServer();
                    // Verify if the server really authorized the transaction :
                    byte[] toVerify = authServerResponse.concatForSignature();

                    if (!acsKeys.verifyAuthentication(toVerify, authServerResponse.getSignature())) {
                        System.out.println("*MONEY* -> didn't authorize this request : " + debitRequest);
                        writer.writeUTF("CANCEL");
                    }
                    else {
                        // Requête à la DB
                        String clientName = debitRequest.getAuthServer().getClientName();
                        ResultSet resultSet = beanOracle.executeQuery("SELECT solde FROM ACS.Clients WHERE nom_client = '" + clientName+"'");
                        resultSet.next();
                        double solde = resultSet.getDouble("solde") - debitRequest.getDebit();
                        beanOracle.executeQuery("UPDATE ACS.Clients SET solde = " + solde + "  WHERE nom_client = '" + clientName+"'");
                        // Débite le compte
                        writer.writeUTF("SUCCESS");
                    }
                    writer.flush();
                    System.out.println("*MONEY* -> Terminer, fin du thread : " + currentThread().getName());
                    // Close
                    socket.close();
                    writer.close();
                    reader.close();
                }
            } catch (IOException | SQLException  e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) { e.printStackTrace(); }
        }
    }
}
