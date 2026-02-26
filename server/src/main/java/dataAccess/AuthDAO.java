package dataaccess;

import model.AuthData;

public interface AuthDAO extends DAO {
    AuthData getAuthDataWithAuthToken(String authToken) throws DataAccessException;

    void addAuthData(AuthData userData) throws DataAccessException;

    void removeAuthData(String authToken) throws DataAccessException;
}
