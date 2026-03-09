package dataaccess;

import model.AuthData;

import java.sql.Connection;

public class DatabaseAuthDAO implements AuthDAO{

    private final Connection connection;

    public DatabaseAuthDAO() {
        try {
            this.connection = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData getAuthDataWithAuthToken(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void addAuthData(AuthData userData) throws DataAccessException {

    }

    @Override
    public void removeAuthData(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
