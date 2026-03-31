package client;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.InvalidMoveException;

public class ChessMoveParser {

    public static void main(String[] args){
        System.out.println(chessLetterToNumber("a"));
        System.out.println(chessLetterToNumber("b"));
        System.out.println(chessLetterToNumber("h"));
    }

    public static ChessMove parseChessMove(String start, String end, String promotion){

        return null;
    }

    private static ChessPosition parseChessPosition(String position) throws InvalidMoveException {
        if(position.length() != 2){
            throw new InvalidMoveException("Invalid position given; must be of format [a-h][1-8]");
        }

        return null;
    }

    private static ChessPiece.PieceType parsePieceType(String piece) throws InvalidMoveException {
        if(piece.isBlank()){return null;}
        return switch (piece.toLowerCase()) {
            case ("knight"), ("horse"), ("kn"), ("h") -> ChessPiece.PieceType.KNIGHT;
            case ("king"), ("k") -> ChessPiece.PieceType.KING;
            case ("queen"), ("q") -> ChessPiece.PieceType.QUEEN;
            case ("pawn"), ("p") -> ChessPiece.PieceType.PAWN;
            case ("bishop"), ("b") -> ChessPiece.PieceType.BISHOP;
            case ("rook"), ("r"), ("castle"), ("c") -> ChessPiece.PieceType.ROOK;
            default -> throw new InvalidMoveException("Invalid piece type");
        };
    }

    /*
    * In Unicode the lowercase letters start with 'a' at 97
    * thus by subtracting 96 it returns the correct number from a to z
    * number from other Unicode values has no meaning in this instance
    * */
    private static int chessLetterToNumber(String letter){
        int c = letter.toLowerCase().charAt(0);
        return c - 96;
    }
}
