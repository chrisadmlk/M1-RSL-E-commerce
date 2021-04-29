package marchand_ACQ;

import java.net.Socket;

public interface I_Requete {
	public Runnable createRunnable (Socket s); 
}
