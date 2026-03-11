package service;

import dataaccess.*;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.InvalidRequestException;
import dataaccess.exception.UnavailableRequestException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requestresult.*;
import util.Constants;
import util.Util;

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
        String passwordHash = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        try {
            UserData userData = new UserData(registerRequest.username(),
                    passwordHash, registerRequest.email());
            userDAO.addUser(userData);
        } catch (InvalidRequestException e) {
            return new RegisterResult(UNAUTHORIZED, e.getMessage());
        } catch (UnavailableRequestException e) {
            return new RegisterResult(FORBIDDEN, e.getMessage());
        } catch (DataAccessException e) {
            return new RegisterResult(SERVER_ERROR, e.getMessage());
        }

        String authToken = Util.newUUID();
        AuthData authData = new AuthData(authToken, registerRequest.username());
        try {
            authDAO.addAuthData(authData);
        } catch (InvalidRequestException e) {
            return new RegisterResult(UNAUTHORIZED, e.getMessage());
        } catch (UnavailableRequestException e) {
            return new RegisterResult(FORBIDDEN, e.getMessage());
        } catch (DataAccessException e) {
            return new RegisterResult(SERVER_ERROR, e.getMessage());
        }
        return new RegisterResult(Constants.OK, null, registerRequest.username(), authToken);

    }

    public LoginResult login(LoginRequest loginRequest) {
        try {
            UserData userData = userDAO.getUser(loginRequest.username());
            boolean correctPassword = BCrypt.checkpw(loginRequest.password(), userData.password());
            if (!correctPassword) {
                return new LoginResult(Constants.UNAUTHORIZED, "Error: unauthorized");
            }
        } catch (InvalidRequestException e) {
            return new LoginResult(UNAUTHORIZED, e.getMessage());
        } catch (DataAccessException e) {
            return new LoginResult(SERVER_ERROR, e.getMessage());
        }

        String authToken = Util.newUUID();
        AuthData authData = new AuthData(authToken, loginRequest.username());
        try {
            authDAO.addAuthData(authData);
        } catch (InvalidRequestException e) {
            return new LoginResult(UNAUTHORIZED, e.getMessage());
        } catch (DataAccessException e) {
            return new LoginResult(SERVER_ERROR, e.getMessage());
        }

        return new LoginResult(Constants.OK, null, loginRequest.username(), authToken);
    }


    public LogoutResult logout(LogoutRequest logoutRequest) {
        try {
            authDAO.removeAuthData(logoutRequest.authData().authToken());
        } catch (InvalidRequestException e) {
            return new LogoutResult(UNAUTHORIZED, e.getMessage());
        } catch (DataAccessException e) {
            return new LogoutResult(SERVER_ERROR, e.getMessage());
        }
        return new LogoutResult(Constants.OK, "");
    }


}
