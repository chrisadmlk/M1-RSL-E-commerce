package mysecurity.utils;

import java.io.Serializable;

public class TransferObject implements Serializable {
    private byte[] bytes;
    public byte[] getBytes() {
        return bytes;
    }
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public TransferObject(byte[] bytes) {
        this.bytes = bytes;
    }
}
