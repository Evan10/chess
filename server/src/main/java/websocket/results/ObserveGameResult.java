package websocket.results;

import model.GameData;

public record ObserveGameResult(int statusCode, String message, GameData gameData) {
}
