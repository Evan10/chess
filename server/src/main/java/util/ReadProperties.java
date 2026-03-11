package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadProperties {


    public static Properties readPropertiesFile(String path) throws IOException {
        InputStream s = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        Properties properties = new Properties();
        properties.load(s);
        return properties;
    }

}
