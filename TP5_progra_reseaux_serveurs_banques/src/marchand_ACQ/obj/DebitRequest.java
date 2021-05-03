package marchand_ACQ.obj;

import client_ACS.obj.AuthServerResponse;

import java.io.Serializable;

public class DebitRequest implements Serializable {
    private double debit;
    private AuthServerResponse authServer;

    public DebitRequest(double debit, AuthServerResponse authServer) {
        this.debit = debit;
        this.authServer = authServer;
    }

    public double getDebit() {
        return debit;
    }

    public void setDebit(double debit) {
        this.debit = debit;
    }

    public AuthServerResponse getAuthServer() {
        return authServer;
    }

    public void setAuthServer(AuthServerResponse authServer) {
        this.authServer = authServer;
    }
}
