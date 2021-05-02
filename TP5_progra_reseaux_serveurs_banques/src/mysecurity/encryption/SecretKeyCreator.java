package mysecurity.encryption;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public abstract class SecretKeyCreator {
    private static final String ALGORITHM = "DES";

    public static SecretKey create() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM,"BC");
        keyGenerator.init(new SecureRandom());
        return keyGenerator.generateKey();
    }
}
