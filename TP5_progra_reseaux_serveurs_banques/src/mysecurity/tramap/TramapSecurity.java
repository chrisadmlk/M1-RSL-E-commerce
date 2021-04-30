package mysecurity.tramap;

public interface TramapSecurity {
    byte[] encrypt(byte[] input);
    byte[] decrypt(byte[] input);
    // Demander si ici "authenticate" correspond bien à "sign" point de vue lexical
    byte[] authenticate(byte[] message);
    // Verify HMAC or signature
    boolean verifyAuthentication(byte[] message, byte[] authentication);
}
