package service;

import chess.ChessGame;
import chess.Constants;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import requestresult.*;
import util.Util;

import static dataaccess.DataAccessException.*;
import static util.Constants.*;

public class GameService {

    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public @NotNull ListGamesResult listGames(ListGamesRequest req) {
        if (req == null) {
            return new ListGamesResult(SERVER_ERROR, "Error: no request object provided");
        }
        try {
            return new ListGamesResult(util.Constants.OK, null, gameDAO.getGameList());
        } catch (DataAccessException e) {
            return new ListGamesResult(SERVER_ERROR, "Error: there was an issue retrieving games");
        }
    }

    public @NotNull CreateGameResult createGame(CreateGameRequest req) {
        if (req.gameName().isBlank()) {
            return new CreateGameResult(util.Constants.BAD_REQUEST, "Error: can't have blank display name");
        }

        ChessGame game = new ChessGame();
        String gameID = Integer.toString(Util.newIntID());
        GameData gameData = new GameData(gameID, null, null, req.gameName(), game);
        try {
            gameDAO.putGame(gameData);
        } catch (DataAccessException e) {
            int errorCode = switch (e.reason){
                case INVALID_REQUEST_ERROR -> UNAUTHORIZED;
                case UNAVAILABLE_REQUEST_ERROR -> FORBIDDEN;
                default -> SERVER_ERROR;
            };
            return new CreateGameResult(errorCode, e.getMessage());
        }

        return new CreateGameResult(util.Constants.OK, "", gameID);
    }

    public @NotNull JoinGameResult joinGame(JoinGameRequest req) {
        try {
            GameData g = gameDAO.getGame(req.gameID());
            if (invalidTeamColor(req.playerColor())) {
                return new JoinGameResult(util.Constants.BAD_REQUEST, "Error: invalid team color");
            }
            boolean joinBlack = req.playerColor().equals(Constants.BLACK_TEAM);
            boolean available = joinBlack
                    ? g.blackUsername() == null
                    : g.whiteUsername() == null;
            if (!available) {
                return new JoinGameResult(util.Constants.FORBIDDEN, "Error: spot already taken");
            }
            GameData newData;
            if (joinBlack) {
                newData = new GameData(g.gameID(), g.whiteUsername(),
                        req.authData().username(), g.gameName(), g.game());
            } else {
                newData = new GameData(g.gameID(), req.authData().username(),
                        g.blackUsername(), g.gameName(), g.game());
            }
            gameDAO.updateGame(newData);
        } catch (DataAccessException e) {
            int errorCode = switch (e.reason){
                case INVALID_REQUEST_ERROR -> UNAUTHORIZED;
                case UNAVAILABLE_REQUEST_ERROR -> FORBIDDEN;
                default -> SERVER_ERROR;
            };
            return new JoinGameResult(errorCode, e.getMessage());
        }

        return new JoinGameResult(util.Constants.OK, "");
    }


    private boolean invalidTeamColor(String color) {
        return color == null || color.isBlank() || !(color.equals(Constants.BLACK_TEAM) || color.equals(Constants.WHITE_TEAM));
    }
}
