package client_ACS;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class serverACS_AUTH extends Thread {


	ServerSocket SSocket=null;
	int portAUTH = 8087;
	

	
	public serverACS_AUTH() {
		 try	{  SSocket = new ServerSocket(portAUTH); } 
		 catch (IOException e) { System.err.println("Erreur de port d'écoute AUTH [" + e + "]"); System.exit(1); }
	}
	
	
	
	public void run() {
		
		Socket CSocket = null;
		while (!isInterrupted()) {					
			try {
					System.out.println("************ Serveur AUTH en attente");
					CSocket = SSocket.accept();
					System.out.println(CSocket.getRemoteSocketAddress().toString()+ "#accept#thread serveur AUTH");
					ThreadConsoACS oneThreadConso = new ThreadConsoACS("NameTemp" , CSocket, 2);
					oneThreadConso.start();
			}
			catch (IOException e) { System.err.println("Erreur d'accept ! ? [" + e.getMessage() + "]"); System.exit(1); }	
		}	
	}
	

	
	

}