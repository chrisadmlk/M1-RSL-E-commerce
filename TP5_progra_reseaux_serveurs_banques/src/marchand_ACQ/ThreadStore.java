package marchand_ACQ;


import client_ACS.obj.AuthServerResponse;
import common.BeanAccessOracle;
import common.Catalog;
import common.ItemStore;
import marchand_ACQ.obj.DebitRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class ThreadStore extends Thread {
    private Socket socket;
    private LinkedList<Socket> taskQueue;
    private BeanAccessOracle beanOracle;


    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;

    private boolean running = true;
    private final int portACQ = 51003;
    private final String hostACQ = "localhost";

    public ThreadStore(LinkedList<Socket> taskQueue) throws Exception {
        this.taskQueue = taskQueue;
        beanOracle = new BeanAccessOracle("MERCHANT");
    }

    @Override
    public void run() {
        System.out.println("*-> Lancement du ThreadClient n° : " + Thread.currentThread().getName());
        boolean logged = false;
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
                        case "CATALOG": {
                            sendCatalog();
                        }
                        case "PAY": {
                            executePayment();
                            break;
                        }
                        case "END" : {
                            running = false;
                            closeConnexion();
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    private void sendCatalog() throws SQLException, IOException {
        // Envoi le catalogue au client
        ResultSet resultSet = beanOracle.executeQuery("SELECT * FROM MERCHANT.Stock");
        Catalog catalog = new Catalog();
        int index = 1;
        while (resultSet.next()) {
            ItemStore item = new ItemStore(
                    index,
                    resultSet.getString("item_name"),
                    resultSet.getInt("quantity"),
                    resultSet.getDouble("price")
            );
            catalog.getItems().add(item);
        }
        writer.writeObject(catalog);
        writer.flush();
    }

    private void executePayment() throws IOException, ClassNotFoundException, SQLException {
        // reçois un catalogue choisi par le client (un panier en gros)
        Catalog cltCatalog = (Catalog) reader.readObject();
        double price = getPrice(cltCatalog);
        AuthServerResponse authentication = (AuthServerResponse) reader.readObject();
        DebitRequest debitRequest = new DebitRequest(price, authentication);
        // Client with ACQ
        Socket paySocket = new Socket(hostACQ, portACQ);
        ObjectOutputStream payWriter = new ObjectOutputStream(paySocket.getOutputStream());
        ObjectInputStream payReader = new ObjectInputStream(paySocket.getInputStream());
        // send reqpay
        payWriter.writeUTF("REQPAY");
        payWriter.flush();
        payWriter.writeObject(debitRequest);
        payWriter.flush();

        // Get response from transaction
        String response = payReader.readUTF();
        if(response.equals("OK")){
            System.out.println("---Server store : Payement effectué ! --");
            writer.writeUTF("OK");
            updateQuantitiesInDB(cltCatalog);
        }
        else {
            System.out.println("---Server store : Payement échoué ! --");
            writer.writeUTF("CANCEL");
        }
        writer.flush();
        // Close
        paySocket.close();
        payWriter.close();
        payReader.close();
    }

    private void updateQuantitiesInDB(Catalog cltCatalog) throws SQLException {
        for (int i = 0; i < cltCatalog.getItems().size(); i++) {
            ItemStore tmp = cltCatalog.getItems().get(i);
            ResultSet resultSet = beanOracle.executeQuery("SELECT quantity FROM MERCHANT.Stock WHERE item_name = " + tmp.getName());
            resultSet.next();
            int quantity = resultSet.getInt("quantity") - tmp.getQuantity();
            beanOracle.executeQuery("UPDATE MERCHANT.Stock SET quantity = " + quantity);
        }
    }

    private double getPrice(Catalog catalog) {
        double price = 0;
        for (int i = 0; i < catalog.getItems().size(); i++) {
            ItemStore tmp = catalog.getItems().get(i);
            double tmpPrice = tmp.getQuantity() * tmp.getPrice();
            price += tmpPrice;
        }
        return price;
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
