package client;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.Collection;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static ClientSessionData sessionData;
    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        sessionData = new ClientSessionData();
        facade = new ServerFacade("localhost",port,sessionData);
    }

    @BeforeEach
    public void setup(){
        try {
            facade.clearDatabase();
            facade.logout();
        } catch (FailResponseCodeException e) {
            System.out.println("User was not logged in");
        }
    }

    @AfterAll
    static void stop() {
        facade.close();
        server.stop();
    }


    @Test
    public void shouldLogUserInWhenRegistered() {
        String username= "Test";
        String password = "Test";
        try {
            sessionData.setAuthData(facade.register(username,password,"Test"));
            facade.logout();
        } catch (FailResponseCodeException e) {
            Assertions.fail(e);
        }
        Assertions.assertDoesNotThrow(()->facade.login(username, password));
    }

    @Test
    public void shouldFailLogUserInWhenNotRegistered() {
        Assertions.assertThrows(FailResponseCodeException.class,
                ()->facade.login("nonexistantusername", "nonexistantpassword"));
    }

    @Test
    public void shouldRegisterUserWhenAvailable(){
        String username= "Test";
        String password = "Test";
        try {
            model.AuthData authData = facade.register(username, password, "Test");
            Assertions.assertNotNull(authData);
            Assertions.assertFalse(authData.authToken().isBlank());
        } catch (FailResponseCodeException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void shouldFailRegisterUserUsernameWhenUsernameInUse(){
        String username= "TestDoubleRegister";
        String password = "TestDoubleRegister";
        String email = "TestDoubleRegister";
        try {
            model.AuthData authData = facade.register(username, password, email);
            Assertions.assertNotNull(authData);
            Assertions.assertFalse(authData.authToken().isBlank());
        }   catch (FailResponseCodeException e) {
            Assertions.fail(e);
        }
        Assertions.assertThrows(FailResponseCodeException.class,
                ()->facade.register(username, password, email));
    }

    @Test
    public void shouldLogoutUserWhenLoggedIn(){
        String username= "Test";
        String password = "Test";
        try {
            sessionData.setAuthData(facade.register(username,password,"Test"));
            Assertions.assertDoesNotThrow(()->facade.logout());
        } catch (FailResponseCodeException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void shouldFailLogoutWhenNotLoggedIn(){
        Assertions.assertThrows(FailResponseCodeException.class,()->facade.logout());
    }

    @Test
    public void shouldListGamesWhenHasAuth(){
        String username= "Test";
        String password = "Test";
        int gameCount = 10;
        try {
            sessionData.setAuthData(facade.register(username,password,"Test"));
            generateRandomGames(gameCount);
            Collection<GameData> games = facade.getGameList();
            Assertions.assertEquals(gameCount, games.size());
        } catch (FailResponseCodeException e) {
            Assertions.fail(e);
        }
    }
    @Test
    public void shouldFailListGamesWhenNoAuth(){
        Assertions.assertThrows(FailResponseCodeException.class, ()->facade.getGameList());
    }

    @Test
    public void shouldCreateGameWhenHasAuth(){
        String username= "Test";
        String password = "Test";
        int gameCount = 10;
        try {
            sessionData.setAuthData(facade.register(username, password, "Test"));
            Assertions.assertDoesNotThrow(()->generateRandomGames(gameCount));
        } catch (FailResponseCodeException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void shouldFailCreateGameWhenNoAuth(){
        Assertions.assertThrows(FailResponseCodeException.class,
                ()->facade.createGame("GameNoAuth"));
    }

    @Test
    public void shouldJoinGameWhenHasAuth(){
        String username= "Test";
        String password = "Test";
        try {
            sessionData.setAuthData(facade.register(username, password, "Test"));
            String gameID = facade.createGame("GameName");
            Assertions.assertDoesNotThrow(()->facade.joinGame(gameID, ChessGame.TeamColor.WHITE));
        } catch (FailResponseCodeException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void shouldFailJoinGameWhenSpotTaken(){
        String username= "Test";
        String password = "Test";
        try {
            sessionData.setAuthData(facade.register(username, password, "Test"));
            String gameID = facade.createGame("GameName");
            Assertions.assertDoesNotThrow(()->facade.joinGame(gameID, ChessGame.TeamColor.WHITE));
            Assertions.assertThrows(FailResponseCodeException.class,
                    ()->facade.joinGame(gameID, ChessGame.TeamColor.WHITE));
        } catch (FailResponseCodeException e) {
            Assertions.fail(e);
        }
    }



    private void generateRandomGames(int n) throws FailResponseCodeException {
        for(int i = 0 ; i < n; i++){
            facade.createGame("Game"+i);
        }
    }
}
