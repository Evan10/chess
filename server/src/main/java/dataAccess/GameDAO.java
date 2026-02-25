package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO extends DAO{

    Collection<GameData> getGameList();
    GameData getGame(String gameID) throws DataAccessException;
    void putGame(GameData game);
    //void deleteGame(String gameID) throws DataAccessException;
}
