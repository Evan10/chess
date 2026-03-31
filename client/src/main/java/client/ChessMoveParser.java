package client;

import chess.*;

public class ChessMoveParser {

    public static void main(String[] args) throws InvalidMoveException {
        ChessMove move = parseChessMove("b2","c2","");
        System.out.println(move);
    }



    public static ChessMove parseChessMove(String start, String end, String promotion) throws InvalidMoveException {
        ChessPosition startPos = parseChessPosition(start);
        ChessPosition endPos = parseChessPosition(end);
        ChessPiece.PieceType type = parsePieceType(promotion);
        if(!startPos.isValid() || !endPos.isValid()){
            throw new InvalidMoveException("Invalid start or end position");
        }
        return new ChessMove(startPos,endPos,type);
    }

    public static ChessPosition parseChessPosition(String position) throws InvalidMoveException {
        if(position.length() != 2){
            throw new InvalidMoveException("Invalid position given; must be of format [a-h][1-8]");
        }
        String lowercasePosition = position.toLowerCase();
        String letter = lowercasePosition.substring(0,1);
        String number = lowercasePosition.substring(1,2);

        int letterIndex = chessLetterToNumber(letter);
        int numberIndex = Integer.parseInt(number);

        return new ChessPosition(numberIndex, letterIndex);
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
