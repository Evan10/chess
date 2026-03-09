package dataaccess;

import model.AuthData;

import java.sql.Connection;

public class DatabaseAuthDAO implements AuthDAO{

    private final Connection connection;

    public DatabaseAuthDAO(Connection connection) {
        this.connection = connection;
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
