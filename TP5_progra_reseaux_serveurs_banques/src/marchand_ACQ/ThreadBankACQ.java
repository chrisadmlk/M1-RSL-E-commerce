package marchand_ACQ;

import marchand_ACQ.obj.DebitRequest;
import mysecurity.certificate.CertificateHandler;
import mysecurity.encryption.AsymmetricCryptTool;
import mysecurity.utils.SSLHello;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ThreadBankACQ extends Thread {
    private Socket socket;
    private LinkedList<Socket> taskQueue;


    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;

    private AsymmetricCryptTool gothamBankCertificate;

    private boolean running = true;
    private final int portACSMoney = 51001;
    private final String hostACS = "localhost";

    public ThreadBankACQ(LinkedList<Socket> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        System.out.println("*-> Lancement du ThreadBank n° : " + Thread.currentThread().getName());

        gothamBankCertificate = new AsymmetricCryptTool();
        gothamBankCertificate.loadFromKeystore("ecom.keystore","pwdpwd","acscert");

        while (isRunning()) {
            synchronized (taskQueue) {
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

                    switch (request) {
                        case "REQPAY": {
                            // reçois une liste ? Ou direct le prix total
                            DebitRequest debitRequest = (DebitRequest) reader.readObject();

                            // Check signature
                            byte[] toVerify = debitRequest.getAuthServer().concatForSignature();
                            if(!gothamBankCertificate.verifyAuthentication(toVerify,debitRequest.getAuthServer().getSignature())){
                                System.out.println("Signature incorrecte");
                                break;
                            }
                            // Signature ok => Process with payment and contact ACS
                            sslDebitAsk(debitRequest);

                            break;
                        }

                        default: {
                            break;
                        }

                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void sslDebitAsk(DebitRequest debitRequest) throws IOException, ClassNotFoundException {
        // Act as a client towards ACS
        Socket paySocket = new Socket(hostACS,portACSMoney);
        System.out.println("#ACQ - Débit request# -> Client se connecte : " + paySocket.getInetAddress().toString());

        ObjectOutputStream payWriter = new ObjectOutputStream(paySocket.getOutputStream());
        ObjectInputStream payReader = new ObjectInputStream(paySocket.getInputStream());

        // Send MONEY request
        payWriter.writeUTF("MONEY"); payWriter.flush();

        // --- SSL Handshake
        // send client Hello
        SSLHello clientHello = new SSLHello(SSLHello.CLIENT);
        payWriter.writeObject(clientHello); payWriter.flush();

        // receive server hello and certificate
        SSLHello servHello = (SSLHello) payReader.readObject();
        CertificateHandler servCertif = (CertificateHandler) payReader.readObject();
        
//        if(certificate.checkSignature() && certificate.checkValidity()) return false;

    }


    private void closeConnexion() throws IOException {
        writer = null;
        reader = null;
        System.out.println("*-> Client déconnecté, on ferme la socket..");
        socket.close();
    }

    public AsymmetricCryptTool getCertificateBank(){
        return new AsymmetricCryptTool();
    }

    public boolean isRunning() {
        return running;
    }

}
