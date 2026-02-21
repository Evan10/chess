package service;

import dataAccess.AuthDAO;
import kotlin.Pair;
import model.AuthData;
import model.UserData;

import java.util.Optional;


public class AuthService {

    private AuthDAO authDAO;

    public AuthService(AuthDAO authDAO){
        this.authDAO=authDAO;
    }

    public AuthData isAuth(String authToken){

        return new AuthData(authToken,"");
    }


}
