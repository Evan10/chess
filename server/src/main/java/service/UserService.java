package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import requestresult.*;
import util.Constants;
import util.Util;

public class UserService {

    private static final boolean ENFORCE_SECURE_PASSWORD = false;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        if (registerRequest.containsNullField()) {
            return new RegisterResult(Constants.BAD_REQUEST, "Error: Request contains null field(s)");
        }

        if (ENFORCE_SECURE_PASSWORD) {
            Util.PasswordValidationResult passwordRes = Util.isValidPassword(registerRequest.password());
            if (!passwordRes.isValid()) {
                return new RegisterResult(Constants.BAD_REQUEST,
                        "Error: invalid password \n" + passwordRes.reason());
            }
        }

        try {
            UserData userData = new UserData(registerRequest.username(),
                    registerRequest.password(), registerRequest.email());
            userDAO.addUser(userData);
        } catch (DataAccessException e) {
            return new RegisterResult(Constants.FORBIDDEN, e.getMessage());
        }
        String authToken = Util.newUUID();
        AuthData authData = new AuthData(authToken, registerRequest.username());
        try {
            authDAO.addAuthData(authData);
        } catch (DataAccessException e) {
            return new RegisterResult(Constants.SERVER_ERROR, e.getMessage());
        }
        return new RegisterResult(Constants.OK, null, registerRequest.username(), authToken);

    }

    public LoginResult login(LoginRequest loginRequest) {
        try {
            UserData userData = userDAO.getUser(loginRequest.username());
            boolean correctPassword = userData.password().equals(loginRequest.password());
            if (!correctPassword) {
                return new LoginResult(Constants.UNAUTHORIZED, "Error: unauthorized");
            }
        } catch (DataAccessException e) {
            return new LoginResult(Constants.UNAUTHORIZED, e.getMessage());
        }

        String authToken = Util.newUUID();
        AuthData authData = new AuthData(authToken, loginRequest.username());
        try {
            authDAO.addAuthData(authData);
        } catch (DataAccessException e) {
            return new LoginResult(Constants.SERVER_ERROR, e.getMessage());
        }
        return new LoginResult(Constants.OK, null, loginRequest.username(), authToken);
    }


    public LogoutResult logout(LogoutRequest logoutRequest) {
        try {
            authDAO.removeAuthData(logoutRequest.authData().authToken());
        } catch (DataAccessException e) {
            return new LogoutResult(Constants.UNAUTHORIZED, e.getMessage());
        }
        return new LogoutResult(Constants.OK, "");
    }
}
