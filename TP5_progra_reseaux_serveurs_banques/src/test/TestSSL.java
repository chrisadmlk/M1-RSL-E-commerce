package test;

import client_ACS.ServerACS;
import client_ACS.obj.AuthServerResponse;
import marchand_ACQ.obj.DebitRequest;
import mysecurity.encryption.AsymmetricCryptTool;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class TestSSL {
    public static void main(String[] args) {
        ServerACS serverACS = new ServerACS();
        serverACS.startServer();

        System.out.println("----------------");

        // Client
        // Act as a client towards ACS

        SSLSocket paySocket = null;
        ObjectOutputStream payWriter = null;
        ObjectInputStream payReader = null;
        boolean isSuccessful = false;

        String FILE_KEYSTORE = "F:\\Workspace\\school\\M1-RSL-E-commerce\\TP5_progra_reseaux_serveurs_banques\\bruce";

        AsymmetricCryptTool crypt = new AsymmetricCryptTool();
        crypt.loadFromKeystore(FILE_KEYSTORE,"pwdpwd","bruce");

        try {
            // Keystore
            KeyStore serverACSKeyStore = KeyStore.getInstance("JKS");
            char[] passwd = "pwdpwd".toCharArray();
            FileInputStream serverInput = new FileInputStream(FILE_KEYSTORE);
            serverACSKeyStore.load(serverInput, passwd);
            System.out.println("--------KS--------");
            // Context
            SSLContext sslContext = SSLContext.getInstance("SSLv3");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            String alg = keyManagerFactory.getAlgorithm();
            System.out.println(alg);
            keyManagerFactory.init(serverACSKeyStore, passwd);
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(alg);
            trustFactory.init(serverACSKeyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);
            System.out.println("--------CONTEXT--------");

            // Factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            System.out.println("--------FACTORY--------");

            // Socket
            paySocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 51001);
            System.out.println("--------SOCKET--------");

            payWriter = new ObjectOutputStream(paySocket.getOutputStream());
            payReader = new ObjectInputStream(paySocket.getInputStream());
//            BufferedReader payReader = new BufferedReader (new InputStreamReader
//                    (paySocket.getInputStream()));
//            BufferedWriter payWriter = new BufferedWriter (new OutputStreamWriter
//                    (paySocket.getOutputStream()));


            // Send MONEY request
            payWriter.writeUTF("MONEY");
            payWriter.flush();
            AuthServerResponse authServerResponse = new AuthServerResponse("Gotham National Bank","Bruce Wayne",41111);
            authServerResponse.setSignature(crypt.authenticate(authServerResponse.concatForSignature()));
            DebitRequest debitRequest = new DebitRequest(100,authServerResponse);
            System.out.println(debitRequest);
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
