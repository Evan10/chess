package service;

import dataAccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import requestResult.*;
import util.Constants;
import util.Util;

import java.util.*;

public class ServiceTests {

    private static UserData existingUser;
    private static String existingUserAuth;
    private static UserData nonExistingUser;

    private static AuthService authService;
    private static ClearApplicationService clearApplicationService;
    private static GameService gameService;
    private static UserService userService;

    @BeforeAll
    static void setup(){
        UserDAO userDAO = new MemoryUserDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        Collection<DAO> allDAOs = List.of(userDAO, gameDAO, authDAO);

        authService = new AuthService(authDAO);
        clearApplicationService = new ClearApplicationService(allDAOs);
        gameService = new GameService(gameDAO);
        userService = new UserService(userDAO, authDAO);

        existingUser = new UserData("bobby","securePassword","email@gmail.com");
        nonExistingUser = new UserData("notBobby","badPassword","notEmail@gmail.com");
    }

    @BeforeEach
    void reset(){
        clearApplicationService.clear(new ClearApplicationRequest());

        RegisterRequest req = new RegisterRequest(existingUser.username(),
                existingUser.password(),existingUser.email());
        RegisterResult res = userService.register(req);
        existingUserAuth = res.authToken();
    }

    @Test
    @Order(1)
    @DisplayName("AuthTokenStoredCorrect")
    public void authCorrectlySaved(){
        AuthData authData = authService.getAuth(existingUserAuth);
        Assertions.assertNotNull(authData);
        Assertions.assertTrue(authData.isValid(), "AuthToken was valid but was not accepted");
        Assertions.assertEquals(authData.username(),existingUser.username(),
                "AuthData returned with incorrect username");
    }

    @Test
    @Order(2)
    @DisplayName("AuthTokenNotFound")
    public void authTokenNotFound(){
        String fakeAuth = Util.newUUID();
        AuthData authData = authService.getAuth(fakeAuth);
        Assertions.assertNull(authData,"Invalid authToken was accepted but should have return null");
    }

    @Test
    @Order(3)
    @DisplayName("ClearAppWorksCorrectly")
    public void clearAppCorrectly(){
        AuthData authData = authService.getAuth(existingUserAuth);
        CreateGameRequest createGameReq = new CreateGameRequest("Game", authData);
        gameService.createGame(createGameReq);
        Assertions.assertFalse(clearApplicationService.areDAOsEmpty(),"Data Access Objects were " +
                "empty when they should have had data in them");

        ClearApplicationRequest clearAppReq = new ClearApplicationRequest();
        clearApplicationService.clear(clearAppReq);

        Assertions.assertTrue(clearApplicationService.areDAOsEmpty(),"The clear request did not " +
                "properly clear the data");

    }

    @Test
    @Order(4)
    @DisplayName("ListGamesAuth")
    public void listGamesWithAuth(){
        int gameCount = 10;
        AuthData authData = authService.getAuth(existingUserAuth);
        addGamesToList(authData,gameCount);

        ListGamesRequest req = new ListGamesRequest(authData);
        ListGamesResult res = gameService.listGames(req);

        Assertions.assertEquals(Constants.OK, res.responseCode(), "Error message was returned when " +
                "it should have succeeded");
        Assertions.assertEquals(gameCount, res.games().size(),"Incorrect number of games returned");


    }

    @Test
    @Order(5)
    @DisplayName("ListGamesNoAuth")
    public void listGamesNoAuth(){
        int gameCount = 10;
        AuthData authData = authService.getAuth(existingUserAuth);
        addGamesToList(authData,gameCount);

        String fakeAuth = Util.newUUID();
        AuthData invalidAuth = new AuthData(fakeAuth, "fakeUser");
        ListGamesRequest req = new ListGamesRequest(invalidAuth);
        ListGamesResult res = gameService.listGames(req);
        Assertions.assertEquals(Constants.UNAUTHORIZED,res.responseCode(), "Auth should have been invalid");
        Assertions.assertEquals(0,res.games().size(), "Games were returned even though Auth was invalid");
    }

    @Test
    @Order(6)
    @DisplayName("JoinGameSuccess")
    public void joinGameSuccess(){
        AuthData authData = authService.getAuth(existingUserAuth);
        CreateGameResult createRes = addGamesToList(authData,1).stream().toList().getFirst();

        String gameID = createRes.gameID();

        JoinGameRequest joinReq = new JoinGameRequest(chess.Constants.BLACK_TEAM, gameID, authData);
        JoinGameResult joinRes = gameService.joinGame(joinReq);
        Assertions.assertEquals(Constants.OK,joinRes.responseCode());

        ListGamesRequest listReq = new ListGamesRequest(authData);
        ListGamesResult listRes = gameService.listGames(listReq);
        GameData gameData = listRes.games().stream()
                .filter((gd)->gd.gameID().equals(gameID))
                .toList().getFirst();

        Assertions.assertEquals(authData.username(),gameData.blackUsername(),
                "User was not selected as black player");

    }

    @Test
    @Order(7)
    @DisplayName("JoinGameSpotTaken")
    public void joinGameSpotTaken(){

        AuthData authData = authService.getAuth(existingUserAuth);
        CreateGameResult createRes = addGamesToList(authData,1).stream().toList().getFirst();

        String gameID = createRes.gameID();

        RegisterRequest registerReq = new RegisterRequest("SpotTaker","ITookSpot","spottaker@gmai.com");
        RegisterResult registerRes = userService.register(registerReq);
        AuthData spotTakerAuthData = new AuthData(registerRes.authToken(),registerRes.username());

        JoinGameRequest joinReq = new JoinGameRequest(chess.Constants.BLACK_TEAM, gameID, spotTakerAuthData);
        JoinGameResult joinRes = gameService.joinGame(joinReq);
        Assertions.assertEquals(Constants.OK,joinRes.responseCode());

        JoinGameRequest failJoinReq = new JoinGameRequest(chess.Constants.BLACK_TEAM, gameID, authData);
        JoinGameResult failJoinRes = gameService.joinGame(joinReq);
        Assertions.assertNotEquals(Constants.OK,failJoinRes.responseCode(),"Should have return an error");

        ListGamesRequest listReq = new ListGamesRequest(authData);
        ListGamesResult listRes = gameService.listGames(listReq);
        GameData gameData = listRes.games().stream()
                .filter((gd)->gd.gameID().equals(gameID))
                .toList().getFirst();

        Assertions.assertNotEquals(authData.username(),gameData.blackUsername(),
                "User should not be black player, but they are");

    }

    @Test
    @Order(8)
    @DisplayName("CreateGameSuccess")
    public void createGameSuccess(){
        AuthData authData = authService.getAuth(existingUserAuth);
        Collection<CreateGameResult> createResults = addGamesToList(authData,10);
        for(CreateGameResult res: createResults){
            Assertions.assertEquals(Constants.OK,res.responseCode(),
                    "Request should have returned a 200 response code");
            Assertions.assertFalse(res.gameID().isBlank(),
                    "Response was successful but no game id was provided");
        }

    }

    @Test
    @Order(9)
    @DisplayName("CreateGameNoAuth")
    public void createGameNoAuth(){
        String fakeAuthToken = Util.newUUID();
        AuthData fakseAuthData = new AuthData(fakeAuthToken, "fakeUser");
        Collection<CreateGameResult> createResults = addGamesToList(fakseAuthData,10);
        for(CreateGameResult res: createResults){
            Assertions.assertEquals(Constants.UNAUTHORIZED,res.responseCode(),
                    "Request should have returned a 401 response code");
            Assertions.assertTrue(res.message().contains("Error"),
                    "Message should have contained error code with the word \"Error\" in it");
        }
    }

    /// USER TESTS

    @Test
    @Order(10)
    @DisplayName("registerValid")
    public void registerValid(){

    }

    @Test
    @Order(11)
    @DisplayName("registerInvalidNoPassword")
    public void registerInvalidNoPassword(){
    }

    @Test
    @Order(12)
    @DisplayName("loginExistingUser")
    public void loginExistingUser(){
    }

    @Test
    @Order(13)
    @DisplayName("loginNonExistingUser")
    public void loginNonExistingUser(){
    }

    @Test
    @Order(14)
    @DisplayName("logoutSuccess")
    public void logoutSuccess(){
    }

    @Test
    @Order(15)
    @DisplayName("logoutNotLoggedIn")
    public void logoutNotLoggedIn(){
    }



    private Collection<CreateGameResult> addGamesToList(AuthData authData, int count){
        Collection<CreateGameResult> results = new ArrayList<>(count);
        for(int i = 0; i < count; i ++){
            String gameName = "Game_"+i;
            CreateGameRequest req = new CreateGameRequest(gameName,authData);
            results.add(gameService.createGame(req));
        }
        return results;
    }



}
