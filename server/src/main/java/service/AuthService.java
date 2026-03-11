package service;

import dataaccess.AuthDAO;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.DatabaseConnectivityException;
import model.AuthData;


public class AuthService {

    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        try {
            return authDAO.getAuthDataWithAuthToken(authToken);
        } catch (DatabaseConnectivityException e) {
            throw e;
        } catch (DataAccessException e) {
            return new AuthData(null, null);
        }
    }


}
