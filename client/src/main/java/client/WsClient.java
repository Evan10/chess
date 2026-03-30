package client;

import com.google.gson.Gson;
import jakarta.websocket.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WsClient extends Endpoint {
    private final Session session;
    private static final Gson serializer = new Gson();
    public WsClient(URI serverAddress, ClientMessageHandler messageHandler) throws URISyntaxException, DeploymentException, IOException {

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, new URI("ws://"+serverAddress.getPath() + "/ws"));
        session.addMessageHandler(messageHandler);

    }


    public void send(Object message) throws IOException {
        send(serializer.toJson(message));
    }
    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }


    public void close(){
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
