package test;

import client_ACS.ServerACS;
import marchand_ACQ.ServerBankACQ;

public class TestSSL {
    public static void main(String[] args) {
        ServerACS serverACS = new ServerACS();
        serverACS.startServer();
        ServerBankACQ serverBankACQ = new ServerBankACQ();
        serverBankACQ.startServer();

    }
}
