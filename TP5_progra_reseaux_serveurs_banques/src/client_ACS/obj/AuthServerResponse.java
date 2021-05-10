package client_ACS.obj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class AuthServerResponse implements Serializable {
    private String bankName;
    private String clientName;
    private int serialNumber;
    private byte[] signature = null;

    public AuthServerResponse(String bankName, String clientName, int serialNumber, byte[] signature) {
        this.bankName = bankName;
        this.clientName = clientName;
        this.serialNumber = serialNumber;
        this.signature = signature;
    }

    public AuthServerResponse(String bankName, String clientName, int serialNumber) {
        this.bankName = bankName;
        this.clientName = clientName;
        this.serialNumber = serialNumber;
    }


    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] concatForSignature(){
        String tmp = bankName + clientName + serialNumber;
        return tmp.getBytes();
    }

    
    public byte[] gatherInfos() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(getBankName().getBytes());
        output.write(getClientName().getBytes());
        output.write(getSerialNumber());
        return output.toByteArray();
    }
    

    @Override
    public String toString() {
        return "AuthServerResponse{" +
                "bankName='" + bankName + '\'' +
                ", clientName='" + clientName + '\'' +
                ", serialNumber=" + serialNumber +
                '}';
    }
}

