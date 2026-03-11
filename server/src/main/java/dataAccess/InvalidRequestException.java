package dataaccess;

public class InvalidRequestException extends DataAccessException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
