package service;

import dataAccess.AuthDAO;

public class AuthService {

    private AuthDAO authDAO;

    public AuthService(AuthDAO authDAO){
        this.authDAO=authDAO;
    }

    public boolean isAuth(String authToken, String username){
        return false;
    }


}
