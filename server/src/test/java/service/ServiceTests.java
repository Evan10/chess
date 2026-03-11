package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import requestresult.*;
import util.Constants;
import util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ServiceTests {

    private static UserData existingUser;
    private static String existingUserAuth;
    private static UserData nonExistingUser;

    private static AuthService authService;
    private static ClearApplicationService clearApplicationService;
    private static GameService gameService;
    private static UserService userService;

    @BeforeAll
    static void setup() {
        UserDAO userDAO = DAOFactory.getUserDAO();;
        GameDAO gameDAO = DAOFactory.getGameDAO();;
        AuthDAO authDAO = DAOFactory.getAuthDAO();;
        Collection<DAO> allDAOs = List.of(userDAO, gameDAO, authDAO);

        authService = new AuthService(authDAO);
        clearApplicationService = new ClearApplicationService(allDAOs);
        gameService = new GameService(gameDAO);
        userService = new UserService(userDAO, authDAO);

        existingUser = new UserData("bobby", "securePassword", "email@gmail.com");
        nonExistingUser = new UserData("notBobby", "badPassword", "notEmail@gmail.com");
    }

    @BeforeEach
    void reset() {
        clearApplicationService.clear(new ClearApplicationRequest());

        RegisterRequest req = new RegisterRequest(existingUser.username(),
                existingUser.password(), existingUser.email());
        RegisterResult res = userService.register(req);
        existingUserAuth = res.authToken();
    }

    @Test
    @Order(1)
    @DisplayName("AuthTokenStoredCorrect")
    public void authCorrectlySaved() {
        AuthData authData = getAuthDataOrNull(existingUserAuth);

         Assertions.assertNotNull(authData);
        Assertions.assertTrue(authData.isValid(), "AuthToken was valid but was not accepted");
        Assertions.assertEquals(authData.username(), existingUser.username(),
                "AuthData returned with incorrect username");
    }

    @Test
    @Order(2)
    @DisplayName("AuthTokenNotFound")
    public void authTokenNotFound() {
        String fakeAuth = Util.newUUID();
        AuthData authData = getAuthDataOrNull(fakeAuth);
        assertNullOrEmpty(authData);
        //Assertions.assertNull(authData, "Invalid authToken was accepted but should have return null");
    }

    @Test
    @Order(3)
    @DisplayName("ClearAppWorksCorrectly")
    public void clearAppCorrectly() {
        AuthData authData = getAuthDataOrNull(existingUserAuth);
        CreateGameRequest createGameReq = new CreateGameRequest("Game", authData);
        gameService.createGame(createGameReq);
        Assertions.assertFalse(clearApplicationService.areDAOsEmpty(), "Data Access Objects were " +
                "empty when they should have had data in them");

        ClearApplicationRequest clearAppReq = new ClearApplicationRequest();
        clearApplicationService.clear(clearAppReq);

        Assertions.assertTrue(clearApplicationService.areDAOsEmpty(), "The clear request did not " +
                "properly clear the data");

    }

    @Test
    @Order(4)
    @DisplayName("ListGamesAuth")
    public void listGamesWithAuth() {
        int gameCount = 10;
        AuthData authData = getAuthDataOrNull(existingUserAuth);
        addGamesToList(authData, gameCount);

        ListGamesRequest req = new ListGamesRequest(authData);
        ListGamesResult res = gameService.listGames(req);

        Assertions.assertEquals(Constants.OK, res.responseCode(), "Error message was returned when " +
                "it should have succeeded");
        Assertions.assertEquals(gameCount, res.games().size(), "Incorrect number of games returned");
    }

    @Test
    @Order(5)
    @DisplayName("listGamesEmpty")
    public void listGamesEmpty() {
        AuthData authData = getAuthDataOrNull(existingUserAuth);
        ListGamesRequest req = new ListGamesRequest(authData);
        ListGamesResult res = gameService.listGames(req);
        Assertions.assertTrue(res.games().isEmpty(), "There should be no games to list");
    }

    @Test
    @Order(6)
    @DisplayName("JoinGameSuccess")
    public void joinGameSuccess() {
        AuthData authData = getAuthDataOrNull(existingUserAuth);
        CreateGameResult createRes = addGamesToList(authData, 1).stream().toList().getFirst();

        String gameID = createRes.gameID();

        JoinGameRequest joinReq = new JoinGameRequest(chess.Constants.BLACK_TEAM, gameID, authData);
        JoinGameResult joinRes = gameService.joinGame(joinReq);
        Assertions.assertEquals(Constants.OK, joinRes.responseCode());

        ListGamesRequest listReq = new ListGamesRequest(authData);
        ListGamesResult listRes = gameService.listGames(listReq);
        GameData gameData = listRes.games().stream()
                .filter((gd) -> gd.gameID().equals(gameID))
                .toList().getFirst();

        Assertions.assertNotNull(authData, "AuthData returned null");
        Assertions.assertEquals(authData.username(), gameData.blackUsername(),
                "User was not selected as black player");

    }

    @Test
    @Order(7)
    @DisplayName("JoinGameSpotTaken")
    public void joinGameSpotTaken() {

        AuthData authData = getAuthDataOrNull(existingUserAuth);;
        CreateGameResult createRes = addGamesToList(authData, 1).stream().toList().getFirst();

        String gameID = createRes.gameID();

        RegisterRequest registerReq = new RegisterRequest("SpotTaker", "ITookSpot", "spottaker@gmai.com");
        RegisterResult registerRes = userService.register(registerReq);
        AuthData spotTakerAuthData = new AuthData(registerRes.authToken(), registerRes.username());

        JoinGameRequest joinReq = new JoinGameRequest(chess.Constants.BLACK_TEAM, gameID, spotTakerAuthData);
        JoinGameResult joinRes = gameService.joinGame(joinReq);
        Assertions.assertEquals(Constants.OK, joinRes.responseCode());

        JoinGameRequest failJoinReq = new JoinGameRequest(chess.Constants.BLACK_TEAM, gameID, authData);
        JoinGameResult failJoinRes = gameService.joinGame(failJoinReq);
        Assertions.assertNotEquals(Constants.OK, failJoinRes.responseCode(), "Should have return an error");

        ListGamesRequest listReq = new ListGamesRequest(authData);
        ListGamesResult listRes = gameService.listGames(listReq);
        GameData gameData = listRes.games().stream()
                .filter((gd) -> gd.gameID().equals(gameID))
                .toList().getFirst();

        Assertions.assertNotEquals(authData.username(), gameData.blackUsername(),
                "User should not be black player, but they are");

    }

    @Test
    @Order(8)
    @DisplayName("CreateGameSuccess")
    public void createGameSuccess() {
        AuthData authData = getAuthDataOrNull(existingUserAuth);;
        Collection<CreateGameResult> createResults = addGamesToList(authData, 10);
        for (CreateGameResult res : createResults) {
            Assertions.assertEquals(Constants.OK, res.responseCode(),
                    "Request should have returned a 200 response code");
            Assertions.assertFalse(res.gameID().isBlank(),
                    "Response was successful but no game id was provided");
        }

    }

    @Test
    @Order(9)
    @DisplayName("createGameInvalidName")
    public void createGameInvalidName() {
        AuthData authData = getAuthDataOrNull(existingUserAuth);;
        CreateGameRequest createReq = new CreateGameRequest("", authData);
        CreateGameResult res = gameService.createGame(createReq);
        Assertions.assertEquals(Constants.BAD_REQUEST, res.responseCode(),
                "Request should have returned a 400 response code");
        Assertions.assertTrue(res.message().contains("Error"),
                "Message should have contained error code with the word \"Error\" in it");

    }

    /// USER TESTS

    @Test
    @Order(10)
    @DisplayName("registerValid")
    public void registerValid() {
        RegisterRequest registerReq = new RegisterRequest(nonExistingUser.username(),
                nonExistingUser.password(), nonExistingUser.email());
        RegisterResult registerRes = userService.register(registerReq);

        Assertions.assertEquals(Constants.OK, registerRes.responseCode(), "Result should have return a 200 response code");
        Assertions.assertFalse(registerRes.authToken().isBlank(), "Result is missing an auth token");
        Assertions.assertFalse(registerRes.username().isBlank(), "Result is missing username");

        AuthData authData = getAuthDataOrNull(registerRes.authToken());;

        Assertions.assertNotNull(authData, "Auth data was null but was expected to have a value");
        Assertions.assertEquals(authData.username(), registerReq.username(), "Username returned by auth service was incorrect");
        Assertions.assertEquals(authData.authToken(), registerRes.authToken(), "Auth Token returned by auth service was incorrect");
    }

    @Test
    @Order(11)
    @DisplayName("registerInvalidNoPassword")
    public void registerInvalidNoPassword() {
        RegisterRequest registerReq = new RegisterRequest(nonExistingUser.username(),
                null, nonExistingUser.email());

        RegisterResult registerRes = userService.register(registerReq);

        Assertions.assertEquals(Constants.BAD_REQUEST, registerRes.responseCode(), "Result should return a 400 response code");
        Assertions.assertTrue(registerRes.message().contains("Error"), "Result should contain an error message containing the word \"Error\"");
        Assertions.assertNull(registerRes.username(), "Username should be null");
        Assertions.assertNull(registerRes.authToken(), "AuthToken should be null");

    }

    @Test
    @Order(12)
    @DisplayName("loginExistingUser")
    public void loginExistingUser() {
        LoginRequest loginReq = new LoginRequest(existingUser.username(), existingUser.password());
        LoginResult loginRes = userService.login(loginReq);

        Assertions.assertEquals(Constants.OK, loginRes.responseCode(), "Should return 200 response code");
        Assertions.assertNull(loginRes.message(), "Response shouldn't contain error message");

        Assertions.assertNotNull(loginRes.username(), "Username shouldn't be null");
        Assertions.assertNotNull(loginRes.authToken(), "Auth token shouldn't be null");

        Assertions.assertFalse(loginRes.authToken().isBlank(), "Response should return valid authToken");
        Assertions.assertFalse(loginRes.username().isBlank(), "Response should return valid username");

        AuthData authData = getAuthDataOrNull(loginRes.authToken());

        Assertions.assertNotNull(authData, "Auth data was null but was expected to have a value");
        Assertions.assertEquals(authData.authToken(), loginRes.authToken(), "Incorrect auth token was returned by auth service");
        Assertions.assertEquals(authData.username(), loginRes.username(), "Incorrect username was returned by auth service");
    }

    @Test
    @Order(13)
    @DisplayName("loginNonExistingUser")
    public void loginNonExistingUser() {
        LoginRequest loginReq = new LoginRequest(nonExistingUser.username(), nonExistingUser.password());
        LoginResult loginRes = userService.login(loginReq);

        Assertions.assertEquals(Constants.UNAUTHORIZED, loginRes.responseCode(),
                "Should return " + Constants.UNAUTHORIZED + " response code");
        Assertions.assertTrue(loginRes.message().contains("Error"), "Response should error message with word \"Error\"");
        Assertions.assertNull(loginRes.authToken(), "Response shouldn't return authToken");
        Assertions.assertNull(loginRes.username(), "Response shouldn't return username");
    }

    @Test
    @Order(14)
    @DisplayName("logoutSuccess")
    public void logoutSuccess() {
        AuthData authData = getAuthDataOrNull(existingUserAuth);
        LogoutRequest logoutReq = new LogoutRequest(authData);
        LogoutResult logoutRes = userService.logout(logoutReq);

        Assertions.assertNotNull(authData,"AuthData was null but was supposed to contain a value");
        Assertions.assertEquals(Constants.OK, logoutRes.responseCode(), "Logout should have been successful");

        AuthData loggedOutAuthData = getAuthDataOrNull(authData.authToken());
        assertNullOrEmpty(loggedOutAuthData);

    }

    @Test
    @Order(15)
    @DisplayName("logoutNotLoggedIn")
    public void logoutNotLoggedIn() {
        AuthData authData = getAuthDataOrNull(existingUserAuth);
        logoutSuccess(); // log out user
        LogoutRequest logoutReq = new LogoutRequest(authData);
        LogoutResult logoutRes = userService.logout(logoutReq);

        Assertions.assertEquals(Constants.UNAUTHORIZED, logoutRes.responseCode(),
                "User was already logged out and should have returned a " + Constants.UNAUTHORIZED + " response code");

        Assertions.assertTrue(logoutRes.message().contains("Error"), "Logout message should contain word \"Error\"");

    }


    private Collection<CreateGameResult> addGamesToList(AuthData authData, int count) {
        Collection<CreateGameResult> results = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String gameName = "Game_" + i;
            CreateGameRequest req = new CreateGameRequest(gameName, authData);
            results.add(gameService.createGame(req));
        }
        return results;
    }


    private AuthData getAuthDataOrNull(String authToken){
        try {
            return authService.getAuth(authToken);
        }catch (DataAccessException e){
            return null;
        }
    }

    /*
     * Asserts that an object is either null or that all non-primitive fields are null
     * due to the nature of primitives never being null and having default values they
     * are ignored. Static fields are ignored
     * It expects all non-primitive non-static fields to be null and will NOT recursively check objects
     *
     * */
    private void assertNullOrEmpty(Object obj) {
        if (obj != null) {
            Class<?> objClass = obj.getClass();
            List<Field> fields = getAllClassFields(objClass);
            for (Field f : fields) {
                Class<?> clazz = f.getType();
                if (clazz.isPrimitive() || Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                f.setAccessible(true);
                try {
                    Assertions.assertNull(f.get(obj), objClass.getName() + " was expected to be null or empty but was not");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private List<Field> getAllClassFields(Class<?> objClass) {
        Class<?> clazz = objClass;
        ArrayList<Field> fields = new ArrayList<>(Arrays.stream(objClass.getDeclaredFields()).toList());
        while (clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());
        }
        return fields;
    }

}
