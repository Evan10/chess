package dataAccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDAO implements GameDAO{

    private final Map<String, GameData> allGameData;
    public MemoryGameDAO(){
        allGameData = new HashMap<>();
    }

    @Override
    public boolean clear() {
        allGameData.clear();
        return true;
    }

    @Override
    public Collection<GameData> getGameList() {
        return allGameData.values();
    }

    @Override
    public boolean putGame(GameData game) {
        allGameData.put(game.gameID(),game);
        return false;
    }

    @Override
    public void deleteGame(String gameID) throws DataAccessException {
        if(!allGameData.containsKey(gameID)) throw new DataAccessException("Error: Game not found");
        allGameData.remove(gameID);
    }

    @Override
    public GameData getGame(String gameID) throws DataAccessException{
        if(!allGameData.containsKey(gameID)) throw new DataAccessException("Error: Game not found");
        return allGameData.get(gameID);
    }

}
