package websocket;


import io.javalin.websocket.*;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class WebsocketConnection implements WsConnectHandler, WsCloseHandler {

    private final WebsocketSessionHandler sessionHandler;

    public WebsocketConnection(WebsocketSessionHandler sessionHandler){
        this.sessionHandler=sessionHandler;
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        sessionHandler.leaveGame(ctx.attribute("currentGameID"),ctx.session);
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        ctx.enableAutomaticPings(5, TimeUnit.MINUTES);
    }

}
