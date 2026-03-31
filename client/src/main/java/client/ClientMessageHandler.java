package client;

import com.google.gson.Gson;
import jakarta.websocket.MessageHandler;
import model.GameData;
import websocket.messages.ServerMessage;

public class ClientMessageHandler implements MessageHandler.Whole<String> {

    public static final Gson DESERIALIZER = new Gson();
    private final ClientSessionData sessionData;
    private final ConsoleWriter consoleWriter;

    ClientMessageHandler(ClientSessionData sessionData, ConsoleWriter consoleWriter){
        this.sessionData=sessionData;
        this.consoleWriter=consoleWriter;
    }

    @Override
    public void onMessage(String s) {
        ServerMessage message = DESERIALIZER.fromJson(s, ServerMessage.class);
        switch (message.getServerMessageType()){
            case LOAD_GAME -> handleLoadGame(message);
            case NOTIFICATION -> handleNotification(message);
            case ERROR -> handlerError(message);
        }
    }


    private void handleLoadGame(ServerMessage message){
        GameData gameData = message.getGameData();
        if(gameData== null || sessionData.getCurrentGameID().equals(gameData.gameID())){
            return;
        }
        sessionData.setCurrentGame(gameData);
        consoleWriter.writeBoard(gameData.game());
        if(sessionData.getColor().equals(gameData.game().getTeamTurn())){
            consoleWriter.writeMessage("Your turn");
        }else{
            consoleWriter.writeMessage("Opponents turn");
        }
    }

    private void handleNotification(ServerMessage message){
        ServerMessage.NotificationType type  = message.getNotificationType();
        if(type == null){return;}
        consoleWriter.writeMessage(message.getMessage());
    }

    private void handlerError(ServerMessage message){
        consoleWriter.writeErrorMessage(message.getMessage());
    }
}
