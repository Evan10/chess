package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import model.AuthData;



public class AuthService {

    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO){
        this.authDAO=authDAO;
    }

    public AuthData getAuth(String authToken){
        try {
            return authDAO.getAuthDataWithAuthToken(authToken);
        } catch (DataAccessException e) {
            return null;
        }
    }


}
