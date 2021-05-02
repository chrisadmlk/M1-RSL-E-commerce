package client_ACS;

import client_ACS.obj.AuthServerResponse;
import common.BeanAccessOracle;
import marchand_ACQ.obj.DebitRequest;
import mysecurity.encryption.AsymmetricCryptTool;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ThreadACSMoney extends Thread {
    private SSLSocket socket;
    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;
    private BeanAccessOracle beanOracle;

    private AsymmetricCryptTool acsKeys;

    public ThreadACSMoney(SSLSocket workSocket, AsymmetricCryptTool acsKeys) {
        this.socket = workSocket;
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
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
                System.out.println("*MONEY* -> " + currentThread().getName() + " - Type de requete : " + request);
                if (request.equals("MONEY")) {
                    DebitRequest debitRequest = (DebitRequest) reader.readObject();
                    AuthServerResponse authServerResponse = debitRequest.getAuthServer();
                    // Verify if the server really authorized the transaction :
                    byte[] toVerify = authServerResponse.concatForSignature();
                    if (!acsKeys.verifyAuthentication(toVerify, authServerResponse.getSignature())) {
                        System.out.println("*MONEY* -> didn't authorize this request : " + debitRequest);
                        writer.writeUTF("CANCEL");
                        writer.flush();
                    } else {
                        // Requête à la DB
                        String clientName = debitRequest.getAuthServer().getClientName();
                        ResultSet resultSet = beanOracle.executeQuery("SELECT solde FROM ACS.Clients WHERE nom_client = " + clientName);
                        resultSet.next();
                        int solde = resultSet.getInt("solde");
                        beanOracle.executeQuery("UPDATE ACS.Clients SET solde = " + solde + "  WHERE nom_client = " + clientName);
                        // Débite le compte
                        writer.writeUTF("SUCCESS");
                        writer.flush();
                    }
                    // Close
                    socket.close();
                    writer.close();
                    reader.close();
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
