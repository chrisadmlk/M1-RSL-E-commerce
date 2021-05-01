package client_ACS;

import common.ObjTransformer;
import mysecurity.utils.HashedObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class AuthClientRequest implements Serializable {
    private String name;
    private String dateRequest;
    private HashedObject digest;
    private byte[] signature;

    public AuthClientRequest() {}

    public AuthClientRequest(String name, String pin) {
        // Name
        this.name = name;
        // Date
        dateRequest = getCurrentDate();
        // Digest with pin
        digest = createDigest(pin);
    }

    private String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return format.format(new Date(System.currentTimeMillis()));
    }

    public HashedObject createDigest(String pin) {
        return new HashedObject(
                ObjTransformer.ObjToByteArray(name + dateRequest + pin),
                "SHA1"
        );
    }

    public byte[] gatherInfos() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(getName().getBytes());
        output.write(getDateRequest().getBytes());
        output.write(getDigest().getBytes());
        return output.toByteArray();
    }

    @Override
    public String toString() {
        return "AuthClientRequest{" +
                "name='" + name + '\'' +
                ", dateRequest='" + dateRequest + '\'' +
                ", digest=" + new String(digest.getBytes()) +
                ", signature=" + new String(signature) +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateRequest() {
        return dateRequest;
    }

    public void setDateRequest(String dateRequest) {
        this.dateRequest = dateRequest;
    }

    public HashedObject getDigest() {
        return digest;
    }

    public void setDigest(HashedObject digest) {
        this.digest = digest;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}
