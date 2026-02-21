package dataAccess;

import model.AuthData;

public interface AuthDAO extends DAO{
    public AuthData getAuthDataWithAuthToken(String authToken) throws DataAccessException;
    public void addAuthData(AuthData userData) throws DataAccessException;
    public void removeAuthData(String authToken) throws  DataAccessException;
}
