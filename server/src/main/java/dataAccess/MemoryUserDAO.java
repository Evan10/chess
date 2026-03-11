package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;


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
            throw new UnavailableRequestException("Error: Username in use");
        }
        allUserData.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (!allUserData.containsKey(username)) {
            throw new InvalidRequestException("Error: User not found");
        }
        return allUserData.get(username);
    }

}
