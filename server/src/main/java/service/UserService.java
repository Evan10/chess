package service;

import dataAccess.UserDAO;
import requestResult.*;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO){
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        return null;
    }
    public LoginResult login(LoginRequest loginRequest) {
        return null;
    }
    public LogoutResult logout(LogoutRequest logoutRequest) {
        return null;
    }
}
