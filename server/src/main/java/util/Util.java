package util;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class Util {

    public static String newUUID() {
        return UUID.randomUUID().toString();
    }

    public static int newIntID() {
        return Math.abs(new Random().nextInt());
    }

    public record PasswordValidationResult(String reason, boolean isValid) {
    }

    private static final Pattern INVALID_CHARS = Pattern.compile("[\\[\\](){}<>|?*\"',.\\-]");
    private static final Pattern NUMBER = Pattern.compile("\\d");
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern SYMBOL = Pattern.compile("[!@$~%#&^]");
    private static final int MINIMUM_PASSWORD_LENGTH = 6;

    public static PasswordValidationResult isValidPassword(String password) {

        if (password == null || password.isBlank()) {
            return new PasswordValidationResult("Password cannot be blank", false);
        }

        boolean isValid = true;
        StringBuilder reason = new StringBuilder();

        if (INVALID_CHARS.matcher(password).find()) {
            isValid = false;
            reason.append("Password cannot contain invalid character: [](){}<>|?*\"',.- \n");
        }
        if (!NUMBER.matcher(password).find()) {
            isValid = false;
            reason.append("Password must contain number \n");
        }
        if (!UPPER.matcher(password).find()) {
            isValid = false;
            reason.append("Password must contain uppercase letter \n");
        }
        if (!LOWER.matcher(password).find()) {
            isValid = false;
            reason.append("Password must contain lowercase letter \n");
        }
        if (!SYMBOL.matcher(password).find()) {
            isValid = false;
            reason.append("Password must contain symbol: !@$~%#&^ \n");
        }
        if (password.length() < MINIMUM_PASSWORD_LENGTH) {
            isValid = false;
            reason.append("Password must contain at least " + MINIMUM_PASSWORD_LENGTH + "characters \n");
        }

        return new PasswordValidationResult(reason.toString(), isValid);
    }


    public static boolean isPlayer(String username, GameData game){
        if(username == null){return false;}
        return username.equals(game.whiteUsername()) || username.equals(game.blackUsername());
    }
    public static ChessGame.TeamColor getTeamColor(String username, GameData game){
        if(!isPlayer(username, game)) {return null;}
        return username.equals(game.whiteUsername())? ChessGame.TeamColor.WHITE: ChessGame.TeamColor.BLACK;
    }

    public static String humanReadableChessMove(ChessMove move){
        ChessPosition start = move.getStartPosition(),end = move.getEndPosition();
        return String.format("%s%s %s%s %s",
                numberToLetter(start.getColumn()),
                start.getRow(),
                numberToLetter(end.getColumn()),
                end.getRow(),
                chessPieceToString(move.getPromotionPiece())
        );
    }

    private static char numberToLetter(int n){
        return (char) ('a' + (n-1));
    }

    private static String chessPieceToString(ChessPiece.PieceType type){
        if(type == null) {return "";}
        return switch (type) {
            case ChessPiece.PieceType.KING -> "K";
            case ChessPiece.PieceType.QUEEN -> "Q";
            case ChessPiece.PieceType.ROOK -> "R";
            case ChessPiece.PieceType.KNIGHT -> "Kn";
            case ChessPiece.PieceType.BISHOP -> "B";
            case ChessPiece.PieceType.PAWN -> "P";
        };
    }
}
