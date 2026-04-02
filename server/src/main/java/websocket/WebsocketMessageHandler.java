package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.exception.DataAccessException;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import model.GameData;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import service.GameService;
import util.MyLogger;
import util.Util;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.results.LeaveGameResult;
import websocket.results.MakeMoveResult;
import websocket.results.ObserveGameResult;
import websocket.results.ResignResult;

import java.io.IOException;
import java.util.logging.Logger;

import static util.Constants.OK;
import static util.Util.humanReadableChessMove;
import static util.Util.isPlayer;
import static websocket.messages.ServerMessage.NotificationType.*;

public class WebsocketMessageHandler implements WsMessageHandler {
    private final static Logger LOGGER = MyLogger.getLogger();
    private static final Gson DESERIALIZER = new Gson();

    private final WebsocketSessionHandler sessionHandler;
    private final GameService gameService;
    private final AuthService authService;

    public WebsocketMessageHandler(WebsocketSessionHandler sessionHandler, GameService gameService, AuthService authService){
        this.sessionHandler=sessionHandler;
        this.gameService=gameService;
        this.authService=authService;
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws Exception {
        UserGameCommand command = DESERIALIZER.fromJson(ctx.message(),UserGameCommand.class);
        LOGGER.info("Websocket message received:\n" +ctx.message() );
        try{
        switch (command.getCommandType()){
            case LEAVE -> handleLeave(ctx, command);
            case CONNECT -> handleConnect(ctx, command);
            case RESIGN -> handleResign(ctx, command);
            case MAKE_MOVE -> handleMakeMove(ctx,command);
        }}catch(DataAccessException e){
            ServerMessage err = new ServerMessage(ServerMessage.ServerMessageType.ERROR,e.getMessage());
            WebsocketSessionHandler.sendErrorMessage(ctx.session,err);
            LOGGER.info("Websocket error:\n" +err.getMessage() );
        }
    }


    private void handleLeave(WsMessageContext ctx, UserGameCommand command) throws IOException, DataAccessException {
        AuthData authData = authService.getAuth(command.getAuthToken());
        if(!authData.isValid()){
            throw new DataAccessException("Error: invalid auth; try logging in again");
        }
        LeaveGameResult result = gameService.leaveGame(command,authData);
        if(result.statusCode()!=OK){
            throw new DataAccessException(result.message());
        }
        sessionHandler.leaveGame(command.getGameID().toString(),ctx.session);
        String leaveMessage = String.format("User %s left the game", authData.username());
        sessionHandler.broadcastNotification(ctx.session,USER_LEAVE,leaveMessage,command.getGameID().toString());

    }

    private void handleConnect(WsMessageContext ctx, UserGameCommand command) throws IOException, DataAccessException {
        AuthData authData = authService.getAuth(command.getAuthToken());
        if(!authData.isValid()){
            throw new DataAccessException("Error: invalid auth; try logging in again");
        }
        String oldGameID = ctx.attribute("currentGameID");
        String newGameID = command.getGameID().toString();
        if(oldGameID!=null){
            sessionHandler.switchGame(oldGameID, newGameID, ctx.session);
        }else{
            sessionHandler.addSessionToGame(newGameID,ctx.session);
        }
        ctx.attribute("currentGameID",newGameID);
        ObserveGameResult result = gameService.observeGame(command,authData);
        if(result.statusCode()!= 200){
            throw new DataAccessException(result.message());
        }
        GameData gameData = result.gameData();
        ServerMessage messageToConnector = new ServerMessage(gameData);
        WebsocketSessionHandler.sendMessage(ctx.session,messageToConnector);

        String connectMessage=decideConnectMessage(authData.username(),gameData);
        sessionHandler.broadcastNotification(ctx.session,USER_CONNECT,connectMessage,newGameID);

    }
    private static String decideConnectMessage(String username, GameData gameData){
        if(Util.isPlayer(username,gameData)){
            ChessGame.TeamColor color = Util.getTeamColor(username, gameData);
            if(color == null){
                return "Error: invalid player team";
            }
            return String.format("User %s connected as the %s player",
                    username, color.name());
        }else{
            return String.format("User %s is now observing this game", username);
        }

    }

    private void handleResign(WsMessageContext ctx, UserGameCommand command) throws IOException, DataAccessException {
        AuthData authData = authService.getAuth(command.getAuthToken());
        if(!authData.isValid()){
            throw new DataAccessException("Error: invalid auth; try logging in again");
        }
        String currentGameID = command.getGameID().toString();
        ResignResult result = gameService.resignFromGame(command, authData);
        if(result.statusCode() != OK){
            ServerMessage err = new ServerMessage(ServerMessage.ServerMessageType.ERROR,result.message());
            WebsocketSessionHandler.sendErrorMessage(ctx.session,err);
            return;
        }
        ServerMessage.NotificationType type = result.teamToResign().equals(ChessGame.TeamColor.WHITE)?
                WHITE_RESIGN:
                BLACK_RESIGN;
        sessionHandler.broadcastNotification(null,type, result.message(),currentGameID);
    }

    private void handleMakeMove(WsMessageContext ctx, UserGameCommand command) throws IOException, DataAccessException {
            AuthData authData = authService.getAuth(command.getAuthToken());
            if(!authData.isValid()){
                throw new DataAccessException("Error: invalid auth; try logging in again");
            }
            MakeMoveResult result = gameService.makeMove(command, authData);
            if(result.statusCode() != OK){
                ServerMessage err = new ServerMessage(ServerMessage.ServerMessageType.ERROR,result.message());
                WebsocketSessionHandler.sendErrorMessage(ctx.session,err);
                return;
            }
            GameData gameData = result.gameData();
            sessionHandler.broadcastGameUpdate(gameData);
            sessionHandler.broadcastNotification(ctx.session,
                CHESS_MOVE,
                decideMoveMessage(command, authData.username()),
                    gameData.gameID());
            boolean doNotifyGameState = !gameData.game().getState().equals(ChessGame.GameState.NORMAL);
            if(doNotifyGameState){
                ServerMessage.NotificationType type = gameStateToNotificationType(gameData.game().getState());
                sessionHandler.broadcastNotification(null,
                        type,
                        decideGameEventMessage(type,gameData.whiteUsername(),gameData.blackUsername()),
                        result.gameData().gameID());
            }

    }

    private ServerMessage.NotificationType gameStateToNotificationType(ChessGame.GameState state){
        return switch (state){
            case WHITE_WIN_OPP_RESIGN -> BLACK_RESIGN;
            case BLACK_WIN_OPP_RESIGN -> WHITE_RESIGN;
            case DRAW_STALEMATE -> DRAW_GAME;
            case WHITE_WIN_CHECKMATE -> WHITE_WIN_BY_CHECKMATE;
            case BLACK_WIN_CHECKMATE -> BLACK_WIN_BY_CHECKMATE;
            case BLACK_CHECK -> BLACK_IN_CHECK;
            case WHITE_CHECK -> WHITE_IN_CHECK;
            case null, default -> PRINT_INFO;
        };
    }

    private String decideGameEventMessage(ServerMessage.NotificationType type,String whiteUser, String blackUser){
        return String.format(switch (type){
            case WHITE_IN_CHECK -> "%2$s put %1$s in check";
            case BLACK_IN_CHECK -> "%1$s put %2$s in check";
            case WHITE_RESIGN -> "%1$s playing white resigned";
            case BLACK_RESIGN -> "%2$s playing black resigned";
            case BLACK_WIN_BY_CHECKMATE -> "%2$s checkmated %1$s";
            case WHITE_WIN_BY_CHECKMATE -> "%1$s checkmated %2$s";
            case DRAW_GAME -> "The game ended in a draw";
            default -> "Unknown game event occurred";
        },whiteUser,blackUser);
    }
    private String decideMoveMessage(UserGameCommand command, String username){
        ChessMove move = command.getMove();
        return username+" made the move " + humanReadableChessMove(move);
    }
}
