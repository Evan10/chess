package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static dataaccess.DataAccessException.INVALID_REQUEST_ERROR;

public class MemoryGameDAO implements GameDAO {

    private final Map<String, GameData> allGameData;

    protected MemoryGameDAO() {
        allGameData = new HashMap<>();
    }

    @Override
    public void clear() {
        allGameData.clear();
    }

    @Override
    public boolean isEmpty() {
        return allGameData.isEmpty();
    }

    @Override
    public Collection<GameData> getGameList() {
        return allGameData.values();
    }

    @Override
    public void putGame(GameData game) {
        allGameData.put(game.gameID(), game);
    }

    @Override
    public void updateGame(GameData game) {
        putGame(game);
    }


    @Override
    public GameData getGame(String gameID) throws DataAccessException {
        if (!allGameData.containsKey(gameID)) {
            throw new DataAccessException("Error: Game not found", INVALID_REQUEST_ERROR);
        }
        return allGameData.get(gameID);
    }

}
