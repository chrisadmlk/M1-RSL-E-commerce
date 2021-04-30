package common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    public static Properties propertyloader(String filename) throws IOException {
        Properties properties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream fileProperties = classLoader.getResourceAsStream(filename);
        if (fileProperties == null) {
            throw new FileNotFoundException("Erreur ouverture du fichier properties");
        }
        properties.load(fileProperties);
        return properties;
    }
}
