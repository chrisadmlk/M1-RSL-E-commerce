package common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CurrentDate {
    public static String get() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return format.format(new Date(System.currentTimeMillis()));
    }
}
