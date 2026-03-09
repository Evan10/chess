package dataaccess;


import model.UserData;

import java.sql.Connection;

public class DatabaseUserDAO implements UserDAO {
    private final Connection connection;

    public DatabaseUserDAO(Connection connection) {
        this.connection = connection;
    }


    @Override
    public boolean usernameInUse(String username) {
        return false;
    }

    @Override
    public void addUser(UserData userData) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
