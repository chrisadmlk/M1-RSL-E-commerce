package test;

import client.ClientCH;
import client_ACS.ServerACS;
import marchand_ACQ.ServerBankACQ;
import marchand_ACQ.ServerStore;

import javax.swing.*;

public class MainTest {
    public static void main(String[] args) {
        ServerACS serverACS = new ServerACS();
        serverACS.startServer();
        ServerBankACQ serverBankACQ = new ServerBankACQ();
        serverBankACQ.startServer();
        ServerStore serverStore = new ServerStore();
        serverStore.startServer();

        System.out.println("Démarrage de l'application client");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ClientCH clientCH = new ClientCH();
                    clientCH.setLocationRelativeTo(null);
                    clientCH.setVisible(true);
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

    }
}
