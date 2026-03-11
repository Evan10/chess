package dataaccess;

import util.ReadProperties;

import java.io.IOException;
import java.util.Properties;

public class DAOFactory {

    private static Properties props;

    static {
        loadProps();
    }

    private static boolean useDatabase() {
        if (props.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(props.getProperty("useDatabase"));
    }

    private static void loadProps() {
        try {
            props = ReadProperties.readPropertiesFile("server.properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static UserDAO getUserDAO() {
        return useDatabase() ? new DatabaseUserDAO() : new MemoryUserDAO();
    }

    public static GameDAO getGameDAO() {
        return useDatabase() ? new DatabaseGameDAO() : new MemoryGameDAO();
    }

    public static AuthDAO getAuthDAO() {
        return useDatabase() ? new DatabaseAuthDAO() : new MemoryAuthDAO();
    }

}
