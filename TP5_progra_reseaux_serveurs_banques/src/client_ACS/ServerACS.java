package client_ACS;

import common.PropertiesLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class ServerACS {
    private static final String FILE_PROPERTIES = "serverACS.properties";
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
            loadProperties();
            serverMoneySocket = new ServerSocket(portMoney);
            serverAuthSocket = new ServerSocket(portAuth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    Thread thSocketHandlerMoney = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket workMoneySocket = serverMoneySocket.accept();
                                new Thread(new ThreadACSMoney(workMoneySocket)).start();
                                System.out.println("*-> Connexion d'un client reçu sur le port Money\n*-> En attente du login");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Thread thSocketHandlerAuth = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket workAuthSocket = serverAuthSocket.accept();
                                new Thread(new ThreadACSAuth(workAuthSocket)).start();
                                System.out.println("*-> Connexion d'un client reçu sur le port Auth\n*-> En attente du login");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thSocketHandlerAuth.start();
                    thSocketHandlerMoney.start();
                }
                // Server stopped
                try {
                    serverMoneySocket.close();
                    serverAuthSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    serverAuthSocket = null;
                    serverMoneySocket = null;
                }
            }
        });
        thread.start();
    }

    private void loadProperties() throws IOException {
        Properties properties = PropertiesLoader.propertyloader(FILE_PROPERTIES);
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
