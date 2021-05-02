package test;

import client_ACS.ServerACS;
import client_ACS.obj.AuthServerResponse;
import marchand_ACQ.ServerBankACQ;
import marchand_ACQ.obj.DebitRequest;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class TestSSL {
    public static void main(String[] args) {
        ServerACS serverACS = new ServerACS();
        serverACS.startServer();
        ServerBankACQ serverBankACQ = new ServerBankACQ();
        serverBankACQ.startServer();



        // Client
        // Act as a client towards ACS

        SSLSocket paySocket = null;
        ObjectOutputStream payWriter = null;
        ObjectInputStream payReader = null;
        boolean isSuccessful = false;

        try {

            // Keystore
            KeyStore serverACSKeyStore = KeyStore.getInstance("JKS");
            String FILE_KEYSTORE = "serverAcq_keystore";
            char[] passwd = "pwdpwd".toCharArray();
            FileInputStream serverInput = new FileInputStream(FILE_KEYSTORE);
            serverACSKeyStore.load(serverInput, passwd);
            // Context
            SSLContext sslContext = SSLContext.getInstance("SSLv3");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(serverACSKeyStore, passwd);
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance("SunX509");
            trustFactory.init(serverACSKeyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);
            // Factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            // Socket
            paySocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 51001);

            payWriter = new ObjectOutputStream(paySocket.getOutputStream());
            payReader = new ObjectInputStream(paySocket.getInputStream());

            // Send MONEY request
            payWriter.writeUTF("MONEY");
            payWriter.flush();
            AuthServerResponse authServerResponse = new AuthServerResponse("Gotham","Bruce Wayne",41111);
            DebitRequest debitRequest = new DebitRequest(5000,authServerResponse);
            payWriter.writeObject(debitRequest);
            payWriter.flush();

            String response = payReader.readUTF();
            if (response.equals("SUCCESS")) {
                isSuccessful = true;
                System.out.println("SUCCESS");
            }
            // Close everything
            payWriter.close();
            payReader.close();
            paySocket.close();
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}