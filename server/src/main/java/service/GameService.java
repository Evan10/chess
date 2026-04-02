package service;

import chess.*;
import dataaccess.exception.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.exception.InvalidRequestException;
import dataaccess.exception.UnavailableRequestException;
import model.AuthData;
import model.GameData;
import model.endpointresults.CreateGameResult;
import model.endpointresults.JoinGameResult;
import model.endpointresults.ListGamesResult;
import org.jetbrains.annotations.NotNull;
import request.*;
import util.Util;
import websocket.commands.UserGameCommand;
import websocket.results.LeaveGameResult;
import websocket.results.MakeMoveResult;
import websocket.results.ObserveGameResult;
import websocket.results.ResignResult;

import static util.Constants.*;
import static util.Util.getTeamColor;
import static util.Util.isPlayer;

public class GameService {

    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }


    public ObserveGameResult observeGame(UserGameCommand command, AuthData authData) throws DataAccessException {
        GameData gameData = gameDAO.getGame(command.getGameID().toString());
        if(authData!= null){
            return new ObserveGameResult(OK,"", gameData);
        }else{
            return new ObserveGameResult(FORBIDDEN,"Error: user does not have authorization", null);
        }
    }

    public MakeMoveResult makeMove(UserGameCommand command, AuthData authData) throws DataAccessException {
        GameData gameData = gameDAO.getGame(command.getGameID().toString());
        ChessGame game = gameData.game();
        ChessMove move = command.getMove();

        if(!authData.username().equals(
                game.getTeamTurn().equals(ChessGame.TeamColor.WHITE)
                ? gameData.whiteUsername()
                : gameData.blackUsername())){
            return new MakeMoveResult(FORBIDDEN, "Error: invalid move attempt", gameData);
        }

        if(!game.getState().isGameOver()){
            try {
                game.makeMove(move);
                gameDAO.updateGame(gameData);
                return new MakeMoveResult(OK, "",gameData);
            } catch (InvalidMoveException e) {
                return new MakeMoveResult(FORBIDDEN,"Error: invalid move", gameData);
            }
        }else{
            return new MakeMoveResult(FORBIDDEN,"Error: game is over", gameData);
        }
    }

    public ResignResult resignFromGame(UserGameCommand command, AuthData authData) throws DataAccessException {
        GameData gameData = gameDAO.getGame(command.getGameID().toString());
        ChessGame game = gameData.game();
        ChessGame.TeamColor team;
        if(gameData.whiteUsername().equals(authData.username())){
            team = ChessGame.TeamColor.WHITE;
        }else if(gameData.blackUsername().equals(authData.username())){
            team = ChessGame.TeamColor.BLACK;
        }else{
            return new ResignResult(FORBIDDEN,"Error: user is not a player in this game", null);
        }
        if(!game.getState().isGameOver()){
                game.setState(team.equals(ChessGame.TeamColor.WHITE)
                        ? ChessGame.GameState.BLACK_WIN_OPP_RESIGN
                        : ChessGame.GameState.WHITE_WIN_OPP_RESIGN);
                gameDAO.updateGame(gameData);
                String msg = String.format("User %s playing as %s resigned",authData.username(),team.name());
                return new ResignResult(OK, msg, team);
        }else{
            return new ResignResult(FORBIDDEN,"Error: game is over",null);
        }
    }

    public LeaveGameResult leaveGame(UserGameCommand command, AuthData authData) throws DataAccessException {
        GameData gameData = gameDAO.getGame(command.getGameID().toString());
        if(!isPlayer(authData.username(),gameData)){
            return new LeaveGameResult(OK,String.format("Observer %s left the game",authData.username()),gameData);
        }
        boolean isWhite = ChessGame.TeamColor.WHITE.equals(getTeamColor(authData.username(),gameData));
        String whiteUsername = isWhite? null: gameData.whiteUsername();
        String blackUsername=isWhite? gameData.blackUsername():null;
        GameData updatedGameData = new GameData(gameData.gameID(),
                whiteUsername,
                blackUsername,
                gameData.gameName(),
                gameData.game());
        gameDAO.updateGame(updatedGameData);
        String message = String.format("The %s player %s left the game",isWhite?"White":"Black",authData.username());
        return new LeaveGameResult(OK,message,updatedGameData);
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
        } catch (InvalidRequestException e) {
            return new CreateGameResult(UNAUTHORIZED, e.getMessage());
        } catch (UnavailableRequestException e) {
            return new CreateGameResult(FORBIDDEN, e.getMessage());
        } catch (DataAccessException e) {
            return new CreateGameResult(SERVER_ERROR, e.getMessage());
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
        } catch (InvalidRequestException e) {
            return new JoinGameResult(UNAUTHORIZED, e.getMessage());
        } catch (UnavailableRequestException e) {
            return new JoinGameResult(FORBIDDEN, e.getMessage());
        } catch (DataAccessException e) {
            return new JoinGameResult(SERVER_ERROR, e.getMessage());
        }

        return new JoinGameResult(util.Constants.OK, "");
    }

    private boolean invalidTeamColor(String color) {
        return color == null || color.isBlank() || !(color.equals(Constants.BLACK_TEAM) || color.equals(Constants.WHITE_TEAM));
    }
}
