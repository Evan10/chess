package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import kotlin.Pair;
import model.AuthData;
import model.UserData;

import java.util.Optional;


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
