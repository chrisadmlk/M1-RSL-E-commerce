package test;

import java.security.Security;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import client.ClientCH;
import client_ACS.ServerACS;
import marchand_ACQ.ServerBankACQ;
import marchand_ACQ.ServerStore;

public class LaunchAllServer {

	
	public static void main(String[] args) {
		

        Thread server = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerACS serverACS = new ServerACS();
                serverACS.startServer();
            }
        });
        server.start();

        ServerBankACQ serverBankACQ = new ServerBankACQ();
        serverBankACQ.startServer();
        ServerStore serverStore = new ServerStore();
        serverStore.startServer();


    }
}