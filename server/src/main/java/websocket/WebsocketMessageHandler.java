package websocket;

import com.google.gson.Gson;
import dataaccess.exception.DataAccessException;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.results.MakeMoveResult;
import websocket.results.ResignResult;

import java.io.IOException;

import static util.Constants.OK;

public class WebsocketMessageHandler implements WsMessageHandler {
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
        switch (command.getCommandType()){
            case LEAVE -> handleLeave(ctx, command);
            case CONNECT -> handleConnect(ctx, command);
            case RESIGN -> handleResign(ctx, command);
            case MAKE_MOVE -> handleMakeMove(ctx,command);
        }

    }


    private void handleLeave(WsMessageContext ctx, UserGameCommand command){
        sessionHandler.leaveGame(command.getGameID().toString(),ctx.session);
    }

    private void handleConnect(WsMessageContext ctx, UserGameCommand command){

        String oldGameID = ctx.attribute("currentGameID");
        String newGameID = command.getGameID().toString();
        if(oldGameID!=null){
            sessionHandler.switchGame(oldGameID, newGameID, ctx.session);
        }else{
            sessionHandler.addSessionToGame(newGameID,ctx.session);
        }
        ctx.attribute("currentGameID",newGameID);
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand command) throws IOException {
        String currentGameID = command.getGameID().toString();
        try {
            AuthData authData = authService.getAuth(command.getAuthToken());
            ResignResult result = gameService.resignFromGame(command, authData);
            if(result.statusCode() != OK){
                ServerMessage err = new ServerMessage(ServerMessage.ServerMessageType.ERROR,result.message());
                WebsocketSessionHandler.sendErrorMessage(ctx.session,err);
                return;
            }
            sessionHandler.broadcastResign(ctx.session,currentGameID, result.message());
        } catch (DataAccessException e) {
            ServerMessage err = new ServerMessage(ServerMessage.ServerMessageType.ERROR,e.getMessage());
            WebsocketSessionHandler.sendErrorMessage(ctx.session,err);
        }
    }

    private void handleMakeMove(WsMessageContext ctx, UserGameCommand command) throws IOException {
        try {
            AuthData authData = authService.getAuth(command.getAuthToken());
            MakeMoveResult result = gameService.makeMove(command, authData);
            if(result.statusCode() != OK){
                ServerMessage err = new ServerMessage(ServerMessage.ServerMessageType.ERROR,result.message());
                WebsocketSessionHandler.sendErrorMessage(ctx.session,err);
                return;
            }
            sessionHandler.broadcastGameUpdate(result.gameData());
        } catch (DataAccessException e) {
            ServerMessage err = new ServerMessage(ServerMessage.ServerMessageType.ERROR,e.getMessage());
            WebsocketSessionHandler.sendErrorMessage(ctx.session,err);
        }
    }



}
