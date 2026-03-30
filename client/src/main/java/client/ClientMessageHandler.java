package client;

import com.google.gson.Gson;
import jakarta.websocket.MessageHandler;
import websocket.messages.ServerMessage;

public class ClientMessageHandler implements MessageHandler.Whole<String> {

    public static final Gson DESERIALIZER = new Gson();

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

    }

    private void handleNotification(ServerMessage message){

    }

    private void handlerError(ServerMessage message){

    }
}
