package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import autres.ClasseAuth;
import client_ACS.ServerACS;
import client_ACS.obj.AuthClientRequest;
import client_ACS.obj.AuthServerResponse;
import marchand_ACQ.ServerBankACQ;
import marchand_ACQ.ServerStore;
import mysecurity.encryption.AsymmetricCryptTool;

public class Test_n5313 {


	public static void main(String[] args) {
		
		Security.addProvider(new BouncyCastleProvider()); 			// la modification du fichier java.security ne fonctionnait pas chez moi...
   	 
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
        
        
        String name="Bruce Wayne";
        String pin="2222";
        ClasseAuth test;
   	 
        Socket clientSocket = null;
        AuthServerResponse responseAuth;
        try {
        	responseAuth = ClasseAuth.authenticateProcess("Bruce Wayne","2222");
			 // chargement du certificat sign� par la banque du client
			 PublicKey cl�PubliqueBanqueClient  = null;
			 X509Certificate certifBanqueClient = null;
			 KeyStore ksv;
			
				ksv = KeyStore.getInstance("JKS");
				ksv.load(new FileInputStream("C:\\Programmation\\IDE - programmes\\Eclipse\\Projets\\Progra_reseau_TP5_All\\M1-RSL-E-commerce\\TP5_progra_reseaux_serveurs_banques\\bruce"), "pwdpwd".toCharArray()); 
				certifBanqueClient = (X509Certificate)ksv.getCertificate("bruce");
			
	    	 cl�PubliqueBanqueClient = certifBanqueClient.getPublicKey();
			 
            // Verify signature
			 AsymmetricCryptTool clientKeys = new AsymmetricCryptTool();
			 clientKeys.setPublicKey(cl�PubliqueBanqueClient); 
            String concat =  responseAuth.getBankName() + responseAuth.getClientName() + responseAuth.getSerialNumber();
            if (!clientKeys.verifyAuthentication(concat.getBytes(), responseAuth.getSignature())) {
                System.out.println("Signature incorrecte");
            }
            else  System.out.println("Signature correcte");
			 
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
    }
	
}
	

