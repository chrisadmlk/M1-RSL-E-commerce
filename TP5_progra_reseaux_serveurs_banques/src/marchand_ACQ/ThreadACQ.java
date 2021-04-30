package marchand_ACQ;

public class ThreadACQ extends Thread
{
	 private I_SourceTaches tachesAExecuter;
	 private String name;
	 
	 private Runnable tacheEnCours;
	 public ThreadACQ(I_SourceTaches st, String n ) {
		 tachesAExecuter = st;
		 name = n;
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
