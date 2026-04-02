package websocket.results;

import model.GameData;

public record LeaveGameResult(int statusCode, String message, GameData gameData) {
}
