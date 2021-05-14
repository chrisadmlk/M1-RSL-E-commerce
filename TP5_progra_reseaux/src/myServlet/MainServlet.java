package myServlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autres.BeanAccessOracle;
import autres.ClasseAuth;
import client_ACS.obj.AuthClientRequest;
import client_ACS.obj.AuthServerResponse;
import marchand_ACQ.ServerStore;
import marchand_ACQ.obj.DebitRequest;
import mysecurity.encryption.AsymmetricCryptTool;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	 BeanAccessOracle beanOracle;
	 ClasseAuth classAuth;
	 @Override
	 public void init (ServletConfig c) throws ServletException 
	 { 
		 super.init(c);
		 try {
			 // beanOracle = new BeanAccessOracle(getInitParameter("UserSGBD"));   TODO : problèmes à la lecture des initparam
			 beanOracle = new BeanAccessOracle("MERCHANT");
		 } catch (Exception e) {e.printStackTrace();};
	 }

	 
	 
	 
	 
	 protected void processRequest(HttpServletRequest request, HttpServletResponse response) 
			 throws ServletException, IOException 
	 { 
		 String rep="";
		 String query="";
		 ResultSet result;
		 Boolean test = false;
		 String nomClient=null;
		 
		 String idDansCookie = null; 
		 String contenuPanier = "";
		 Cookie[] tabCookies = request.getCookies(); 
		 
		 boolean redemandeduPin = false;
		 
		 response.setContentType("text/html"); 
		 ServletOutputStream out = response.getOutputStream(); 
		 out.println("<HTML><HEAD><TITLE>"); 
		 out.println("Produits de la boutique"); 
		 out.println("</TITLE></HEAD><BODY>"); 
		 String titre = "Produits de la boutique"; 
		 out.println("<H2>" + titre + "</H2>"); 

		 
		 
		 
		 // nécessaire pour "nettoyer" les cookies des tests précédents
		 Cookie[] cookies = request.getCookies();
		 if(cookies!=null)
		 for (int i = 0; i < cookies.length; i++) {
		  cookies[i].setMaxAge(0);
		  cookies[i].setPath("/");
		  response.addCookie(cookies[i]);
		 }
		 
		 
		 
		 
		 // recherche d'un id de session
		 if (tabCookies != null) 
			 for (int i=0; i<tabCookies.length; i++) 
			 { 
				 if ("idSession".equals(tabCookies[i].getName())) 
					 {idDansCookie =  URLDecoder.decode( tabCookies[i].getValue(), "UTF-8" ) ; 
				 System.out.println(idDansCookie);}
			 }
		 String idNewDansCookie=null; 
		 
		 
		 // si première connexion : attribution d'un id après vérification du login/mdp
		 if (idDansCookie == null) 
		 { 

			 // Test login mdp
			 try { 
				 String mdpReceived = request.getParameter("pwd");
				 query = "SELECT * FROM MERCHANT.LOGIN WHERE lgn='"+ request.getParameter("login") +"'";  
				 //query = "SELECT * FROM MERCHANT.LOGIN";
				 System.out.println(query);
		  		 result = beanOracle.executeQuery(query);
		      	 String mdp;
		      	 while (result.next()){ 
		  			mdp = result.getString(2); 
		  			if (mdp.equals(mdpReceived)) test = true; 
		  		 } 
			 } 
			 catch (SQLException e) {  
				 e.printStackTrace(); 
			 } 
	
			// échec du login : message d'erreur + sortie de la méthode
			if (! test) {
				 out.println("<p>Échec du login...</p>"); 	
				 out.println("</body>");
				 out.println("</html>");
				 out.close();	
				 return;
			}
			
			// réussite du login : création des cookies de session 
			else {
				idNewDansCookie = (new Integer((int)(Math.random()*1000))).toString() ; 
				String dataPanier = "";
				Cookie cookieId = new Cookie ("panier", URLEncoder.encode( dataPanier, "UTF-8" ) );
				Cookie cookieId2 = new Cookie ("idSession", URLEncoder.encode( idNewDansCookie, "UTF-8" ) ); 
				Cookie cookieNom = new Cookie ("nomClient", URLEncoder.encode( request.getParameter("login"), "UTF-8" ) ); 
				System.out.println("Valeurs de cookie attribuées : " + cookieId.getValue() + " - " + cookieId2.getValue() + " - " + cookieNom.getValue() );
				response.addCookie(cookieId); 
				response.addCookie(cookieId2);  
				response.addCookie(cookieNom);  
			}

		 }
		 
		 
		 
		 
		 
		 // pas la première visite => accéder via un des deux boutons ajouter ou payer
		 else
		 {
			 // servira à checker le boutons appelant
			 String action = request.getParameter("action");	
			
			 // on récupère le contenu actuel du panier
			 for (int i=0; i<tabCookies.length; i++) 
			 { 
				 if ("panier".equals(tabCookies[i].getName())) {
					 contenuPanier = URLDecoder.decode( tabCookies[i].getValue(), "UTF-8" ) ; 
				 }
				 if ("nomClient".equals(tabCookies[i].getName())) {
					 nomClient = URLDecoder.decode( tabCookies[i].getValue(), "UTF-8" ) ; 
				 }
			 }
			 
			 if (action != null) {
				 
				 
				 // ajout d'une ligne produit au cookie panier
				 if (action.equals("Ajouter")) {

					 String queryRep=null;
					 String lineToAddToCookie="";

					 try {
						 queryRep = beanOracle.executeProcedureChangeStock( Integer.parseInt(request.getParameter("quantity")),  request.getParameter("item"));
					} catch (NumberFormatException | SQLException e) { e.printStackTrace();	}
					    
					 if (queryRep.equals("Opération correctement effectuée")) {
						 lineToAddToCookie = request.getParameter("item") + ";" + request.getParameter("quantity");
					 }
					 else if (queryRep.equals("Pas suffisament de stock !")) {
						 lineToAddToCookie = request.getParameter("item") + ";" + request.getParameter("quantity") + ";" + "failed";
					 }
					 else System.out.println("Erreur dans la réponse de la procédure d'ajout / retrait d'items en stock");
					 

					 contenuPanier = contenuPanier + "/" + lineToAddToCookie;
				 } 
				 
				 
				 
				 // lancement du processus de payement
				 else if (action.equals("Payer")) {
					 
//					 // TODO : auth du client, verif de la réponse aini que de la signature de la réponse de sa banque
//					 String pin = request.getParameter("pin");
//					 AuthServerResponse authResponse = null;
//					 boolean testAuth = true;
////					 authResponse = authClient(nomClient, pin);
//					 try {
//						authResponse = classAuth.authenticateProcess(nomClient, pin);
//					} catch (ClassNotFoundException | IOException e2) {  e2.printStackTrace(); 	}
//					 if( !(authResponse.getClientName().equals(nomClient))) {
//						 testAuth = false;
//					 }
//					 
//					 
//					 // chargement du certificat signé par la banque du client
//					 PublicKey cléPubliqueBanqueClient  = null;
//					 X509Certificate certifBanqueClient = null;
//					 KeyStore ksv;
//					try {
//						ksv = KeyStore.getInstance("JKS");
//						ksv.load(new FileInputStream("C:\\Programmation\\IDE - programmes\\Eclipse\\Projets\\Progra_reseau_TP5_All\\M1-RSL-E-commerce\\TP5_progra_reseaux_serveurs_banques\\bruce"), "pwdpwd".toCharArray()); 
//						certifBanqueClient = (X509Certificate)ksv.getCertificate("bruce");
//					} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e1) {
//						e1.printStackTrace();
//					}
//			    	 cléPubliqueBanqueClient = certifBanqueClient.getPublicKey();
//					 
//                     // Verify signature
//					 AsymmetricCryptTool clientKeys = new AsymmetricCryptTool();
//					 clientKeys.setPublicKey(cléPubliqueBanqueClient); 	// TODO  : mettre clé publique du server ACS auth
//                     String concat =  authResponse.getBankName() + authResponse.getClientName() + authResponse.getSerialNumber();
//                     if (!clientKeys.verifyAuthentication(concat.getBytes(), authResponse.getSignature())) {
//                         System.out.println("Signature incorrecte");
//                         testAuth = false;
//                     }
//					 
//	
//      	 			
//      	 			
//      	 			
//
//					
//					if (!testAuth) {
//						redemandeduPin = true;
//					}
//					else {
						// calcul du montant
						double montant=0;
						String[] lignesPanier = contenuPanier.split("/");		 
						for (int j =1; j<lignesPanier.length; j++ ) {    // on commence à j=1 pour token vide de départ
							String[] elementsLigne = lignesPanier[j].split(";");
							if (elementsLigne.length == 2) {		// cas d'une ligne précédemment acceptée dans le panier
								try {
									int nb = Integer.parseInt(elementsLigne[1]);
									query = "SELECT price FROM MERCHANT.STOCK WHERE item_name='"+ elementsLigne[0] +"'";  
									result = beanOracle.executeQuery(query);
									result.next();
									double price = result.getDouble(1);
									montant = montant + price * nb;
								} catch (SQLException e) {e.printStackTrace();}
							}
						}	
						
						
//						boolean testPayement = tryPayement(nomClient, montant);
						
					 
					 boolean testPayement = true;
						if (testPayement) {
							// message de réusssite et ajout dans infos dans la table commands
							lignesPanier = contenuPanier.split("/");		 
							for (int j =1; j<lignesPanier.length; j++ ) {    
								String[] elementsLigne = lignesPanier[j].split(";");
								if (elementsLigne.length == 2) {	
									try {
										query = "INSERT INTO MERCHANT.COMMANDS (client_name, item_name, quantity) values('"+nomClient+"', '"+elementsLigne[0]+"', "+elementsLigne[1]+")";  
										beanOracle.executeUpdate(query);
									} catch (SQLException e) {e.printStackTrace();}
								}
							}
							 out.println("<p>Réussite du payement !</p>"); 	
							 out.println("</body>");
							 out.println("</html>");
							 out.close();	
							 return;
						}
						else {
							// message d'erreur et remise des produits du panier dans le stock
							lignesPanier = contenuPanier.split("/");		 
							for (int j =1; j<lignesPanier.length; j++ ) {    
								String[] elementsLigne = lignesPanier[j].split(";");
								if (elementsLigne.length == 2) {	
									 try {
										 int nb = Integer.parseInt(elementsLigne[1]);
										 nb = -nb;
										 beanOracle.executeProcedureChangeStock( nb, elementsLigne[0]);
									} catch (NumberFormatException | SQLException e) { e.printStackTrace();	}
								}
							}
							out.println("<p>Échec du payement...</p>"); 	
							out.println("</body>");
							out.println("</html>");
							out.close();	
							return;
						}
					}
					
//				 } 
				 
				 
				 
				 else  System.out.println("Problème dans l'analyse des boutons actions !!");

				 
				 // on recopie les cookies mis à jours
				 Cookie cookieId = new Cookie ("panier",  URLEncoder.encode( contenuPanier, "UTF-8" ) ); 
				 Cookie cookieId2 = new Cookie ("idSession", URLEncoder.encode( idDansCookie, "UTF-8" ) ); 
				 Cookie cookieNom = new Cookie ("nomClient", URLEncoder.encode( nomClient, "UTF-8" ) ); 
				 System.out.println(contenuPanier);
				 response.addCookie(cookieId); 
				 response.addCookie(cookieId2); 
				 response.addCookie(cookieNom); 

			 }
		 }
		 
		 

		 

		 // création d'un tableau html regroupant les produits de la boutique

		 out.println("<table>"); 
		 out.println("<thead>"); 
		 out.println("<tr><th>Produit</th><th>Prix</th></tr>"); 
		 out.println("</thead>"); 
		 out.println("<tbody>"); 

		 query="SELECT * FROM Stock";  
		 try { 
	  		 result = beanOracle.executeQuery(query);
	      	 while (result.next()){  
	  			 out.println("<tr><td>" + result.getString(1) + "</td><td>" + result.getDouble(2) + "</td></tr>");  
	  		 }
		 } 
		 catch (SQLException e) {  e.printStackTrace(); } 
		 
		 out.println("</tbody>");
		 out.println("</table>");
		 out.println("<br>");
		 out.println("<form action=MainServlet >");
		 out.println("Item : <input type=text name=item><br>");
		 out.println("Quantité : <input type=text name=quantity><br>");
		 out.println("<input type=submit name=action value=Ajouter>");
		 out.println("</form>");	
		 out.println("<br>");
		 out.println("<p>Panier :</p>");
		 out.println("<textarea id=&#34;story&#34; name=&#34;story&#34;");
		 out.println("rows=5 cols=100 width=200 >");
			

		 // S'il y a quelque chose dans le panier, on l'ajoute au code html
			
		 if ( !(contenuPanier.equals(""))) {			 
			String[] lignesPanier = contenuPanier.split("/");		 
			for (int j =1; j<lignesPanier.length; j++ ) {    // on commence à j=1 pour token vide de départ
				String[] elementsLigne = lignesPanier[j].split(";");
				if (elementsLigne.length == 2)  out.println(elementsLigne[1] + " x " + elementsLigne[0] );
			}	 
		}	

	
		 out.println("</textarea>");
		 out.println("<br>");
		 out.println("<form action=MainServlet >");
		 if (redemandeduPin) out.println("Pin : <input type=text name=pin style=background-color:#ff696 required>");
		 else out.println("Pin : <input type=text name=pin required>");
		 out.println("<input type=submit name=action value=Payer>");
		 out.println("</form>");
		 out.println("<br>");
		 out.println("<p>Informations :</p>");
		 out.println("<textarea id=&#34;story&#34; name=&#34;story&#34;");
		 out.println("rows=5 cols=100 width=200 >");
		 
		 
		 
		// Zone d'infos : sert à dire quand un produit n'a pas été ajouté au panier
			if ( !(contenuPanier.equals(""))) {			 
				String[] lignesPanier = contenuPanier.split("/");		 
				for (int j =1; j<lignesPanier.length; j++ ) {    // on commence à j=1 pour token vide de départ
					String[] elementsLigne = lignesPanier[j].split(";");
					if (elementsLigne.length == 3)  out.println("!!! échec de l'ajout de : " + elementsLigne[1] + " x " + elementsLigne[0] + "!!!" );
				}	 
			}	
		 
		 out.println("</textarea>");
		 out.println("</body>");
		 out.println("</html>");
		 
		 out.close();
		 

		 
	 }
	 
	 
	 
	 
	 
	 
	 
//	 protected AuthServerResponse authClient(String name, String pin)  {
//		 
//         Socket clientSocket = null;
//         AuthServerResponse responseAuth = null;
//         try {
//             clientSocket = new Socket("localhost", 51002);
//
//             System.out.println("#-> Client se connecte : " + clientSocket.getInetAddress().toString());
//
//             ObjectOutputStream writer = new ObjectOutputStream(clientSocket.getOutputStream());
//             ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
//
//             // Secure
//             writer.writeUTF("SECURE"); writer.flush();
//             AsymmetricCryptTool myKeys = new AsymmetricCryptTool();
//             myKeys.createKeyPair();
//             AsymmetricCryptTool serverKey = new AsymmetricCryptTool();
//             serverKey.setPublicKey((PublicKey) reader.readObject());
//             if (myKeys.getPublicKey() == null)  {
//            	 System.out.println("teuteuteu");
//            	 return responseAuth;
//             }
//             
//             writer.writeObject(myKeys.getPublicKey());
//
//             System.out.println("Keys : " + serverKey.getPublicKey() + " - " + myKeys.getPublicKey());
//
//             // Authentication
//             writer.writeUTF("AUTH"); writer.flush();
//             AuthClientRequest request = new AuthClientRequest(name, pin);
//             request.setSignature(myKeys.authenticate(request.gatherInfos()));
//             writer.writeObject(request); writer.flush();
//
//             System.out.println("Request : " + request.toString());
//             System.out.println("TEST : " + reader.readUTF());
//
//             responseAuth = (AuthServerResponse) reader.readObject();
//
//         } catch (IOException | ClassNotFoundException e ){
//             e.printStackTrace();
//         }
//         return responseAuth;
//	 }
	 
	 
	 
	 
	 
	 protected boolean tryPayement(String name, Double montant) {
		 

	        SSLSocket paySocket = null;
	        ObjectOutputStream payWriter = null;
	        ObjectInputStream payReader = null;
	        boolean isSuccessful = false;

	        String FILE_KEYSTORE = "C:\\Programmation\\IDE - programmes\\Eclipse\\Projets\\Progra_reseau_TP5_All\\M1-RSL-E-commerce\\TP5_progra_reseaux_serveurs_banques\\bruce";

	        AsymmetricCryptTool crypt = new AsymmetricCryptTool();
	        crypt.loadFromKeystore(FILE_KEYSTORE,"pwdpwd","bruce");

	        try {
	            // Keystore
	            KeyStore serverACSKeyStore = KeyStore.getInstance("JKS");
	            char[] passwd = "pwdpwd".toCharArray();
	            FileInputStream serverInput = new FileInputStream(FILE_KEYSTORE);
	            serverACSKeyStore.load(serverInput, passwd);
	            System.out.println("--------KS--------");
	            // Context
	            SSLContext sslContext = SSLContext.getInstance("SSLv3");
	            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
	            String alg = keyManagerFactory.getAlgorithm();
	            System.out.println(alg);
	            keyManagerFactory.init(serverACSKeyStore, passwd);
	            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(alg);
	            trustFactory.init(serverACSKeyStore);
	            sslContext.init(keyManagerFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);
	            System.out.println("--------CONTEXT--------");

	            // Factory
	            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
	            System.out.println("--------FACTORY--------");

	            // Socket
	            paySocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 51001);
	            System.out.println("--------SOCKET--------");

	            payWriter = new ObjectOutputStream(paySocket.getOutputStream());

	            // Send MONEY request
	            payWriter.writeUTF("MONEY");
	            payWriter.flush();
	            AuthServerResponse authServerResponse = new AuthServerResponse("Gotham National Bank","Bruce Wayne",41111);
	            authServerResponse.setSignature(crypt.authenticate(authServerResponse.concatForSignature()));
	            DebitRequest debitRequest = new DebitRequest(100,authServerResponse);
	            System.out.println(debitRequest);
	            payWriter.writeObject(debitRequest);
	            payWriter.flush();

	            String response = payReader.readUTF();
	            if (response.equals("SUCCESS")) {
	                isSuccessful = true;
	                System.out.println("SUCCESS");
	                return true;
	            }
	            // Close everything
	            payWriter.close();
	            payReader.close();
	            paySocket.close();
	        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
	            e.printStackTrace();
	        } catch (NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
		 if (isSuccessful) return true;
		 else return false;
	 }
	 
	 
	 
	 
	 protected synchronized void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		 processRequest(request, response); 
}
 

	protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		 processRequest(request, response); 
	 }	 
 

}
