package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {

    private final Map<String, AuthData> data;

    public MemoryAuthDAO() {
        data = new HashMap<>();
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public AuthData getAuthDataWithAuthToken(String authToken) throws DataAccessException {
        if (!data.containsKey(authToken)) {
            throw new DataAccessException("Token not found");
        }
        return data.get(authToken);
    }

    @Override
    public void addAuthData(AuthData userData) throws DataAccessException {
        if (data.containsKey(userData.authToken())) {
            throw new DataAccessException("Error: Auth Token already in use");
        }
        data.put(userData.authToken(), userData);
    }

    @Override
    public void removeAuthData(String authToken) throws DataAccessException {
        if (!data.containsKey(authToken)) {
            throw new DataAccessException("Error: User not found");
        }
        data.remove(authToken);
    }

}
