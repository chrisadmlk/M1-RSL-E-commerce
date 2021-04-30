package client_ACS;

import common.BeanAccessOracle;
import common.ObjTransformer;
import mysecurity.tramap.AsymmetricCryptTool;
import mysecurity.utils.TransferObject;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;

public class ThreadACSAuth extends Thread {
    private Socket socket;
    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;
    private BeanAccessOracle beanOracle;

    private AsymmetricCryptTool clientKeys;
    private AsymmetricCryptTool serverACQKeyHandler;

    public ThreadACSAuth(Socket workSocket) {
        this.socket = workSocket;
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
            beanOracle = new BeanAccessOracle("ACS");
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
                        // Simulate certificates
                        clientKeys = new AsymmetricCryptTool();
                        clientKeys.createKeyPair();
                        // Sending public key
                        writer.writeObject(clientKeys.getPublicKey());
                        writer.flush();
                        // Receive ACQ public key
                        serverACQKeyHandler = new AsymmetricCryptTool();
                        serverACQKeyHandler.setPublicKey((PublicKey) reader.readObject());
                        break;
                    }
                    case "AUTH": {
                        // Authenticate client
                        AuthClientRequest authClientRequest = (AuthClientRequest) reader.readObject();

                        // Requires testing for hash
                        authClientRequest.createDigest("Call database for pin..");

                        // Verify signature + digest
                        String stringSignature = authClientRequest.getName() + authClientRequest.getDateRequest();
                        byte[] verifySignature = stringSignature.getBytes() + authClientRequest.getDigest();
                        if(!serverACQKeyHandler.verifyAuthentication()){

                        }

                        // Send response
                        String bankName = "Gotham National Bank";
                        int serial = (int) Math.random();
                        String concat = bankName + authClientRequest.getName() + serial;

                        AuthServerResponse authServerResponse = new AuthServerResponse(
                                bankName,
                                authClientRequest.getName(),
                                serial,
                                clientKeys.authenticate(concat.getBytes())
                        );
                        writer.writeObject(authServerResponse); writer.flush();
                        break;
                    }
                    default: {
                        System.out.println("loop");
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
