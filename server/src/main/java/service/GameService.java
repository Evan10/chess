package service;

import chess.ChessGame;
import chess.Constants;
import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import requestResult.*;
import util.Util;

public class GameService {

    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO){
        this.gameDAO=gameDAO;
    }

    public @NotNull ListGamesResult listGames(ListGamesRequest req){
        String gameList = gameDAO.getGameList().toString();
        return new ListGamesResult(200,gameList);
    }

    public @NotNull CreateGameResult createGame(CreateGameRequest req){
        ChessGame game = new ChessGame();
        String gameID = Util.newUUID();
        GameData gameData = new GameData(gameID,null,null,req.gameName(),game);
        gameDAO.putGame(gameData);

        return new CreateGameResult(200,"");
    }

    public @NotNull JoinGameResult joinGame(JoinGameRequest req){
        try {
            GameData g = gameDAO.getGame(req.gameID());
            boolean joinBlack = req.playerColor().equals(Constants.BLACK_TEAM);
            boolean available = joinBlack
                    ?g.blackUsername() == null
                    :g.whiteUsername() == null;
            if(!available){
                return new JoinGameResult(util.Constants.BAD_REQUEST,"Error: spot already taken");
            }
            GameData newData;
            if(joinBlack){
                 newData = new GameData(g.gameID(),g.whiteUsername(),
                        req.authData().username(),g.gameName(),g.game());
            }else{
                newData = new GameData(g.gameID(),req.authData().username(),
                        g.blackUsername(),g.gameName(),g.game());
            }
            gameDAO.putGame(newData);
        } catch (DataAccessException e) {
            return new JoinGameResult(util.Constants.NOT_FOUND,"Error: game not found");
        }

        return new JoinGameResult(200,"");
    }

}
