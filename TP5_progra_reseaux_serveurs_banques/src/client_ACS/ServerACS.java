package client_ACS;

import common.PropertiesLoader;
import mysecurity.tramap.AsymmetricCryptTool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class ServerACS {
    private static final String FILE_PROPERTIES = "src/client_ACS/serverACS.properties";
    private static final String PROPERTY_PORT_MONEY = "PORT_MONEY";
    private static final String PROPERTY_PORT_AUTH = "PORT_AUTH";

    // Default values
    private int portMoney = 51001;
    private int portAuth = 51002;

    private ServerSocket serverAuthSocket = null;
    private ServerSocket serverMoneySocket = null;
    private boolean isRunning = true;

    public ServerACS() {
        try {
//            loadProperties();
            serverMoneySocket = new ServerSocket(portMoney);
            serverAuthSocket = new ServerSocket(portAuth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        System.out.println("Démarrage ACS");
        Thread thSocketHandlerMoney = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Socket workMoneySocket = serverMoneySocket.accept();
                        ThreadACSMoney thMoney = new ThreadACSMoney(workMoneySocket);
                        thMoney.start();
                        System.out.println("*-> Connexion d'un client reçu sur le port Money\n*-> En attente du login");
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
                        Socket workAuthSocket = serverAuthSocket.accept();
                        AsymmetricCryptTool serverACQKeys = new AsymmetricCryptTool();
                        serverACQKeys.createKeyPair();
                        ThreadACSAuth thAuth = new ThreadACSAuth(workAuthSocket,serverACQKeys);
                        thAuth.start();
                        System.out.println("*-> Connexion d'un client reçu sur le port Auth\n*-> En attente du login");
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
        ServerACS serverMovements = new ServerACS();
        serverMovements.startServer();
    }
}
