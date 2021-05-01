package mysecurity.tramap;

public interface TramapSecurity {
    byte[] encrypt(byte[] input);
    byte[] decrypt(byte[] input);
    // Create HMAC or signature
    byte[] authenticate(byte[] message);
    // Verify HMAC or signature
    boolean verifyAuthentication(byte[] message, byte[] authentication);
}
