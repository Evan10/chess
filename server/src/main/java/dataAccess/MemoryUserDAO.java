package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

import static dataaccess.DataAccessException.INVALID_REQUEST_ERROR;

public class MemoryUserDAO implements UserDAO {

    private final Map<String, UserData> allUserData;

    protected MemoryUserDAO() {
        allUserData = new HashMap<>();
    }

    @Override
    public void clear() {
        allUserData.clear();
    }

    @Override
    public boolean isEmpty() {
        return allUserData.isEmpty();
    }

    public boolean usernameInUse(String username) {
        return allUserData.containsKey(username);
    }

    @Override
    public void addUser(UserData userData) throws DataAccessException {
        if (usernameInUse(userData.username())) {
            throw new DataAccessException("Error: Username in use",INVALID_REQUEST_ERROR);
        }
        allUserData.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (!allUserData.containsKey(username)) {
            throw new DataAccessException("Error: User not found", INVALID_REQUEST_ERROR);
        }
        return allUserData.get(username);
    }

}
