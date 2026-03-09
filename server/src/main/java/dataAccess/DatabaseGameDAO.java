package dataaccess;

import model.GameData;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

public class DatabaseGameDAO implements GameDAO{
    private final Connection connection;

    public DatabaseGameDAO(Connection connection) {
        this.connection = connection;
    }


    @Override
    public Collection<GameData> getGameList() {
        return List.of();
    }

    @Override
    public GameData getGame(String gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void putGame(GameData game) {

    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
