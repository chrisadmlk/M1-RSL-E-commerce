package test;

import client_ACS.ServerACS;
import client_ACS.obj.AuthClientRequest;
import client_ACS.obj.AuthServerResponse;
import marchand_ACQ.ServerBankACQ;
import marchand_ACQ.ServerStore;
import mysecurity.encryption.AsymmetricCryptTool;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;

public class TestAuthentication {
    public static void main(String[] args) {
        Thread server = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerACS serverACS = new ServerACS();
                serverACS.startServer();
            }
        });
        server.start();

        ServerStore serverStore = new ServerStore();
        serverStore.startServer();

        ServerBankACQ serverBankACQ = new ServerBankACQ();
        serverBankACQ.startServer();

        // Test with client :
        Thread client = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Début faux client");
                Socket clientSocket = null;
                try {
                    clientSocket = new Socket("localhost", 51002);

                    System.out.println("#-> Client se connecte : " + clientSocket.getInetAddress().toString());

                    ObjectOutputStream writer = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());

                    // Secure
                    writer.writeUTF("SECURE"); writer.flush();
                    AsymmetricCryptTool myKeys = new AsymmetricCryptTool();
                    myKeys.createKeyPair();
                    AsymmetricCryptTool serverKey = new AsymmetricCryptTool();
                    serverKey.setPublicKey((PublicKey) reader.readObject());
                    writer.writeObject(myKeys.getPublicKey());

                    System.out.println("Keys : " + serverKey.getPublicKey() + " - " + myKeys.getPublicKey());

                    // Authentication
                    writer.writeUTF("AUTH"); writer.flush();
                    AuthClientRequest request = new AuthClientRequest("Bruce Wayne","2222");
                    request.setSignature(myKeys.authenticate(request.gatherInfos()));
                    writer.writeObject(request); writer.flush();

                    System.out.println("Request : " + request.toString());

                    AuthServerResponse response = (AuthServerResponse) reader.readObject();
                    System.out.println("Auth réussie : " + response.toString());
                    System.out.println("TEST : " + reader.readUTF());

                } catch (IOException | ClassNotFoundException e ){
                    e.printStackTrace();
                }
            }
        });
        client.start();

    }
}
