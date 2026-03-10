package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requestresult.*;
import util.Constants;
import util.Util;

import static dataaccess.DataAccessException.INVALID_REQUEST_ERROR;
import static dataaccess.DataAccessException.UNAVAILABLE_REQUEST_ERROR;
import static util.Constants.*;

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
            return new RegisterResult(BAD_REQUEST, "Error: Request contains null field(s)");
        }

        if (ENFORCE_SECURE_PASSWORD) {
            Util.PasswordValidationResult passwordRes = Util.isValidPassword(registerRequest.password());
            if (!passwordRes.isValid()) {
                return new RegisterResult(BAD_REQUEST,
                        "Error: invalid password \n" + passwordRes.reason());
            }
        }
        String password_hash = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        try {
            UserData userData = new UserData(registerRequest.username(),
                    password_hash, registerRequest.email());
            userDAO.addUser(userData);
        } catch (DataAccessException e) {
            int errorCode = switch (e.reason){
                case INVALID_REQUEST_ERROR -> UNAUTHORIZED;
                case UNAVAILABLE_REQUEST_ERROR -> FORBIDDEN;
                default -> SERVER_ERROR;
            };
            return new RegisterResult(errorCode, e.getMessage());
        }
        String authToken = Util.newUUID();
        AuthData authData = new AuthData(authToken, registerRequest.username());
        try {
            authDAO.addAuthData(authData);
        } catch (DataAccessException e) {
            int errorCode = switch (e.reason){
                case INVALID_REQUEST_ERROR -> UNAUTHORIZED;
                case UNAVAILABLE_REQUEST_ERROR -> FORBIDDEN;
                default -> SERVER_ERROR;
            };
            return new RegisterResult(errorCode, e.getMessage());
        }
        return new RegisterResult(Constants.OK, null, registerRequest.username(), authToken);

    }

    public LoginResult login(LoginRequest loginRequest) {
        try {
            UserData userData = userDAO.getUser(loginRequest.username());
            boolean correctPassword = BCrypt.checkpw(loginRequest.password(),userData.password());
            if (!correctPassword) {
                return new LoginResult(Constants.UNAUTHORIZED, "Error: unauthorized");
            }
        } catch (DataAccessException e) {
            int errorCode = switch (e.reason){
                case INVALID_REQUEST_ERROR -> UNAUTHORIZED;
                case UNAVAILABLE_REQUEST_ERROR -> FORBIDDEN;
                default -> SERVER_ERROR;
            };
            return new LoginResult(errorCode, e.getMessage());
        }

        String authToken = Util.newUUID();
        AuthData authData = new AuthData(authToken, loginRequest.username());
        try {
            authDAO.addAuthData(authData);
        } catch (DataAccessException e) {
            int errorCode = e.reason== INVALID_REQUEST_ERROR? Constants.UNAUTHORIZED: SERVER_ERROR;
            return new LoginResult(errorCode, e.getMessage());
        }
        return new LoginResult(Constants.OK, null, loginRequest.username(), authToken);
    }


    public LogoutResult logout(LogoutRequest logoutRequest) {
        try {
            authDAO.removeAuthData(logoutRequest.authData().authToken());
        } catch (DataAccessException e) {
            int errorCode = e.reason== INVALID_REQUEST_ERROR? Constants.UNAUTHORIZED: SERVER_ERROR;
            return new LogoutResult(errorCode, e.getMessage());
        }
        return new LogoutResult(Constants.OK, "");
    }


}
