package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import util.MyLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import static dataaccess.DatabaseManager.getConnection;

public class DatabaseGameDAO implements GameDAO {
    private final Gson serializer;

    private final static Logger logger = MyLogger.getLogger();

    protected DatabaseGameDAO() {
        serializer = new Gson();
    }


    @Override
    public Collection<GameData> getGameList() throws DataAccessException {
        String statement = """
                SELECT *
                FROM games;
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ResultSet set = ps.executeQuery();
            Collection<GameData> gameList = new ArrayList<>();
            while (set.next()) {
                ChessGame game = serializer.
                        fromJson(set.getString("gameJSON"), ChessGame.class);
                gameList.add(new GameData(set.getString("gameID"),
                        set.getString("whiteUsername"),
                        set.getString("blackUsername"),
                        set.getString("gameName"),
                        game));
            }
            return gameList;
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            } else if (e.getSQLState().startsWith("42")) {
                throw new InvalidRequestException("Error: bad request");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public GameData getGame(String gameID) throws DataAccessException {
        String statement = """
                SELECT whiteUsername, blackUsername, gameName, gameJSON
                FROM games
                WHERE gameID = ?
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new InvalidRequestException("Error: Game not found");
                }

                ChessGame game = serializer.
                        fromJson(rs.getString("gameJSON"), ChessGame.class);
                return new GameData(
                        gameID,
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        game
                );
            }
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            } else if (e.getSQLState().startsWith("42")) {
                throw new InvalidRequestException("Error: bad request");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void putGame(GameData game) throws DataAccessException {
        String statement = """
                INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, gameJSON)
                VALUES (?,?,?,?,?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, game.gameID());
            ps.setString(2, game.whiteUsername());
            ps.setString(3, game.blackUsername());
            ps.setString(4, game.gameName());
            ps.setString(5, serializer.toJson(game.game()));
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("23")) {
                throw new UnavailableRequestException("Error: Game name already in use");
            } else if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            } else if (e.getSQLState().startsWith("42")) {
                throw new InvalidRequestException("Error: bad request");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String statement = """
                UPDATE games
                SET whiteUsername = ?,
                    blackUsername = ?,
                    gameName = ?,
                    gameJSON = ?
                WHERE gameID = ?;
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, serializer.toJson(game.game()));
            ps.setString(5, game.gameID());
            int linesChanged = ps.executeUpdate();
            if (linesChanged == 0) {
                throw new InvalidRequestException("Error: game not found");
            }
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            } else if (e.getSQLState().startsWith("42")) {
                throw new InvalidRequestException("Error: bad request");
            }
            throw new DataAccessException(e.toString());
        }

    }

    @Override
    public void clear() throws DataAccessException {
        String statement = """
                DELETE FROM games
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public boolean isEmpty() {
        try (Statement s = getConnection().createStatement()) {
            return !s.executeQuery("SELECT 1 FROM games LIMIT 1;").next();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
