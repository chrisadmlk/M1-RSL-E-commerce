package client_ACS;

import java.io.Serializable;

public class AuthServerResponse implements Serializable {
    private String bankName;
    private String clientName;
    private int serialNumber;
    private byte[] signature;

    public AuthServerResponse(String bankName, String clientName, int serialNumber, byte[] signature) {
        this.bankName = bankName;
        this.clientName = clientName;
        this.serialNumber = serialNumber;
        this.signature = signature;
    }
}

