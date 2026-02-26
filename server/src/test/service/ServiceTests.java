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
        UserDAO userDAO = new MemoryUserDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
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
    @DisplayName("AuthTest")
    public void authTest() {
        AuthData authData = authService.getAuth(existingUserAuth);
        Assertions.assertNotNull(authData);
        Assertions.assertTrue(authData.isValid(), "AuthToken was valid but was not accepted");
        Assertions.assertEquals(authData.username(), existingUser.username(),
                "AuthData returned with incorrect username");

        String fakeAuth = Util.newUUID();
        AuthData fakeAuthData = authService.getAuth(fakeAuth);
        Assertions.assertNull(fakeAuthData, "Invalid authToken was accepted but should have return null");
    }

    @Test
    @Order(2)
    @DisplayName("ClearAppTest")
    public void clearAppCorrectly() {
        AuthData authData = authService.getAuth(existingUserAuth);
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
    @Order(3)
    @DisplayName("ListGameTest")
    public void listGamesTest() {
        int gameCount = 10;
        AuthData authData = authService.getAuth(existingUserAuth);
        addGamesToList(authData, gameCount);

        ListGamesRequest req = new ListGamesRequest(authData);
        ListGamesResult res = gameService.listGames(req);

        Assertions.assertEquals(Constants.OK, res.responseCode(), "Error message was returned when " +
                "it should have succeeded");
        Assertions.assertEquals(gameCount, res.games().size(), "Incorrect number of games returned");


        clearApplicationService.clear(new ClearApplicationRequest());
        ListGamesResult emptyRes = gameService.listGames(req);
        Assertions.assertTrue(emptyRes.games().isEmpty(), "There should be no games to list");
    }

    @Test
    @Order(4)
    @DisplayName("JoinGameTest")
    public void joinGameTest() {
        AuthData authData = authService.getAuth(existingUserAuth);
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

        Assertions.assertEquals(authData.username(), gameData.blackUsername(),
                "User was not selected as black player");

        //Spot Taken
        RegisterRequest registerRequest = new RegisterRequest(nonExistingUser.username(),
                nonExistingUser.password(),nonExistingUser.username());

        RegisterResult registerResult = userService.register(registerRequest);
        AuthData newUserAuthData = authService.getAuth(registerResult.authToken());

        JoinGameRequest failJoinReq = new JoinGameRequest(chess.Constants.BLACK_TEAM, gameID, newUserAuthData);
        JoinGameResult failJoinRes = gameService.joinGame(failJoinReq);
        Assertions.assertNotEquals(Constants.OK, failJoinRes.responseCode(), "Should have return an error");

        ListGamesRequest failListReq = new ListGamesRequest(authData);
        ListGamesResult failListRes = gameService.listGames(failListReq);
        GameData failGameData = failListRes.games().stream()
                .filter((gd) -> gd.gameID().equals(gameID))
                .toList().getFirst();

        Assertions.assertNotEquals(newUserAuthData.username(), failGameData.blackUsername(),
                "User should not be black player, but they are");

    }

    @Test
    @Order(5)
    @DisplayName("CreateGameTest")
    public void createGameTest() {
        AuthData authData = authService.getAuth(existingUserAuth);
        Collection<CreateGameResult> createResults = addGamesToList(authData, 10);
        for (CreateGameResult res : createResults) {
            Assertions.assertEquals(Constants.OK, res.responseCode(),
                    "Request should have returned a 200 response code");
            Assertions.assertFalse(res.gameID().isBlank(),
                    "Response was successful but no game id was provided");
        }

        CreateGameRequest createReq = new CreateGameRequest("", authData);
        CreateGameResult res = gameService.createGame(createReq);
        Assertions.assertEquals(Constants.BAD_REQUEST, res.responseCode(),
                "Request should have returned a 400 response code");
        Assertions.assertTrue(res.message().contains("Error"),
                "Message should have contained error code with the word \"Error\" in it");

    }

    /// USER TESTS

    @Test
    @Order(6)
    @DisplayName("registerTest")
    public void registerTest() {
        RegisterRequest registerReq = new RegisterRequest(nonExistingUser.username(),
                nonExistingUser.password(), nonExistingUser.email());
        RegisterResult registerRes = userService.register(registerReq);

        Assertions.assertEquals(Constants.OK, registerRes.responseCode(), "Result should have return a 200 response code");
        Assertions.assertFalse(registerRes.authToken().isBlank(), "Result is missing an auth token");
        Assertions.assertFalse(registerRes.username().isBlank(), "Result is missing username");

        AuthData authData = authService.getAuth(registerRes.authToken());

        Assertions.assertEquals(authData.username(), registerReq.username(), "Username returned by auth service was incorrect");
        Assertions.assertEquals(authData.authToken(), registerRes.authToken(), "Auth Token returned by auth service was incorrect");

        RegisterRequest failRegisterReq = new RegisterRequest(nonExistingUser.username(),
                null, nonExistingUser.email());
        RegisterResult failRegisterRes = userService.register(failRegisterReq);
        Assertions.assertEquals(Constants.BAD_REQUEST, failRegisterRes.responseCode(), "Result should return a 400 response code");
        Assertions.assertTrue(failRegisterRes.message().contains("Error"), "Result should contain an error message containing the word \"Error\"");
        Assertions.assertNull(failRegisterRes.username(), "Username should be null");
        Assertions.assertNull(failRegisterRes.authToken(), "AuthToken should be null");

    }

    @Test
    @Order(7)
    @DisplayName("loginTest")
    public void loginTest() {
        LoginRequest loginReq = new LoginRequest(existingUser.username(), existingUser.password());
        LoginResult loginRes = userService.login(loginReq);

        Assertions.assertEquals(Constants.OK, loginRes.responseCode(), "Should return 200 response code");
        Assertions.assertNull(loginRes.message(), "Response shouldn't contain error message");

        Assertions.assertNotNull(loginRes.username(), "Username shouldn't be null");
        Assertions.assertNotNull(loginRes.authToken(), "Auth token shouldn't be null");

        Assertions.assertFalse(loginRes.authToken().isBlank(), "Response should return valid authToken");
        Assertions.assertFalse(loginRes.username().isBlank(), "Response should return valid username");

        AuthData authData = authService.getAuth(loginRes.authToken());

        Assertions.assertEquals(authData.authToken(), loginRes.authToken(), "Incorrect auth token was returned by auth service");
        Assertions.assertEquals(authData.username(), loginRes.username(), "Incorrect username was returned by auth service");

        LoginRequest failLoginReq = new LoginRequest(nonExistingUser.username(), nonExistingUser.password());
        LoginResult failLoginRes = userService.login(failLoginReq);

        Assertions.assertEquals(Constants.UNAUTHORIZED, failLoginRes.responseCode(),
                "Should return " + Constants.UNAUTHORIZED + " response code");
        Assertions.assertTrue(failLoginRes.message().contains("Error"), "Response should error message with word \"Error\"");
        Assertions.assertNull(failLoginRes.authToken(), "Response shouldn't return authToken");
        Assertions.assertNull(failLoginRes.username(), "Response shouldn't return username");
    }

    @Test
    @Order(8)
    @DisplayName("logoutSuccess")
    public void logoutTest() {
        AuthData authData = authService.getAuth(existingUserAuth);
        LogoutRequest logoutReq = new LogoutRequest(authData);
        LogoutResult logoutRes = userService.logout(logoutReq);

        Assertions.assertEquals(Constants.OK, logoutRes.responseCode(), "Logout should have been successful");

        AuthData loggedOutAuthData = authService.getAuth(authData.authToken());
        assertNullOrEmpty(loggedOutAuthData);


        //call same request on now logged out user
        LogoutResult failLogoutRes = userService.logout(logoutReq);

        Assertions.assertEquals(Constants.UNAUTHORIZED, failLogoutRes.responseCode(),
                "User was already logged out and should have returned a " + Constants.UNAUTHORIZED + " response code");

        Assertions.assertTrue(failLogoutRes.message().contains("Error"), "Logout message should contain word \"Error\"");

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
