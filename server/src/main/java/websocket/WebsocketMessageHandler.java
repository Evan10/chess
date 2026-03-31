package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;

public class WebsocketMessageHandler implements WsMessageHandler {
    private static final Gson DESERIALIZER = new Gson();

    private final WebsocketSessionHandler sessionHandler;
    WebsocketMessageHandler(WebsocketSessionHandler sessionHandler){
        this.sessionHandler=sessionHandler;

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

    }

    private void handleConnect(WsMessageContext ctx, UserGameCommand command){
        sessionHandler.addSessionToGame(command.getGameID().toString(),ctx.session);
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand command){

    }

    private void handleMakeMove(WsMessageContext ctx, UserGameCommand command){

    }



}
