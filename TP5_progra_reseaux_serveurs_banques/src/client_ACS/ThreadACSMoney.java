package client_ACS;

import common.BeanAccessOracle;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

// Thread qui correspond à la partie MONEY -> Il enverra la demande au PORT_REQPAY de ACQ

// Sera en SSL !!!

public class ThreadACSMoney extends Thread{
    private Socket socket;
    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;
    private BeanAccessOracle beanOracle;

    public ThreadACSMoney(Socket workSocket) {
        this.socket = workSocket;
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
            beanOracle = new BeanAccessOracle("ACS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("*MONEY*-> Lancement ThreadMoney n° : " + Thread.currentThread().getName());

        if(socket != null) {
            while(!socket.isClosed()) try{
                String request = reader.readUTF();
                System.out.println("*MONEY* -> " + currentThread().getName() + " - Type de requete : " + request);
                if(request.equals("MONEY")){
                    // Gestion du payment

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
