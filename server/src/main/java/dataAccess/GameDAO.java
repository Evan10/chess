package dataAccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO extends DAO{

    public Collection<GameData> getGameList();
    public GameData getGame(String gameID) throws DataAccessException;
    public boolean putGame(GameData game);
    public void deleteGame(String gameID) throws DataAccessException;
}
