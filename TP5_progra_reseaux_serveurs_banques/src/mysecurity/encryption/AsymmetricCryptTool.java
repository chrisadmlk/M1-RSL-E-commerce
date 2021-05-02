package mysecurity.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

// Contains a pair of Key (Pk, Prk)
// Have methods regarding asymmetric encryption and signatures (from interface AdamSec)
public class AsymmetricCryptTool implements TramapSecurity {
    private Cipher cipher;
    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    private PublicKey publicKey;
    public PublicKey getPublicKey() {
        return publicKey;
    }
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    private PrivateKey privateKey;
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    private X509Certificate certificate = null;
    public X509Certificate getCertificate() {
        return certificate;
    }

    public AsymmetricCryptTool() {
        String algorithm = "RSA/ECB/PKCS1Padding";
        String codeProvider = "BC";
        try {
            this.cipher = Cipher.getInstance(algorithm, codeProvider);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    // Load keys from a keystore
    public void loadFromKeystore(String file, String password, String alias){
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(file), password.toCharArray());
            // PrK
            if(keyStore.isKeyEntry(alias)) {
                setPrivateKey((PrivateKey) keyStore.getKey(alias, password.toCharArray()));
            }
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            setPublicKey(certificate.getPublicKey());
        }
        catch (NoSuchAlgorithmException
                | KeyStoreException
                | CertificateException
                | IOException
                | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }

    public void createKeyPair(){
        try {
            KeyPair keyPair = KeyPairCreator.create();
            setPrivateKey(keyPair.getPrivate());
            setPublicKey(keyPair.getPublic());
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] encrypt(byte[] input) {
        byte[] output = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            output = cipher.doFinal(input);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public byte[] decrypt(byte[] input) {
        byte[] output = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            output = cipher.doFinal(input);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public byte[] authenticate(byte[] message) {
        byte[] signature = null;
        try {
            Signature signTool = Signature.getInstance("SHA256withRSA", "SunRsaSign");
            signTool.initSign(privateKey);
            signTool.update(message);
            signature = signTool.sign();
        }
        catch (SignatureException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidKeyException e) {
            e.printStackTrace();
        }
        return signature;
    }

    @Override
    public boolean verifyAuthentication(byte[] message, byte[] authentication) {
        try {
            Signature signTool = Signature.getInstance("SHA256withRSA", "SunRsaSign");
            signTool.initVerify(publicKey);
            signTool.update(message);
            return signTool.verify(authentication);
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidKeyException
                | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        // Test
        byte[] message = "Ce n'est pas le doute, c'est la certitude qui rend fou.. Et ce code aussi".getBytes();
        int length = message.length;

        KeyPair keyPairServer = null;
        try {
            keyPairServer = KeyPairCreator.create();
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Crypt
        AsymmetricCryptTool cryptClient = new AsymmetricCryptTool();
        assert keyPairServer != null;
        cryptClient.setPublicKey(keyPairServer.getPublic());
        byte[] cipher = cryptClient.encrypt(message);

        System.out.println(new String(message) + " -- -- " + cipher);

        // Decrypt
        AsymmetricCryptTool cryptServer = new AsymmetricCryptTool();
        cryptServer.setPrivateKey(keyPairServer.getPrivate());
        byte[] decrypt = cryptServer.decrypt(cipher);
        System.out.println(decrypt + " -- " + new String(decrypt).substring(0, length) + " -- " + new String(decrypt));

        // Signature
        byte[] messageToSign = "Livre à lire absolument : Pensée pour moi-même de Marc Aurèle".getBytes();
        KeyPair keyPairClient = null;
        try {
            keyPairClient = KeyPairCreator.create();
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert keyPairClient != null;
        cryptClient.setPrivateKey(keyPairClient.getPrivate());
        cryptServer.setPublicKey(keyPairClient.getPublic());
        byte[] signature = cryptClient.authenticate(messageToSign);
        System.out.println("Signature ok ? : " + cryptServer.verifyAuthentication(messageToSign, signature));
    }
}