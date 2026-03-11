package dataaccess;

public class UnavailableRequestException extends DataAccessException{
    public UnavailableRequestException(String message) {
        super(message);
    }
}
