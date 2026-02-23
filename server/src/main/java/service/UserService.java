package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;
import requestResult.*;
import util.Util;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    public UserService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO=authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        try{
            UserData userData = new UserData(registerRequest.username(),
                    registerRequest.password(),registerRequest.email());
            userDAO.addUser(userData);
        }catch (DataAccessException e){
            return new RegisterResult(403,e.getMessage());
        }
        String authToken = Util.newUUID();
        AuthData authData = new AuthData(authToken,registerRequest.username());
        try {
            authDAO.addAuthData(authData);
        } catch (DataAccessException e) {
            return new RegisterResult(500,e.getMessage());
        }
        return new RegisterResult(200,null,registerRequest.username(),authToken);

    }
    public LoginResult login(LoginRequest loginRequest) {
        try {
            UserData userData = userDAO.getUser(loginRequest.username());
            boolean correctPassword = userData.password().equals(loginRequest.password());
            if(!correctPassword){
                return new LoginResult(401,"Error: unauthorized");
            }
        } catch (DataAccessException e) {
            return new LoginResult(401, e.getMessage());
        }

        String authToken = Util.newUUID();
        AuthData authData = new AuthData(authToken,loginRequest.username());
        try {
            authDAO.addAuthData(authData);
        } catch (DataAccessException e) {
            return new LoginResult(500,e.getMessage());
        }
        return new LoginResult(200,null,loginRequest.username(),authToken);
    }


    public LogoutResult logout(LogoutRequest logoutRequest) {
        if(logoutRequest.authData().authToken().isBlank())
            return new LogoutResult(401, "Error: unauthorized");

        try {
            authDAO.removeAuthData(logoutRequest.authData().authToken());
        } catch (DataAccessException e) {
            return new LogoutResult(400,e.getMessage());
        }

        return new LogoutResult(200,"");
    }
}
