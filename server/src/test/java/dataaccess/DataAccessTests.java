package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.InvalidRequestException;
import dataaccess.exception.UnavailableRequestException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static util.Util.newIntID;
import static util.Util.newUUID;

public class DataAccessTests {

    private final static UserData EXISTING_USER =
            new UserData("Bobby", "qwerty123", "email@email.com");
    private final static UserData NONEXISTING_USER =
            new UserData("NotBobby", "notQwerty123", "NotEmail@email.com");
    private static AuthDAO authDAO;
    private static GameDAO gameDAO;
    private static UserDAO userDAO;

    @BeforeAll
    static void setup() {
        authDAO = DAOFactory.getAuthDAO();
        gameDAO = DAOFactory.getGameDAO();
        userDAO = DAOFactory.getUserDAO();
    }

    @BeforeEach
    public void reset() {
        try {
            authDAO.clear();
            gameDAO.clear();
            userDAO.clear();

            userDAO.addUser(EXISTING_USER);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(0)
    @DisplayName("Clear database test")
    void clearDatabaseTest() {
        addDataToDAOs();

        Assertions.assertFalse(allDAOsAreEmpty());
        try {
            authDAO.clear();
            gameDAO.clear();
            userDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(allDAOsAreEmpty());
    }


    @Test
    @Order(1)
    @DisplayName("Get game list")
    void gameListTest() {
        Collection<GameData> games = addToGameDAO();
        HashSet<GameData> gamesSet = new HashSet<>(games);
        try {
            Collection<GameData> gamesFromDAO = gameDAO.getGameList();
            HashSet<GameData> gamesFromDAOSet = new HashSet<>(gamesFromDAO);

            Assertions.assertEquals(gamesSet, gamesFromDAOSet);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Get game empty")
    void gameListEmptyTest() {
        try {
            Collection<GameData> gamesFromDAO = gameDAO.getGameList();
            Assertions.assertTrue(gamesFromDAO.isEmpty());
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }


    @Test
    @Order(3)
    @DisplayName("Get game")
    void getGameWithIDTest() {
        Collection<GameData> games = addToGameDAO();
        try {
            for (GameData gd : games) {
                GameData gamesFromDAO = gameDAO.getGame(gd.gameID());
                Assertions.assertEquals(gd, gamesFromDAO);
            }
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Get game nonexistent")
    void getGameNotFoundTest() {
        addToGameDAO();
        Assertions.assertThrows(DataAccessException.class, () -> gameDAO.getGame("FakeGameID"));
    }

    @Test
    @Order(5)
    @DisplayName("Create game success")
    void createGameTest() {
        GameData gameData = new GameData(Integer.toString(newIntID()),
                null, null,
                "game", new ChessGame());
        try {
            gameDAO.putGame(gameData);
            GameData game = gameDAO.getGame(gameData.gameID());
            Assertions.assertEquals(game, gameData);
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(6)
    @DisplayName("Update game")
    void updateGameSuccessTest() {
        List<String> usernames = addDataToDAOs().stream().toList();
        GameData gameData = new GameData(Integer.toString(newIntID()),
                null, null,
                "game", new ChessGame());
        try {
            gameDAO.putGame(gameData);
            ChessGame chessGame = new ChessGame();
            messWithChessBoard(chessGame);

            gameData = new GameData(gameData.gameID(), usernames.getFirst(), usernames.getLast()
                    , "change name", chessGame);

            gameDAO.updateGame(gameData);

            Assertions.assertEquals(gameData, gameDAO.getGame(gameData.gameID()));
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(7)
    @DisplayName("Update nonexistent game")
    void updateGameNotFoundTest() {
        List<String> usernames = addDataToDAOs().stream().toList();
        ChessGame chessGame = new ChessGame();
        messWithChessBoard(chessGame);
        GameData gameData = new GameData(Integer.toString(newIntID()), usernames.getFirst(), usernames.getLast()
                , "FakeGameData", chessGame);
        Assertions.assertThrowsExactly(InvalidRequestException.class, () -> gameDAO.updateGame(gameData));
    }

    @Test
    @Order(8)
    @DisplayName("Get Auth data")
    void getAuthData() {
        List<String> usernames = IntStream.range(0, 10)
                .mapToObj(i -> "Username" + i).toList();
        List<String> authTokens = IntStream.range(0, 10)
                .mapToObj(i -> newUUID()).toList();
        try {
            for (int i = 0; i < 10; i++) {
                userDAO.addUser(new UserData(usernames.get(i), "password", "email@email.com"));
                authDAO.addAuthData(new AuthData(authTokens.get(i), usernames.get(i)));
            }
            for (String authToken : authTokens) {
                AuthData authData = authDAO.getAuthDataWithAuthToken(authToken);
                Assertions.assertTrue(usernames.contains(authData.username()));
            }
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(9)
    @DisplayName("get nonexistent authData")
    void getNonexistentAuthData() {
        String fakeID = newUUID();
        addDataToDAOs();
        Assertions.assertThrowsExactly(InvalidRequestException.class,
                () -> authDAO.getAuthDataWithAuthToken(fakeID));
    }

    @Test
    @Order(10)
    @DisplayName("Add AuthData")
    void addAuthDataTest() {
        String authToken = newUUID();
        Assertions.assertTrue(authDAO.isEmpty(), "No authData should exist at this point");
        try {
            authDAO.addAuthData(new AuthData(authToken, EXISTING_USER.username()));
            Assertions.assertFalse(authDAO.isEmpty(),
                    "AuthData was added but that change is not reflected in database");
            Assertions.assertNotNull(authDAO.getAuthDataWithAuthToken(authToken),
                    "AuthDAO returned null when it should have returned a value");
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }

    }

    @Test
    @Order(11)
    @DisplayName("Add Invalid AuthData")
    void addInvalidAuthDataTest() {
        String authToken = newUUID();
        Assertions.assertTrue(authDAO.isEmpty(), "No authData should exist at this point");
        Assertions.assertThrowsExactly(InvalidRequestException.class,
                () -> authDAO.addAuthData(new AuthData(authToken, NONEXISTING_USER.username())));
    }

    @Test
    @Order(12)
    @DisplayName("remove AuthData")
    void removeAuthDataTest() {
        String authToken = newUUID();
        Assertions.assertTrue(authDAO.isEmpty(), "No authData should exist at this point");
        try {
            authDAO.addAuthData(new AuthData(authToken, EXISTING_USER.username()));
            Assertions.assertFalse(authDAO.isEmpty(),
                    "AuthData was added but that change is not reflected in database");
            authDAO.removeAuthData(authToken);
            Assertions.assertTrue(authDAO.isEmpty());
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }

    }

    @Test
    @Order(13)
    @DisplayName("remove nonexistent authData")
    void removeInvalidAuthDataTest() {
        addDataToDAOs();
        String fakeAuth = newUUID();
        Assertions.assertThrowsExactly(InvalidRequestException.class,
                () -> authDAO.removeAuthData(fakeAuth));
    }

    @Test
    @Order(14)
    @DisplayName("Username in use check")
    void usernameInUseTest() {
        Assertions.assertTrue(userDAO.usernameInUse(EXISTING_USER.username()));
        Assertions.assertFalse(userDAO.usernameInUse(NONEXISTING_USER.username()));
    }

    @Test
    @Order(15)
    @DisplayName("Check username using null")
    void invalidUsernameTest() {
        Assertions.assertFalse(userDAO.usernameInUse(null));
        Assertions.assertFalse(userDAO.usernameInUse(""));
    }

    @Test
    @Order(16)
    @DisplayName("Add User test")
    void addUserTest() {
        Assertions.assertThrowsExactly(InvalidRequestException.class,
                () -> userDAO.getUser(NONEXISTING_USER.username()),
                "Error should be thrown when trying to get a nonexisting user");
        try {
            userDAO.addUser(NONEXISTING_USER);
            UserData userData = userDAO.getUser(NONEXISTING_USER.username());
            Assertions.assertEquals(NONEXISTING_USER, userData, "Incorrect user data was returned from username");
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(17)
    @DisplayName("Add user username in use")
    void addUserDuplicateUsernameTest() {
        Assertions.assertThrowsExactly(UnavailableRequestException.class,
                () -> userDAO.addUser(EXISTING_USER),
                "Error should throw when adding a user when the name is already in use");
    }

    @Test
    @Order(18)
    @DisplayName("Get user test")
    void getUserTest() {
        try {
            userDAO.getUser(EXISTING_USER.username());
        } catch (DataAccessException e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(19)
    @DisplayName("get nonexistent user test")
    void getNonexistentUserTest() {
        Assertions.assertThrowsExactly(InvalidRequestException.class,
                () -> userDAO.getUser(NONEXISTING_USER.username()),
                "Error should be thrown when trying to get a nonexisting user");
    }



    /*
     *   X Collection<GameData> getGameList() throws DataAccessException;
     *   X GameData getGame(String gameID) throws DataAccessException;
     *   X void putGame(GameData game) throws DataAccessException;
     *   X void updateGame(GameData game) throws DataAccessException;
     *
     *   X AuthData getAuthDataWithAuthToken(String authToken) throws DataAccessException;
     *   X void addAuthData(AuthData userData) throws DataAccessException;
     *   X void removeAuthData(String authToken) throws DataAccessException;
     *
     *   boolean usernameInUse(String username);
     *   void addUser(UserData userData) throws DataAccessException;
     *   UserData getUser(String username) throws DataAccessException;
     * */


    public static boolean allDAOsAreEmpty() {
        return authDAO.isEmpty() && gameDAO.isEmpty() && userDAO.isEmpty();
    }

    public static Collection<String> addDataToDAOs() {
        Collection<String> usernames = IntStream.range(0, 10)
                .mapToObj(i -> "Username" + i)
                .toList();
        addToUserDAO(usernames);// must be first to avoid errors
        addToAuthDAO(usernames);
        addToGameDAO();
        return usernames;
    }

    public static void addToAuthDAO(Collection<String> usernames) {
        for (String username : usernames) {
            try {
                authDAO.addAuthData(new AuthData(Util.newUUID(), username));
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Collection<GameData> addToGameDAO() {
        ArrayList<GameData> games = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            try {
                GameData gd = new GameData(Integer.toString(newIntID()),
                        null, null,
                        "game" + i, new ChessGame());
                games.add(gd);
                gameDAO.putGame(gd);
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return games;
    }

    public static void addToUserDAO(Collection<String> usernames) {
        for (String username : usernames) {
            try {
                userDAO.addUser(new UserData(username,
                        "password" + username,
                        "email" + username + "@gmail.com"));
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void messWithChessBoard(ChessGame game) {
        game.toggleTeamTurn();
        try {
            game.getBoard().movePiece(new ChessMove(
                    new ChessPosition(2, 4)
                    , new ChessPosition(4, 4),
                    null));
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
    }
}
