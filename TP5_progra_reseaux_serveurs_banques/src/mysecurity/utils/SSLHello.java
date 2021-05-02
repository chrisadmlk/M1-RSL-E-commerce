package mysecurity.utils;

import common.CurrentDate;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class SSLHello implements Serializable {
    private final String version = "1.0";
    private byte[] rnd_cli;
    private byte[] rnd_serv;
    private String date;
    private int sessionId;
    private List<String> cipherSuite; // Empty for now they always use RSA

    public static final int CLIENT = 0;
    public static final int SERVER = 1;

    public SSLHello(int type) {
        if(type == CLIENT){
            this.rnd_cli = generate(28);
            this.rnd_serv = null;
            sessionId = 0;
        }
        else if(type == SERVER){
            this.rnd_cli = null;
            this.rnd_serv = generate(28);
            sessionId = (int) (Math.random() * 100);
        }
        this.date = CurrentDate.get();
    }

    public byte[] generate(int size){
        Random random = new Random();
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] getRnd_cli() {
        return rnd_cli;
    }

    public void setRnd_cli(byte[] rnd_cli) {
        this.rnd_cli = rnd_cli;
    }

    public byte[] getRnd_serv() {
        return rnd_serv;
    }

    public void setRnd_serv(byte[] rnd_serv) {
        this.rnd_serv = rnd_serv;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getCipherSuite() {
        return cipherSuite;
    }

    public void setCipherSuite(List<String> cipherSuite) {
        this.cipherSuite = cipherSuite;
    }


}
