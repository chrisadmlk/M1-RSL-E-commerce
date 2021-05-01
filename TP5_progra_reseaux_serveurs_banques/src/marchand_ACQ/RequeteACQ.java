package marchand_ACQ;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.sql.ResultSet;

import common.BeanAccessOracle;

public class RequeteACQ implements I_Requete, Serializable {
	
	 private int type;
	 private int protocoleTag;
	 private String chargeUtile;
	 private Socket socketClient;
	 
	 public RequeteACQ(int p, int t, String chu, Socket s) {
		 protocoleTag = p; type = t; chargeUtile = chu; socketClient =s;
	 }
	 
	 public String getChargeUtile() {
	 	return chargeUtile;
	 }
	 
	 public Runnable createRunnable (final Socket s)  { 
		 return new Runnable() {
			 public void run() {
				 traiteRequete(s);
			 }
		 }; 
	 }
		
	 private void traiteRequete(Socket sock) {
		 BeanAccessOracle beanOracle;
		 DataInputStream dis = null; DataOutputStream dos = null;
		 int choix = type;
		 String s = chargeUtile; 
		 String rep="";
	 	 String query="";
	 	 ResultSet result;
	 	 String[] token = null;

         try {
 			
        	 dis = new DataInputStream(new BufferedInputStream(socketClient.getInputStream()));
			 dos = new DataOutputStream(new BufferedOutputStream(socketClient.getOutputStream()));
			 beanOracle = new BeanAccessOracle("ACS");

		} catch ( Exception e) { e.printStackTrace();	}

         s = s.substring(0, s.length() - 1); 
         
         
         // chargement des infos de cryptage
         
         
         
         // d�cryptage
         
         
         // traitement des requ�tes en clair
         
         token = s.split(";");
         
         // recryptage

      	try{  
      		dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
      		dos.write(rep.getBytes()); dos.flush(); }
     	catch (IOException e) { System.out.println("Erreur d'�criture = " + e.getMessage()); }
	 }
	 
}






