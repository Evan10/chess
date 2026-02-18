package service;

import dataAccess.GameDAO;
import requestResult.*;

public class GameService {

    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO){
        this.gameDAO=gameDAO;
    }

    public ListGamesResult listGames(ListGamesRequest req){
        return null;
    }

    public CreateGameResult createGame(CreateGameRequest req){
        return null;
    }

    public JoinGameResult joinGame(JoinGameRequest req){
        return null;
    }

}
