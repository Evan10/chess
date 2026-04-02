package websocket;


import io.javalin.websocket.*;

import org.jetbrains.annotations.NotNull;
import util.MyLogger;

import java.util.logging.Logger;

public class WebsocketConnection implements WsConnectHandler, WsCloseHandler {


    private final static Logger LOGGER = MyLogger.getLogger();
    private final WebsocketSessionHandler sessionHandler;

    public WebsocketConnection(WebsocketSessionHandler sessionHandler){
        this.sessionHandler=sessionHandler;
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {
        sessionHandler.leaveGame(ctx.attribute("currentGameID"),ctx.session);
        LOGGER.info("User disconnected websocket because: "+ctx.reason());
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        ctx.enableAutomaticPings();
        LOGGER.info("New user connected websocket");
    }

}
