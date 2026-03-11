package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO extends DAO {

    Collection<GameData> getGameList() throws DataAccessException;

    GameData getGame(String gameID) throws DataAccessException;

    void putGame(GameData game) throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;
    //void deleteGame(String gameID) throws DataAccessException;
}
