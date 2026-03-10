package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception {
    public static final int UNKNOWN_ERROR = 0;
    public static final int DATABASE_ERROR = 1;
    public static final int INVALID_REQUEST_ERROR = 2;
    public static final int UNAVAILABLE_REQUEST_ERROR = 3;

    public final int reason;

    public DataAccessException(String message) {
        super(message);
        reason = UNKNOWN_ERROR;
    }

    public DataAccessException(String message, int reason) {
        super(message);
        this.reason=reason;
    }

    public DataAccessException(String message, Throwable ex) {
        super(message, ex);
        reason = UNKNOWN_ERROR;
    }
}
