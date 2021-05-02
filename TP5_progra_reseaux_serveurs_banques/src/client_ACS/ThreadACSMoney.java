package client_ACS;

import common.BeanAccessOracle;
import mysecurity.certificate.CertificateHandler;
import mysecurity.encryption.AsymmetricCryptTool;
import mysecurity.utils.SSLHello;
import mysecurity.utils.TransferObject;

import javax.crypto.KeyGenerator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class ThreadACSMoney extends Thread{
    private Socket socket;
    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;
    private BeanAccessOracle beanOracle;

    private AsymmetricCryptTool acsKeys;

    public ThreadACSMoney(Socket workSocket, AsymmetricCryptTool acsKeys) {
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

        if(socket != null) {
            while(!socket.isClosed()) try{
                String request = reader.readUTF();
                System.out.println("*MONEY* -> " + currentThread().getName() + " - Type de requete : " + request);
                if(request.equals("MONEY")){
                    // --- SSL Handshake
                    // receive client Hello
                    SSLHello clientHello = (SSLHello) reader.readObject();

                    // send server hello
                    SSLHello servHello = new SSLHello(SSLHello.SERVER);
                    writer.writeObject(servHello); writer.flush();

                    // Send certificate
                    CertificateHandler certificate = new CertificateHandler(acsKeys.getCertificate());
                    writer.writeObject(certificate); writer.flush();

                    // Receive premaster
                    TransferObject preMaster = (TransferObject) reader.readObject();

                    KeyGenerator keyGenerator = KeyGenerator.getInstance("DES","BC");
                    keyGenerator.init();
                    keyGenerator.generateKey();

                }

            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
    }
}
