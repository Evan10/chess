package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import util.MyLogger;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static util.Constants.OK;

public class WebsocketSessionHandler {

    private static final Logger LOGGER = MyLogger.getLogger();

    private final ConcurrentMap<String, List<Session>> sessions = new ConcurrentHashMap<>();
    private static final Gson SERIALIZER = new Gson();

    public WebsocketSessionHandler(){
    }

    public void addSessionToGame(String gameID, Session session){
        if(gameID == null){return;}
        sessions.computeIfAbsent(gameID,id -> new ArrayList<>()).add(session);
    }

    public void leaveGame(String gameID, Session session){
        if(gameID == null){return;}
        List<Session> sessionList = sessions.get(gameID);
        if(sessionList!= null) {
            sessionList.remove(session);
            if (sessionList.isEmpty()) {
                sessions.remove(gameID);
            }
        }
    }

    public void switchGame(String oldGameID,String newGameID, Session session){
        leaveGame(oldGameID, session);
        addSessionToGame(newGameID, session);
    }

    public void broadcastGameUpdate(GameData gameData) throws IOException {
        List<Session> watchers = sessions.get(gameData.gameID());
        ServerMessage messageObj = new ServerMessage(gameData);
        String message = SERIALIZER.toJson(messageObj);
        broadcast(null, message,watchers);
    }


    public void broadcastNotification(Session s, ServerMessage.NotificationType type, String msg, String gameID) throws IOException {
        List<Session> watchers = sessions.get(gameID);
        ServerMessage messageObj = new ServerMessage(type,msg);
        String message = SERIALIZER.toJson(messageObj);
        broadcast(s,message,watchers);
    }

    private void broadcast(Session ignored, String message, List<Session> watchers) throws IOException {
        LOGGER.info("Message broadcasted:\n"+message);
        for(Session s: watchers){
            if(s.isOpen() && !s.equals(ignored)) {
                s.getRemote().sendString(message);
            }
        }
    }

    public static void sendErrorMessage(Session session, ServerMessage message) throws IOException {
        String json = SERIALIZER.toJson(message);
        LOGGER.info(String.format("Error message returned to user:\n%s", json));
        session.getRemote().sendString(json);
    }

    public static void sendMessage(Session session, ServerMessage message) throws IOException {
        String json = SERIALIZER.toJson(message);
        LOGGER.info(String.format("Message to user:\n%s", json));
        session.getRemote().sendString(json);
    }

    public void closeConnections(){
        closeConnections(OK,"Websocket connection closed");
    }

    public void closeConnections(int statusCode, String reason){
        Collection<Session> allSessions = new ArrayList<>();
        sessions.values().forEach(allSessions::addAll);
        for(Session s : allSessions){
            s.close(statusCode, reason);
        }
    }
}
