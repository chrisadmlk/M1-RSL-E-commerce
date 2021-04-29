package client_ACS;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class serverACS_MONEY extends Thread {

	ServerSocket SSocket=null;
	int portMONEY = 8086;
	
	

	public serverACS_MONEY () {
		 try	{  SSocket = new ServerSocket(portMONEY); } 
		 catch (IOException e) { System.err.println("Erreur de port d'écoute MONEY [" + e + "]"); System.exit(1); }
	}
	

	
	public void run() {
		Socket CSocket = null;
		while (!isInterrupted()) {					
			try {
					System.out.println("************ Serveur MONEY en attente");
					CSocket = SSocket.accept();
					System.out.println(CSocket.getRemoteSocketAddress().toString()+ "#accept#thread serveur MONEY");
					ThreadConsoACS oneThreadConso = new ThreadConsoACS("NameTemp" , CSocket, 1);
					oneThreadConso.start();
			}
			catch (IOException e) { System.err.println("Erreur d'accept ! ? [" + e.getMessage() + "]"); System.exit(1); }
	
		}

	}
	
}
