package client;


import common.Catalog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientCH extends JFrame{
    private JTextArea basketArea;
    private JButton connectButton;
    private JButton endTransactionButton;
    private JPanel clientGUI;
    private JComboBox quantity;
    private JComboBox comboBox2;
    private JButton ajouterButton;
    private JTextArea catalogArea;
    private JButton cancelButton;

    public ObjectOutputStream writer = null;
    public ObjectInputStream reader = null;

    public Socket clientSocket;
    private boolean alreadyLog = false;

    public ClientCH() {
        add(clientGUI);
        setTitle("Application Mouvements");
        setSize(1000, 800);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startClient("127.0.0.1",51000);
            }
        });
    }

    public void startClient(String host, int port) {
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
        writer.writeUTF("CATALOG"); writer.flush();
        Catalog catalog = (Catalog) reader.readObject();
        catalogArea.setText(catalog.toString());
    }

    private void stopClient() {

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
