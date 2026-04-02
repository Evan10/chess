package websocket.results;

import chess.ChessGame;

public record ResignResult(int statusCode, String message, ChessGame.TeamColor teamToResign) {
}
