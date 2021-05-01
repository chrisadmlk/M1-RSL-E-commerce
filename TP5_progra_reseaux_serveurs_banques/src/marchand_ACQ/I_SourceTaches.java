package marchand_ACQ;

public interface I_SourceTaches {
	 public Runnable getTache() throws InterruptedException;
	 public boolean existTaches();
	 public void recordTache (Runnable r);
} 

