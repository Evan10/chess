package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


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
    public void putGame(GameData game) throws DataAccessException{
        boolean nameInUse = allGameData.values().stream()
                .anyMatch((gd) -> gd.gameName().equals(game.gameName()));
        if(nameInUse) throw new InvalidRequestException("Username is already in use");
        allGameData.put(game.gameID(), game);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        putGame(game);
    }


    @Override
    public GameData getGame(String gameID) throws DataAccessException {
        if (!allGameData.containsKey(gameID)) {
            throw new InvalidRequestException("Error: Game not found");
        }
        return allGameData.get(gameID);
    }

}
