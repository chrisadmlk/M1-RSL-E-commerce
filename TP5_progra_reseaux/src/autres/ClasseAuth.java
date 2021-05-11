package autres;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import client_ACS.obj.AuthClientRequest;
import client_ACS.obj.AuthServerResponse;
import mysecurity.encryption.AsymmetricCryptTool;

public class ClasseAuth {

	
	public static AuthServerResponse authenticateProcess(String login, String pin) throws IOException, ClassNotFoundException {
		

		Security.addProvider(new BouncyCastleProvider()); 			// la modification du fichier java.security ne fonctionnait pas chez moi...


        Socket authSocket = null;
        authSocket = new Socket("localhost", 51002);
        System.out.println("#-> Client se connecte : " + authSocket.getInetAddress().toString());

        ObjectOutputStream authWriter = new ObjectOutputStream(authSocket.getOutputStream());
        ObjectInputStream authReader = new ObjectInputStream(authSocket.getInputStream());

        // Secure
        authWriter.writeUTF("SECURE"); authWriter.flush();
        AsymmetricCryptTool myKeys = new AsymmetricCryptTool();
        myKeys.createKeyPair();
        AsymmetricCryptTool serverKey = new AsymmetricCryptTool();
        serverKey.setPublicKey((PublicKey) authReader.readObject());
        authWriter.writeObject(myKeys.getPublicKey());

        // Authentication
        authWriter.writeUTF("AUTH"); authWriter.flush();
        AuthClientRequest request = new AuthClientRequest("Bruce Wayne","2222");
        request.setSignature(myKeys.authenticate(request.gatherInfos()));
        authWriter.writeObject(request); authWriter.flush();

        String success = authReader.readUTF();
        if (success.equals("OK")) {
            AuthServerResponse response = (AuthServerResponse) authReader.readObject();
            authReader.close();
            authWriter.close();
            authSocket.close();
            return response;
        } else {
            authReader.close();
            authWriter.close();
            authSocket.close();
            return null;
        }
    }
	
}
