package dataAccess;

import model.UserData;

public interface UserDAO extends DAO{

    public boolean usernameInUse(String username);
    public void addUser(UserData userData) throws DataAccessException;
    public void updateUser(String username, UserData userData) throws DataAccessException;
    public UserData getUser(String username) throws DataAccessException;
    public void deleteUser(String username) throws DataAccessException;
}
