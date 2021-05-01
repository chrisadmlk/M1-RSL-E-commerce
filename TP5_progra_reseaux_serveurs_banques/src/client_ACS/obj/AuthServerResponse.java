package client_ACS.obj;

import java.io.Serializable;
import java.util.Arrays;

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

    @Override
    public String toString() {
        return "AuthServerResponse{" +
                "bankName='" + bankName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", serialNumber=" + serialNumber +
                ", signature=" + new String(signature) +
                '}';
    }
}

