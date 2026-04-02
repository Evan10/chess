package websocket.messages;

import model.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    GameData game;

    NotificationType notificationType;
    String message;


    String errorMessage;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public enum NotificationType {
        CHESS_MOVE,
        USER_CONNECT,
        USER_LEAVE,
        WHITE_IN_CHECK,
        BLACK_IN_CHECK,
        OPPONENT_RESIGN,
        YOU_RESIGN,
        BLACK_WIN,
        WHITE_WIN,
        DRAW_GAME,
        PRINT_INFO
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessage(GameData gameData) {
        this.serverMessageType = ServerMessageType.LOAD_GAME;
        this.game =gameData;
    }

    public ServerMessage(ServerMessageType type, String message) {
        this.serverMessageType = type;
        if(type.equals(ServerMessageType.ERROR)){
            this.errorMessage = message;
        }else {
            this.message = message;
        }
    }

    public ServerMessage(NotificationType type, String message) {
        this.serverMessageType = ServerMessageType.NOTIFICATION;
        this.notificationType = type;
        this.message=message;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public GameData getGame() {
        return game;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
