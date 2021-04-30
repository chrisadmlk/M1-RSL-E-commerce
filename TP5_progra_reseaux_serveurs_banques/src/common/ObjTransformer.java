package common;

import java.io.*;

public class ObjTransformer {
    public static byte[] ObjToByteArray(Object obj){
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        byte[] result = null;
        try {
            objectOutputStream = new ObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            result = arrayOutputStream.toByteArray();
        } catch (IOException error) {
            System.out.println("! Error transforming Object to ByteArray " +
                    "----> returning null result !" + error);
        }
        finally {
            try {
                arrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Object ByteArrayToObj(byte[] array){
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(array);
        ObjectInputStream objectInputStream = null;
        Object obj = null;
        try{
            objectInputStream = new ObjectInputStream(arrayInputStream);
            obj = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException error){
            System.out.println("! Error transforming ByteArray to Object " +
                    "----> returning null result !" + error);
        }
        finally {
            if(objectInputStream != null){
                try {
                    arrayInputStream.close();
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
}
