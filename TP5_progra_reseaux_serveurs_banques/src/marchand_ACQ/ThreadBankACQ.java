package marchand_ACQ;

import marchand_ACQ.obj.DebitRequest;
import mysecurity.certificate.CertificateHandler;
import mysecurity.encryption.AlgorithmParam;
import mysecurity.encryption.AsymmetricCryptTool;
import mysecurity.utils.HashedObject;
import mysecurity.utils.SSLHello;
import mysecurity.utils.TransferObject;

import javax.crypto.KeyGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

        bankKeys = new AsymmetricCryptTool();
        bankKeys.loadFromKeystore("ecom.keystore","pwdpwd","picsoukeys");

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
                            // receive debit request
                            DebitRequest debitRequest = (DebitRequest) reader.readObject();
                            // Signature ok => Process with payment and contact ACS
                            if(sslDebitAsk(debitRequest)){
                                writer.writeUTF("OK");
                            }
                            else{
                                writer.writeUTF("NOT OK");
                            }
                            writer.flush();

                            break;
                        }

                        default: {
                            break;
                        }

                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean sslDebitAsk(DebitRequest debitRequest) throws IOException, ClassNotFoundException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
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

        if(servCertif.checkSignature() && servCertif.checkValidity()) {
            System.out.println("Certificat invalide !!");
        }

        AsymmetricCryptTool serverCrypt = new AsymmetricCryptTool();
        serverCrypt.setPublicKey(serverCrypt.getPublicKey());

        // Check signature of DebitRequest
        byte[] toVerify = debitRequest.getAuthServer().concatForSignature();
        if(!serverCrypt.verifyAuthentication(toVerify,debitRequest.getAuthServer().getSignature())){
            System.out.println("Signature incorrecte -- Client avec mauvaise banque ?");
        }

        // Classic SSL : Generate pre master and encrypt it with Server Pk
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(clientHello.getRnd_cli());
        output.write(servHello.getRnd_serv());
        output.write(servHello.getSessionId());
        byte[] toHash = output.toByteArray();
        HashedObject hashed = new HashedObject(toHash, "SHA-256");
        TransferObject preMaster = new TransferObject(serverCrypt.encrypt(hashed.getBytes()));
        payWriter.writeObject(preMaster); payWriter.flush();

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES","BC");
        keyGenerator.init(new AlgorithmParam(hashed.getBytes()));
        keyGenerator.generateKey();

        return true;
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
