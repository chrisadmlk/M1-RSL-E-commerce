package mysecurity.encryption;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.AlgorithmParameterSpec;

public class AlgorithmParam implements AlgorithmParameterSpec {
    private BigInteger bytes;

    public AlgorithmParam(BigInteger bytes) {
        this.bytes = bytes;
    }

    public static void main(String[] args) {
        BigInteger sheeesh = new BigInteger("14444444444");
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("DES","BC");
            keyGenerator.init(new AlgorithmParam(sheeesh));
            SecretKey secret = keyGenerator.generateKey();
            System.out.println(secret);
            keyGenerator = KeyGenerator.getInstance("DES","BC");
            keyGenerator.init(new AlgorithmParam(sheeesh));
            SecretKey secret2 = keyGenerator.generateKey();
            System.out.println(secret2);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }


    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
