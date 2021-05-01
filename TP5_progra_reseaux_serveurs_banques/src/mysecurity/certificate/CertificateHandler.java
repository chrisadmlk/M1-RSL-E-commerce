package mysecurity.certificate;

import java.io.Serializable;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

public class CertificateHandler implements Serializable {
    private final X509Certificate certificate;

    public CertificateHandler(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public PublicKey getPublicKey(){
        return certificate.getPublicKey();
    }

    public String gatherInformation(){
        String s = "Contenu du certificat : \n";
        s += "Classe de l'objet : " + certificate.getClass().getName() + "\n";
        s += "Type de certificat : " + certificate.getType() + "\n";
        s += "Nom propriétaire du certificat" + certificate.getSubjectDN().getName() + "\n";
        s += "Clé publique : " + getPublicKey().toString() + "\n";
        s += "Dates limites de validé : { " + certificate.getNotBefore() + " - "
                + certificate.getNotAfter() + " }\n";
        s += "Signataire du certificat : " + certificate.getIssuerDN().getName() +"\n";
        s += "Algorithme de signature : " + certificate.getSigAlgName() + "\n";
        s += "Signature : " + certificate.getSignature();
        return s;
    }

    public boolean checkValidity(){
        try {
            certificate.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            System.out.println("--- Certificat invalide ---");
            return false;
        }
        return true;
    }

    public boolean checkSignature(){
        try {
            certificate.verify(getPublicKey());
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            return false;
        }
        return true;
    }
}
