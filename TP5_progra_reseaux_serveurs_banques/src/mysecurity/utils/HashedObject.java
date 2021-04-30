package mysecurity.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class HashedObject extends TransferObject {
    private final String codeProvider = "BC";
    public String getAlgorithm() {
        return algorithm;
    }
    private String algorithm;

    public HashedObject(byte[] hash, String algorithm) {
        super(hash);
        this.algorithm = algorithm;
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(algorithm,codeProvider);
            messageDigest.update(getBytes());
            setBytes(messageDigest.digest());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyHash(byte[] bytesToVerify){
        MessageDigest messageDigest = null;
        byte[] compare = null;
        try {
            messageDigest = MessageDigest.getInstance(algorithm,codeProvider);
            messageDigest.update(bytesToVerify);
            compare = messageDigest.digest();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        System.out.println(compare + " -- " + getBytes());
        // return compare == getBytes();
        return MessageDigest.isEqual(compare,getBytes());
    }
}
