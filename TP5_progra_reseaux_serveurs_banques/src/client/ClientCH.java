package client;


import client_ACS.obj.AuthClientRequest;
import client_ACS.obj.AuthServerResponse;
import common.Catalog;
import common.ItemStore;
import mysecurity.encryption.AsymmetricCryptTool;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class ClientCH extends JFrame {
    private JTextArea basketArea;
    private JButton connectButton;
    private JButton endTransactionButton;
    private JPanel clientGUI;
    private JComboBox quantityBox;
    private JComboBox itemCombo;
    private JButton ajouterButton;
    private JTextArea catalogArea;
    private JButton cancelButton;

    public ObjectOutputStream writer = null;
    public ObjectInputStream reader = null;

    private List<ItemStore> list;
    private Catalog wantedList;

    public Socket clientSocket;
    private boolean alreadyLog = false;
    private int portAuth = 51002;
    private String hostAuth = "localhost";

    public ClientCH() {
        add(clientGUI);
        setTitle("Application Mouvements");
        setSize(1000, 800);

        list = new ArrayList<>();
        wantedList = new Catalog();

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    startClient("127.0.0.1", 51000);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    stopClient();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        ajouterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = "";
                int index = itemCombo.getSelectedIndex();
                ItemStore item = list.get(index);
                item.setQuantity(quantityBox.getSelectedIndex() + 1);
                double price = item.getPrice() * item.getQuantity();
                s = item.getName() + " - " + item.getQuantity() + " - || Prix : " + price;
                basketArea.setText(basketArea.getText() + "\n" + s);
                wantedList.getItems().add(item);
            }
        });
        endTransactionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendWantedCatalog();
                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    private void sendWantedCatalog() throws IOException, ClassNotFoundException {
        AuthServerResponse servResponse = authenticateProcess();
        writer.writeUTF("PAY");
        writer.flush();
        // Send client's choices (Catalog wanted)
        writer.writeObject(wantedList);
        writer.flush();
        if (servResponse != null) {
            writer.writeObject(servResponse);
            writer.flush();
        } else {
            catalogArea.setText(catalogArea.getText() + "\n" + "Echec de l'achat.." + "\n");
        }

        String finalResponse = reader.readUTF();
        if(finalResponse.equals("OK")){
            catalogArea.setText(catalogArea.getText() + "\n" + "Achat validé ! Merci pour le pognon !" + "\n");
        }
        else{
            catalogArea.setText(catalogArea.getText() + "\n" + "Echec de l'achat.." + "\n");
        }
    }

    private AuthServerResponse authenticateProcess() throws IOException, ClassNotFoundException {
        System.out.println("Client ---> S'authentifie");
        Socket authSocket = new Socket(hostAuth, portAuth);
        ObjectInputStream authReader = new ObjectInputStream(authSocket.getInputStream());
        ObjectOutputStream authWriter = new ObjectOutputStream(authSocket.getOutputStream());

        AsymmetricCryptTool myKeys = new AsymmetricCryptTool();
        AsymmetricCryptTool serverKey = new AsymmetricCryptTool();

        System.out.println("Client ---> Se connecte à ACS");

        // Crée une paire de clé pour le client
        myKeys.createKeyPair();
        // Secure connexion - PK trade
        authWriter.writeUTF("SECURE");
        authWriter.flush();
        serverKey.setPublicKey((PublicKey) authReader.readObject());
        authWriter.writeObject(myKeys.getPublicKey());

        // Authenticate
        authWriter.writeUTF("AUTH");
        authWriter.flush();
        AuthClientRequest clientRequest = new AuthClientRequest("Bruce Wayne", "2222");
        byte[] signature = myKeys.authenticate(clientRequest.gatherInfos());
        clientRequest.setSignature(signature);
        authWriter.writeObject(clientRequest);
        authWriter.flush();

        String success = authReader.readUTF();
        if (success.equals("OK")) {
            return (AuthServerResponse) reader.readObject();
        } else {
            return null;
        }
    }

    public void startClient(String host, int port) throws IOException {
        if (alreadyLog) stopClient();
        clientSocket = null;
        try {
            clientSocket = new Socket(host, port);

            System.out.println("#-> Client se connecte : " + clientSocket.getInetAddress().toString());

            writer = new ObjectOutputStream(clientSocket.getOutputStream());
            reader = new ObjectInputStream(clientSocket.getInputStream());

            if (clientSocket == null || writer == null) {
                System.out.println("Il y a eu une erreur lors de la connexion");
            }
            alreadyLog = true;
            receiveCatalog();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void receiveCatalog() throws IOException, ClassNotFoundException {
        writer.writeUTF("CATALOG");
        writer.flush();
        Catalog catalog = (Catalog) reader.readObject();
        catalogArea.setText(catalog.toString());
        list = catalog.getItems();
        for (ItemStore itemStore : list) {
            itemCombo.addItem(itemStore.getName());
        }

    }

    private void stopClient() throws IOException {
        // Close the client
        writer.writeUTF("END");
        writer.flush();
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        System.out.println("Démarrage de l'application client");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ClientCH clientCH = new ClientCH();
                clientCH.setLocationRelativeTo(null);
                clientCH.setVisible(true);
            }
        });
    }
}
