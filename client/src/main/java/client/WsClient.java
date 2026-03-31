package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import model.AuthData;
import websocket.commands.UserGameCommand;

import javax.naming.NoPermissionException;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static websocket.commands.UserGameCommand.CommandType.*;

public class WsClient extends Endpoint {
    private final Session session;
    private final ClientSessionData sessionData;
    private static final Gson serializer = new Gson();
    public WsClient(URI serverAddress, ClientMessageHandler messageHandler, ClientSessionData sessionData) throws URISyntaxException, DeploymentException, IOException {
        this.sessionData=sessionData;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, new URI("ws://"+serverAddress.getPath() + "/ws"));
        session.addMessageHandler(messageHandler);

    }

    public void connectToGame(int gameID) throws IOException {
        AuthData authData = sessionData.getAuthData();
        if(authData == null) {throw new IOException("Error: no auth");}
        String authToken = authData.authToken();
        UserGameCommand command = new UserGameCommand(CONNECT,authToken,gameID);
        send(command);
    }

    public void makeMoveInGame(ChessMove move) throws IOException {
        AuthData authData = sessionData.getAuthData();
        if(authData == null) {throw new IOException("Error: no auth");}
        String authToken = authData.authToken();
        UserGameCommand command = new UserGameCommand(MAKE_MOVE,authToken,
                Integer.parseInt(sessionData.getCurrentGameID()),
                move);
        send(command);
    }

    public void leaveGame() throws IOException{
        AuthData authData = sessionData.getAuthData();
        if(authData == null) {throw new IOException("Error: no auth");}
        String authToken = authData.authToken();
        UserGameCommand command = new UserGameCommand(LEAVE,
                authToken,
                Integer.parseInt(sessionData.getCurrentGameID()));
        send(command);
    }
    public void resignFromGame() throws IOException{
        AuthData authData = sessionData.getAuthData();
        if(authData == null) {throw new IOException("Error: no auth");}
        String authToken = authData.authToken();
        UserGameCommand command = new UserGameCommand(RESIGN,
                authToken,
                Integer.parseInt(sessionData.getCurrentGameID()));
        send(command);
    }

    private void send(Object message) throws IOException {
        send(serializer.toJson(message));
    }
    private void send(String message) throws IOException {
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
