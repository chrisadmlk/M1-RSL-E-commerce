package client_ACS;

import mysecurity.encryption.AsymmetricCryptTool;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;

public class ServerACS {
    private static final String FILE_PROPERTIES = "src/client_ACS/serverACS.properties";
    private static final String PROPERTY_PORT_MONEY = "PORT_MONEY";
    private static final String PROPERTY_PORT_AUTH = "PORT_AUTH";

    private static final String FILE_KEYSTORE = "F:\\Workspace\\school\\M1-RSL-E-commerce\\TP5_progra_reseaux_serveurs_banques\\bruce";


    // Default values
    private int portMoney = 51001;
    private int portAuth = 51002;

    private ServerSocket serverAuthSocket = null;
    private SSLServerSocket serverMoneySocket = null;

    private boolean isRunning = true;

    public ServerACS() {
        try {
//            loadProperties();
            // Simple Socket for Auth
            serverAuthSocket = new ServerSocket(portAuth);

            // SSL for Money
            // Keystore
            KeyStore serverACSKeyStore = KeyStore.getInstance("JKS");
            char[] passwd = "pwdpwd".toCharArray();
            FileInputStream serverInput = new FileInputStream(FILE_KEYSTORE);
            serverACSKeyStore.load(serverInput,passwd);
            // Context
            SSLContext sslContext = SSLContext.getInstance("SSLv3");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(serverACSKeyStore,passwd);
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance("SunX509");
            trustFactory.init(serverACSKeyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);
            // Factory
            SSLServerSocketFactory sslSocketFactory = sslContext.getServerSocketFactory();
            // Socket
            serverMoneySocket = (SSLServerSocket) sslSocketFactory.createServerSocket(portMoney);
        } catch (IOException
                | KeyStoreException
                | CertificateException
                | UnrecoverableKeyException
                | NoSuchAlgorithmException
                | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        System.out.println("%% ACS %% --- Démarrage ACS -> SockAuth" + serverAuthSocket + " \n%% ACS %% -> SockWork" + serverMoneySocket);

        AsymmetricCryptTool serverACSkeys = new AsymmetricCryptTool();
        serverACSkeys.loadFromKeystore(FILE_KEYSTORE,"pwdpwd","bruce");

        Thread thSocketHandlerMoney = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        SSLSocket workMoneySocket = (SSLSocket) serverMoneySocket.accept();
                        ThreadACSMoney thMoney = new ThreadACSMoney(workMoneySocket,serverACSkeys);
                        thMoney.start();
                        System.out.println("%% MONEY %% -> Connexion d'un client reçu sur le port Money\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Server stopped
                try {
                    serverMoneySocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    serverMoneySocket = null;
                }
            }
        });
        Thread thSocketHandlerAuth = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        System.out.println("%% AUTH %% -> accept... " + serverAuthSocket.getInetAddress() + ":" + serverAuthSocket.getLocalPort() +  "\n");
                        Socket workAuthSocket = serverAuthSocket.accept();
                        ThreadACSAuth thAuth = new ThreadACSAuth(workAuthSocket,serverACSkeys);
                        thAuth.start();
                        System.out.println("%% AUTH %% -> Connexion d'un client reçu sur le port Auth\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Server stopped
                try {
                    serverAuthSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    serverAuthSocket = null;
                }
            }
        });
        thSocketHandlerAuth.start();
        thSocketHandlerMoney.start();
        System.out.println("%% ACS %% --- Finished Starting");
    }


    private void loadProperties() throws IOException {
        Properties properties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream fileProperties = classLoader.getResourceAsStream(FILE_PROPERTIES);
        if (fileProperties == null) {
            throw new FileNotFoundException("Erreur ouverture du fichier properties");
        }
        properties.load(fileProperties);
        portMoney = Integer.parseInt(properties.getProperty(PROPERTY_PORT_MONEY));
        portAuth = Integer.parseInt(properties.getProperty(PROPERTY_PORT_AUTH));
    }

    public void close() {
        isRunning = false;
    }


    public static void main(String[] args) {
        ServerACS serverACS = new ServerACS();
        serverACS.startServer();
    }
}
