package marchand_ACQ;

import java.sql.SQLException;

public class ThreadACQ extends Thread
{
	 private I_SourceTaches tachesAExecuter;
	 private String nom;
	  
	 
	 private Runnable tacheEnCours;
	 public ThreadACQ(I_SourceTaches st, String n ) {
		 tachesAExecuter = st;
		 nom = n;
	 }

	 public void run(){
		 
		 while (!isInterrupted()) {
			 
			 try {
				 tacheEnCours = tachesAExecuter.getTache();
			 }
			 catch (InterruptedException e) { System.out.println("Interruption : " + e.getMessage()); }
			 System.out.println("run de tachesencours (ThreadConso)");
			 tacheEnCours.run();
		 }
	 }
}
