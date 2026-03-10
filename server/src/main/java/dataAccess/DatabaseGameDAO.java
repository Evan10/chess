package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.AuthData;
import model.GameData;
import util.MyLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static dataaccess.DataAccessException.*;
import static dataaccess.DatabaseManager.getConnection;

public class DatabaseGameDAO implements GameDAO{
    private final Gson serializer;

    private static Logger logger = MyLogger.getLogger();

    protected DatabaseGameDAO() {
        serializer = new Gson();
    }


    @Override
    public Collection<GameData> getGameList() throws DataAccessException {
        String statement = """
                SELECT *
                FROM games;
                """;

        try(PreparedStatement ps = getConnection().prepareStatement(statement)){
            ResultSet set = ps.executeQuery();
            Collection<GameData> gameList = new ArrayList<>();
            while(set.next()){
                ChessGame game = serializer.
                        fromJson(set.getString("gameJSON"),ChessGame.class);
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
                throw new DataAccessException("Error: Internal Database error", DATABASE_ERROR);
            }else if(e.getSQLState().startsWith("42")){
                throw new DataAccessException("Error: bad request", INVALID_REQUEST_ERROR);
            }
            throw new DataAccessException(e.toString(),UNKNOWN_ERROR);
        }
    }

    @Override
    public GameData getGame(String gameID) throws DataAccessException {
        String statement = """
            SELECT whiteUsername, blackUsername, gameName, gameJSON
            FROM games
            WHERE gameID = ?
            """;

        try(PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1,gameID);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()){
                    throw new DataAccessException("Error: Game not found", INVALID_REQUEST_ERROR);
                }

                ChessGame game = serializer.
                        fromJson(rs.getString("gameJSON"),ChessGame.class);
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
                throw new DataAccessException("Error: Internal Database error", DATABASE_ERROR);
            }else if(e.getSQLState().startsWith("42")){
                throw new DataAccessException("Error: bad request", INVALID_REQUEST_ERROR);
            }
            throw new DataAccessException(e.toString(),UNKNOWN_ERROR);
        }
    }

    @Override
    public void putGame(GameData game) throws DataAccessException{
        String statement = """
                INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, gameJSON)
                VALUES (?,?,?,?,?)
                """;
        try(PreparedStatement ps = getConnection().prepareStatement(statement)){
            ps.setString(1, game.gameID());
            ps.setString(2, game.whiteUsername());
            ps.setString(3, game.blackUsername());
            ps.setString(4, game.gameName());
            ps.setString(5, serializer.toJson(game.game()));
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning(e.toString());
            if(e.getSQLState().startsWith("23")){
                throw new DataAccessException("Error: Game name already in use", UNAVAILABLE_REQUEST_ERROR);
            } else if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DataAccessException("Error: Internal Database error", DATABASE_ERROR);
            }else if(e.getSQLState().startsWith("42")){
                throw new DataAccessException("Error: bad request", INVALID_REQUEST_ERROR);
            }
            throw new DataAccessException(e.toString(),UNKNOWN_ERROR);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException{
        String statement = """
                UPDATE games
                SET whiteUsername = ?,
                    blackUsername = ?,
                    gameJSON = ?
                WHERE gameID = ?;
                """;
        try(PreparedStatement ps = getConnection().prepareStatement(statement)){
            ps.setString(1,game.whiteUsername());
            ps.setString(2,game.blackUsername());
            ps.setString(3, serializer.toJson(game.game()));
            ps.setString(4,game.gameID());
            int linesChanged = ps.executeUpdate();
            if(linesChanged==0){
                throw new DataAccessException("Error: game not found", INVALID_REQUEST_ERROR);
            }
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DataAccessException("Error: Internal Database error", DATABASE_ERROR);
            }else if(e.getSQLState().startsWith("42")){
                throw new DataAccessException("Error: bad request", INVALID_REQUEST_ERROR);
            }
            throw new DataAccessException(e.toString(),UNKNOWN_ERROR);
        }

    }

    @Override
    public void clear() throws DataAccessException {
        String statement = """
            DELETE FROM games
            """;

        try(PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DataAccessException("Error: Internal Database error", DATABASE_ERROR);
            }
            throw new DataAccessException(e.toString(),UNKNOWN_ERROR);
        }
    }

    @Override
    public boolean isEmpty() {
        try(Statement s = getConnection().createStatement()){
            return !s.executeQuery("SELECT 1 FROM games LIMIT 1;").next();
        }catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
