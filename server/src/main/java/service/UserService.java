package service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import requestResult.*;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    public UserService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO=authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        userDAO.


    }
    public LoginResult login(LoginRequest loginRequest) {
        return null;
    }
    public LogoutResult logout(LogoutRequest logoutRequest) {
        return null;
    }
}
