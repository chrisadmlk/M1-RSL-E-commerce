package myServlet;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import autres.BeanAccessOracle;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 BeanAccessOracle beanOracle;
	 
	 @Override
	 public void init (ServletConfig c) throws ServletException 
	 { 
		 super.init(c);
		 try {
			 // beanOracle = new BeanAccessOracle(getInitParameter("UserSGBD"));   TODO : probl�mes � la lecture des initparam
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
		 
		 response.setContentType("text/html"); 
		 ServletOutputStream out = response.getOutputStream(); 
		 out.println("<HTML><HEAD><TITLE>"); 
		 out.println("Produits de la boutique"); 
		 out.println("</TITLE></HEAD><BODY>"); 
		 String titre = "Produits de la boutique"; 
		 out.println("<H2>" + titre + "</H2>"); 

		 
		 
		 
		 // n�cessaire pour "nettoyer" les cookies des tests pr�c�dents
		 
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
		 
		 
		 // si premi�re connexion : attribution d'un id apr�s v�rification du login/mdp
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
	
			// �chec du login : message d'erreur + sortie de la m�thode
			if (! test) {
				 out.println("<p>�chec du login...</p>"); 	
				 out.println("</body>");
				 out.println("</html>");
				 out.close();	
				 return;
			}
			
			// r�ussite du login : cr�ation des cookies de session 
			else {
				idNewDansCookie = (new Integer((int)(Math.random()*1000))).toString() ; 
				String dataPanier = "";
				Cookie cookieId = new Cookie ("panier", URLEncoder.encode( dataPanier, "UTF-8" ) );
				Cookie cookieId2 = new Cookie ("idSession", URLEncoder.encode( idNewDansCookie, "UTF-8" ) ); 
				Cookie cookieNom = new Cookie ("nomClient", URLEncoder.encode( request.getParameter("login"), "UTF-8" ) ); 
				System.out.println("Valeurs de cookie attribu�es : " + cookieId.getValue() + " - " + cookieId2.getValue() + " - " + cookieNom.getValue() );
				response.addCookie(cookieId); 
				response.addCookie(cookieId2);  
				response.addCookie(cookieNom);  
			}

		 }
		 
		 
		 
		 
		 
		 // pas la premi�re visite => acc�der via un des deux boutons ajouter ou payer
		 else
		 {
			 // servira � checker le boutons appelant
			 String action = request.getParameter("action");	
			
			 // on r�cup�re le contenu actuel du panier
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
					    
					 if (queryRep.equals("Op�ration correctement effectu�e")) {
						 lineToAddToCookie = request.getParameter("item") + ";" + request.getParameter("quantity");
					 }
					 else if (queryRep.equals("Pas suffisament de stock !")) {
						 lineToAddToCookie = request.getParameter("item") + ";" + request.getParameter("quantity") + ";" + "failed";
					 }
					 else System.out.println("Erreur dans la r�ponse de la proc�dure d'ajout / retrait d'items en stock");
					 

					 contenuPanier = contenuPanier + "/" + lineToAddToCookie;
				 } 
				 
				 
				 
				 // lancement du processus de payement
				 else if (action.equals("Payer")) {
					 
					 // TODO : cas bouton payer : faire somme gr�ce aux infos dans le cookie panier et enclencher processus de payement
					
					
				 } 
				 
				 
				 else  System.out.println("Probl�me dans l'analyse des boutons actions !!");

				 
				 // on recopie les cookies mis � jours
				 Cookie cookieId = new Cookie ("panier",  URLEncoder.encode( contenuPanier, "UTF-8" ) ); 
				 Cookie cookieId2 = new Cookie ("idSession", URLEncoder.encode( idDansCookie, "UTF-8" ) ); 
				 Cookie cookieNom = new Cookie ("nomClient", URLEncoder.encode( nomClient, "UTF-8" ) ); 
				 System.out.println(contenuPanier);
				 response.addCookie(cookieId); 
				 response.addCookie(cookieId2); 
				 response.addCookie(cookieNom); 

			 }
		 }
		 
		 

		 

		 // cr�ation d'un tableau html regroupant les produits de la boutique

		 out.println("<thead>");
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
		 out.println("Quantit� : <input type=text name=quantity><br>");
		 out.println("<input type=submit name=action value=Ajouter>");
		 out.println("</form>");	
		 out.println("<br>");
		 out.println("<p>Panier :</p>");
		 out.println("<textarea id=&#34;story&#34; name=&#34;story&#34;");
		 out.println("rows=5 cols=100 width=200 >");
			

		 // S'il y a quelque chose dans le panier, on l'ajoute au code html
			if ( !(contenuPanier.equals(""))) {			 
				String[] lignesPanier = contenuPanier.split("/");		 
				for (int j =1; j<lignesPanier.length; j++ ) {    // on commence � j=1 pour token vide de d�part
					String[] elementsLigne = lignesPanier[j].split(";");
					if (elementsLigne.length == 2)  out.println(elementsLigne[1] + " x " + elementsLigne[0] );
				}	 
			}	

	
		 out.println("</textarea>");
		 out.println("<br>");
		 out.println("<form action=&#34;ProcessingPayement&#34; >");
		 out.println("<input type=submit name=action value=Payer>");
		 out.println("</form>");
		 out.println("<br>");
		 out.println("<p>Informations :</p>");
		 out.println("<textarea id=&#34;story&#34; name=&#34;story&#34;");
		 out.println("rows=5 cols=100 width=200 >");
		 
		 
		 
		// Zone d'infos : sert � dire quand un produit n'a pas �t� ajout� au panier
			if ( !(contenuPanier.equals(""))) {			 
				String[] lignesPanier = contenuPanier.split("/");		 
				for (int j =1; j<lignesPanier.length; j++ ) {    // on commence � j=1 pour token vide de d�part
					String[] elementsLigne = lignesPanier[j].split(";");
					if (elementsLigne.length == 3)  out.println("!!! �chec de l'ajout de : " + elementsLigne[1] + " x " + elementsLigne[0] + "!!!" );
				}	 
			}	
		 
		 out.println("</textarea>");
		 out.println("</body>");
		 out.println("</html>");
		 
		 out.close();
		 

		 
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 protected synchronized void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		 processRequest(request, response); 
}
 

	protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		 processRequest(request, response); 
	 }	 
 

}
