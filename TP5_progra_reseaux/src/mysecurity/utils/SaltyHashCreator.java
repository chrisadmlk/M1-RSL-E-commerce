package mysecurity.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class SaltyHashCreator {
    public static String create(String pwd){
        String hash = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            byte[] salt = getSalt(pwd);
            messageDigest.update(salt);
            byte[] bytes = messageDigest.digest(pwd.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte aByte : bytes) {
                stringBuilder.append(
                        Integer.toString(
                                (aByte & 0xff) + 0x100,
                                16
                        ).substring(1)
                );
            }
            hash = stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    private static byte[] getSalt(String message) throws NoSuchAlgorithmException {
        char[] array =  message.toCharArray();
        double phi = (1 + Math.sqrt(5)) /2;
        Integer salt = (int) Math.round(Math.pow(phi, array[0]) / Math.sqrt(5));
        String tmp = salt.toString();
        return tmp.getBytes();
    }
}
