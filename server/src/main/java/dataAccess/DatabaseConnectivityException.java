package dataaccess;

public class DatabaseConnectivityException extends DataAccessException {
    public DatabaseConnectivityException(String message) {
        super(message);
    }

    public DatabaseConnectivityException(String message, Throwable ex) {
        super(message, ex);
    }
}
