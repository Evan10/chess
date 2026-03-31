package websocket;


import io.javalin.websocket.*;

import org.jetbrains.annotations.NotNull;

public class WebsocketConnection implements WsConnectHandler, WsCloseHandler {

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) throws Exception {

    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) throws Exception {
        ctx.enableAutomaticPings();

    }

}
