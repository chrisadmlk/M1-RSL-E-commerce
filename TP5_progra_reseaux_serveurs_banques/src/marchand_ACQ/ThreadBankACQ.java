package marchand_ACQ;

import marchand_ACQ.obj.DebitRequest;
import mysecurity.encryption.AsymmetricCryptTool;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.LinkedList;

public class ThreadBankACQ extends Thread {
    private Socket socket;
    private LinkedList<Socket> taskQueue;

    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;

    private AsymmetricCryptTool bankKeys;

    private boolean running = true;
    private final int portACSMoney = 51001;
    private final String hostACS = "localhost";

    public ThreadBankACQ(LinkedList<Socket> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        System.out.println("*-> Lancement du ThreadBank n° : " + Thread.currentThread().getName());


        while (isRunning()) {
            synchronized(taskQueue) {
                if (!taskQueue.isEmpty()) {
                    socket = taskQueue.removeFirst();
                    // Buffers
                    try {
                        reader = new ObjectInputStream(socket.getInputStream());
                        writer = new ObjectOutputStream(socket.getOutputStream());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("*-> Prise en charge d'une connexion" +
                            "|| Thread : " + Thread.currentThread().getName());
                }
            }
            if (socket != null) {
                while (!socket.isClosed()) try {
                    String request = reader.readUTF();
                    System.out.println("*-> " + currentThread().getName() + " - Type de requete : " + request);

                    if ("REQPAY".equals(request)) {// receive debit request
                        DebitRequest debitRequest = (DebitRequest) reader.readObject();
                        // Signature ok => Process with payment and contact ACS
                        if (sslDebitAsk(debitRequest)) {
                            writer.writeUTF("OK");
                        } else {
                            writer.writeUTF("CANCEL");
                        }
                        writer.flush();
                    }
                } catch (IOException
                        | ClassNotFoundException
                        | NoSuchAlgorithmException
                        | NoSuchProviderException
                        | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean sslDebitAsk(DebitRequest debitRequest) throws IOException, ClassNotFoundException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
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
            paySocket = (SSLSocket) sslSocketFactory.createSocket(hostACS, portACSMoney);

            payWriter = new ObjectOutputStream(paySocket.getOutputStream());
            payReader = new ObjectInputStream(paySocket.getInputStream());

            // Send MONEY request
            payWriter.writeUTF("MONEY");
            payWriter.flush();
            payWriter.writeObject(debitRequest);
            payWriter.flush();

            String response = payReader.readUTF();
            if (response.equals("SUCCESS")) {
                isSuccessful = true;
            }
            // Close everything
            payWriter.close();
            payReader.close();
            paySocket.close();
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }
        return isSuccessful;
    }

    private void closeConnexion() throws IOException {
        writer = null;
        reader = null;
        System.out.println("*-> Client déconnecté, on ferme la socket..");
        socket.close();
    }

    public boolean isRunning() {
        return running;
    }

}
