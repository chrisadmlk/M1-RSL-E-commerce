package mysecurity.encryption;

import java.security.*;

public abstract class KeyPairCreator {
    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    public static KeyPair create() throws NoSuchProviderException, NoSuchAlgorithmException {
        // Asymmetric keys generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM,"BC");
        keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    public static void main(String[] args) {
        try {
            System.out.println(KeyPairCreator.create());
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
