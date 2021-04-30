package mysecurity.tramap;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class SymmetricCryptTool implements TramapSecurity {

    private Cipher cipher;
    private SecretKey secretKey = null;
    private static final String CODE_PROVIDER = "BC";

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public SymmetricCryptTool(String algorithm) {
        try {
            secretKey = SecretKeyCreator.create();
            cipher = Cipher.getInstance(algorithm, CODE_PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public SymmetricCryptTool(SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        this.secretKey = secretKey;
        cipher = Cipher.getInstance("DES/ECB/PKCS5Padding", CODE_PROVIDER);
    }

    @Override
    public byte[] encrypt(byte[] input) {
        byte[] output = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
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
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            output = cipher.doFinal(input);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public byte[] authenticate(byte[] message) {
        byte[] authentication = null;
        try {
            Mac hmac = Mac.getInstance("HMAC-SHA1","BC");
            hmac.init(secretKey);
            hmac.update(message);
            authentication = hmac.doFinal();
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidKeyException e) {
            e.printStackTrace();
        }
        return authentication;
    }

    @Override
    public boolean verifyAuthentication(byte[] message, byte[] authentication) {
        try {
            Mac hmac = Mac.getInstance("HMAC-SHA1","BC");
            hmac.init(secretKey);
            hmac.update(message);
            byte[] localHmac = hmac.doFinal();
            return MessageDigest.isEqual(localHmac, authentication);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void main(String[] args) {
        SymmetricCryptTool sym = new SymmetricCryptTool("DES/ECB/PKCS5Padding");
        String message = "Par-delà bien et mal";
        byte[] msg = sym.encrypt(message.getBytes());
        byte[] decrypt = sym.decrypt(msg);
        System.out.println(new String(decrypt));
    }
}
