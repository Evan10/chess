package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import model.AuthData;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;

import static websocket.commands.UserGameCommand.CommandType.*;

public class WsClient extends Endpoint {
    private final Session session;
    private final ClientSessionData sessionData;
    private static final Gson SERIALIZER = new Gson();

    public WsClient(String host, int port, ClientMessageHandler messageHandler, ClientSessionData sessionData) throws Exception {
        this.sessionData=sessionData;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI endpoint = new URI(String.format("ws://%s:%d/ws",host, port));
        System.out.println(endpoint);
        session = container.connectToServer(this, endpoint);
        session.addMessageHandler(messageHandler);
    }

    public void connectToGame(int gameID) throws IOException {
        handleBasicGameEvent(CONNECT,gameID);
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
        handleBasicGameEvent(LEAVE,Integer.parseInt(sessionData.getCurrentGameID()));
    }
    public void resignFromGame() throws IOException{
        handleBasicGameEvent(RESIGN,Integer.parseInt(sessionData.getCurrentGameID()));
    }

    private void handleBasicGameEvent(UserGameCommand.CommandType type, int gameID) throws IOException{
        AuthData authData = sessionData.getAuthData();
        if(authData == null) {throw new IOException("Error: no auth");}
        String authToken = authData.authToken();
        UserGameCommand command = new UserGameCommand(type, authToken, gameID);
        send(command);

    }

    private void send(Object message) throws IOException {
        send(SERIALIZER.toJson(message));
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
