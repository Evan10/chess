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
    GameData gameData;

    NotificationType notificationType;
    String message;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public enum NotificationType {
        OPPONENT_RESIGN,
        YOU_RESIGN,
        YOU_WIN,
        YOU_LOSE,
        YOU_DRAW_GAME,
        PRINT_INFO
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessage(GameData gameData) {
        this.serverMessageType = ServerMessageType.LOAD_GAME;
        this.gameData=gameData;
    }

    public ServerMessage(ServerMessageType type, String message) {
        this.serverMessageType = type;
        this.message=message;
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

    public GameData getGameData() {
        return gameData;
    }

    public String getMessage() {
        return message;
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
