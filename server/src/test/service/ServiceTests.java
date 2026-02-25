package service;

import dataAccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import requestResult.*;
import util.Constants;
import util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ServiceTests {

    private static UserData existingUser;
    private static String existingUserAuth;
    private static UserData nonExistingUser;

    private static AuthService authService;
    private static ClearApplicationService clearApplicationService;
    private static GameService gameService;
    private static UserService userService;

    private static UserDAO userDAO;
    private static GameDAO gameDAO;
    private static AuthDAO authDAO;
    private static Collection<DAO> allDAOs;

    @BeforeAll
    static void setup(){
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        allDAOs = List.of(userDAO,gameDAO,authDAO);

        authService = new AuthService(authDAO);
        clearApplicationService = new ClearApplicationService(allDAOs);
        gameService = new GameService(gameDAO);
        userService = new UserService(userDAO,authDAO);

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
    }

    @Test
    @Order(6)
    @DisplayName("JoinGameSuccess")
    public void joinGameSuccess(){
    }

    @Test
    @Order(7)
    @DisplayName("JoinGameSpotTaken")
    public void joinGameSpotTaken(){
    }

    @Test
    @Order(8)
    @DisplayName("CreateGameSuccess")
    public void createGameSuccess(){
    }

    @Test
    @Order(9)
    @DisplayName("CreateGameNoAuth")
    public void createGameNoAuth(){
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
