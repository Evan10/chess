package dataAccess;

import model.UserData;

public interface UserDAO extends DAO{
    boolean usernameInUse(String username);
    void addUser(UserData userData) throws DataAccessException;
    void updateUser(String username, UserData userData) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void deleteUser(String username) throws DataAccessException;
}
