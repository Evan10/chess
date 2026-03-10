package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

import static dataaccess.DataAccessException.INVALID_REQUEST_ERROR;

public class MemoryAuthDAO implements AuthDAO {

    private final Map<String, AuthData> data;

    protected MemoryAuthDAO() {
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
            throw new DataAccessException("Error: Token not found", INVALID_REQUEST_ERROR);
        }
        return data.get(authToken);
    }

    @Override
    public void addAuthData(AuthData userData) throws DataAccessException {
        if (data.containsKey(userData.authToken())) {
            throw new DataAccessException("Error: Auth Token already in use", INVALID_REQUEST_ERROR);
        }
        data.put(userData.authToken(), userData);
    }

    @Override
    public void removeAuthData(String authToken) {
        data.remove(authToken);
    }

}
