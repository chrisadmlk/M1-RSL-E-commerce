package marchand_ACQ;

import common.PropertiesLoader;
import marchand_ACQ.threadpool.ServerThreadPool;
import marchand_ACQ.threadpool.ThreadServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Properties;

public class ServerBankACQ implements ServerThreadPool {
    private static final String FILE_PROPERTIES         = "marchand_ACQ/ressources/acq_bank.properties";
    private static final String PROPERTY_PORT           = "port";
    private static final String PROPERTY_MAX_THREADS    = "MAX_THREADS";

    private LinkedList<Socket> taskQueue = null;
    // Default values
    private int MAX_THREADS = 5;
    private int port = 51003;
    private ServerSocket serverSocket = null;

    public ServerBankACQ() {
        try {
//            loadProperties();
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() throws IOException {
        Properties properties = PropertiesLoader.propertiesLoader(FILE_PROPERTIES);
        port = Integer.parseInt(properties.getProperty(PROPERTY_PORT));
        MAX_THREADS = Integer.parseInt(properties.getProperty(PROPERTY_MAX_THREADS));
    }

    public ServerBankACQ(int port) {
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer(){
        taskQueue = new LinkedList<>();
        ThreadServer threadServer = new ThreadServer(taskQueue,this);
        threadServer.start();
        System.out.println("## Bank ACQ ## -> Server Starting ");
    }

    public void close(){}

    @Override
    public ServerSocket getSocket() {
        return serverSocket;
    }

    @Override
    public int getThreadPoolSize() {
        return MAX_THREADS;
    }

    @Override
    public void createThread() {
        ThreadBankACQ threadBank = new ThreadBankACQ(taskQueue);
        threadBank.start();
    }

    @Override
    public synchronized void addToTask(Socket socketToHandle) {
        taskQueue.add(socketToHandle);
        System.out.println("## Bank ACQ ## -> TaskQueue : " + taskQueue);
    }


    public static void main(String[] args) {
        ServerBankACQ serverBankACQ = new ServerBankACQ();
        serverBankACQ.startServer();
    }
}
