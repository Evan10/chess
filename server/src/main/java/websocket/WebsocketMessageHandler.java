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
        sessionHandler.broadcastResign(ctx.session,currentGameID, result.message());
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
            sessionHandler.broadcastGameUpdate(result.gameData());
            boolean toSender = !result.gameData().game().getState().equals(ChessGame.GameState.NORMAL);
            sessionHandler.broadcastNotification(toSender ? null : ctx.session,
                    CHESS_MOVE,
                    decideMoveMessage(command, authData.username()),
                    result.gameData().gameID());


    }

    private String decideMoveMessage(UserGameCommand command, String username){
        ChessMove move = command.getMove();
        return username+" made the move " + humanReadableChessMove(move);
    }
}
