package client_ACS;

import client_ACS.obj.AuthClientRequest;
import client_ACS.obj.AuthServerResponse;
import common.BeanAccessOracle;
import common.ObjTransformer;
import mysecurity.encryption.AsymmetricCryptTool;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.sql.SQLException;

public class ThreadACSAuth extends Thread {
    private Socket socket;
    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;
    private BeanAccessOracle beanOracle;

    private AsymmetricCryptTool serverACSkeys;
    private AsymmetricCryptTool clientKeys;

    public ThreadACSAuth(Socket workSocket, AsymmetricCryptTool serverKeys) {
        this.socket = workSocket;
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
            beanOracle = new BeanAccessOracle("ACS");
            serverACSkeys = serverKeys;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("*AUTH*-> Lancement ThreadAuth n° : " + Thread.currentThread().getName());

        if (socket != null) {
            while (!socket.isClosed()) try {
                String request = reader.readUTF();
                System.out.println("*AUTH* -> " + currentThread().getName() + " - Type de requete : " + request);
                switch (request) {
                    case "SECURE": {
                        // Sending public key
                        writer.writeObject(serverACSkeys.getPublicKey());
                        writer.flush();
                        // Receive client public key
                        clientKeys = new AsymmetricCryptTool();
                        clientKeys.setPublicKey((PublicKey) reader.readObject());
                        break;
                    }
                    case "AUTH": {
                        // Authenticate client
                        AuthClientRequest authClientRequest = (AuthClientRequest) reader.readObject();
                        String name = authClientRequest.getName();
                        String dateRequest = authClientRequest.getDateRequest();

                        // Verify signature
                        AuthClientRequest copyToVerify = new AuthClientRequest();
                        copyToVerify.setName(authClientRequest.getName());
                        copyToVerify.setDateRequest(authClientRequest.getDateRequest());
                        copyToVerify.setDigest(authClientRequest.getDigest());
                        if (!clientKeys.verifyAuthentication(copyToVerify.gatherInfos(), authClientRequest.getSignature())) {
                            System.out.println("Signature incorrecte");
                            writer.writeUTF("CANCEL");
                            writer.flush();
                            break;
                        }

                        // Verify digest with PIN
                        String pin = beanOracle.getPIN(name);
                        if (!authClientRequest.getDigest().verifyHash(ObjTransformer.ObjToByteArray(name + dateRequest + pin))) {
                            System.out.println("Hash + pin incorrect -- Authentification impossible");
                            writer.writeUTF("CANCEL");
                            writer.flush();
                            break;
                        }

                        // Send response if digest is OK
                        String bankName = "Gotham National Bank";
                        int serial = (int) (Math.random() * 420) + 5;
                        String concat = bankName + authClientRequest.getName() + serial;

                        AuthServerResponse authServerResponse = new AuthServerResponse(
                                bankName,
                                authClientRequest.getName(),
                                serial,
                                serverACSkeys.authenticate(concat.getBytes())
                        );

                        writer.writeObject(authServerResponse);
                        writer.flush();
                        writer.writeUTF("OK");
                        writer.flush();
                        break;
                    }

                    default: {
                        System.out.println("loop");
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
