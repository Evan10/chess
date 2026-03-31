package websocket.results;

import model.GameData;

public record MakeMoveResult(int statusCode, String message, GameData gameData) {
}
