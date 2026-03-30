package ui;

import chess.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class UIChessBoardHelper {

    public static void main(String[] args){
        ChessGame chessGame = new ChessGame();
        System.out.printf(uiChessBoard(chessGame,ChessGame.TeamColor.WHITE,
                chessGame.validMoves(new ChessPosition(2,7))));
    }


    public static String uiChessBoard(ChessGame chessGame, ChessGame.TeamColor color, Collection<ChessMove> legalMoves){
        StringBuilder uiBoard = new StringBuilder();
        ChessBoard board = chessGame.getBoard();
        boolean orientation = color.equals(ChessGame.TeamColor.WHITE);
        boolean tileBlack = false;
        uiBoard.append(drawNumbers(orientation));
        int xVal, yVal;

        ChessPosition start = legalMoves != null && legalMoves.iterator().hasNext()
                ? legalMoves.iterator().next().getStartPosition()
                : null;
        for(int y = 1; y<=8 ; y++){
            yVal = orientation? 9-y:y;
            uiBoard.append(drawLetter(yVal));
            for(int x = 1; x<=8 ; x++){
                xVal = orientation?x:9-x;
                ChessPosition position = new ChessPosition(yVal,xVal);
                boolean legalMove = legalMoves != null && legalMoves.stream()
                        .anyMatch((chessMove -> chessMove.getEndPosition()
                                .equals(position)));
                String piece = chessPieceToCharacter(board.getPiece(position));
                boolean isStart = start!= null && start.equals(position);
                uiBoard.append(setBGColor(isStart,legalMove,tileBlack));
                tileBlack = !tileBlack;
                uiBoard.append(piece);
            }
            uiBoard.append(drawLetter(yVal));
            uiBoard.append("\n");
            tileBlack = !tileBlack;
            uiBoard.append(resetAll());
        }
        uiBoard.append(drawNumbers(orientation));
        return uiBoard.toString();
    }

    public static String chessPieceToCharacter(ChessPiece piece){
        if(piece == null){
            return EscapeSequences.EMPTY;
        }
        boolean isWhite = piece.getTeamColor().equals(ChessGame.TeamColor.WHITE);
        String textColor = isWhite? EscapeSequences.SET_TEXT_COLOR_BLUE:EscapeSequences.SET_TEXT_COLOR_RED;
        return  textColor + switch (piece.getPieceType()){
            case PAWN ->EscapeSequences.BLACK_PAWN;
            case ROOK ->EscapeSequences.BLACK_ROOK;
            case BISHOP ->EscapeSequences.BLACK_BISHOP;
            case KING ->EscapeSequences.BLACK_KING;
            case KNIGHT ->EscapeSequences.BLACK_KNIGHT;
            case QUEEN ->EscapeSequences.BLACK_QUEEN;
        };
    }

    private static String resetAll(){
        return EscapeSequences.RESET_TEXT_BOLD_FAINT
                + EscapeSequences.RESET_TEXT_COLOR
                + EscapeSequences.RESET_BG_COLOR
                + EscapeSequences.RESET_TEXT_ITALIC
                + EscapeSequences.RESET_TEXT_UNDERLINE
                + EscapeSequences.RESET_TEXT_BLINKING;
    }

    public static String drawLetter(int n){
        return resetAll()
                +(EscapeSequences.SET_BG_COLOR_BLACK)
                + EscapeSequences.SET_TEXT_BOLD
                + EscapeSequences.SET_TEXT_COLOR_WHITE
                + String.format(" %-2s",numberToChessLetter(n));
    }
    public static String drawNumbers(boolean orientation){
        return resetAll()
                +(EscapeSequences.SET_BG_COLOR_BLACK)
                + EscapeSequences.SET_TEXT_COLOR_WHITE
                + EscapeSequences.SET_TEXT_BOLD
                + EscapeSequences.EMPTY
                + (orientation ? " 1  2   3   4  5   6  7   8":" 8  7   6   5  4   3  2   1")
                + EscapeSequences.EMPTY
                +"\n"
                + resetAll();
    }


    public static String numberToChessLetter(int n){
        return switch (n){
            case 1 ->"a";
            case 2 ->"b";
            case 3 ->"c";
            case 4 ->"d";
            case 5 ->"e";
            case 6 ->"f";
            case 7 ->"g";
            case 8 ->"h";
            default -> EscapeSequences.EMPTY;
        };
    }


    public static String setBGColor(boolean start,boolean legalMove, boolean isBlack){
        if(start){
            return EscapeSequences.SET_BG_COLOR_MAGENTA;
        } else if(legalMove){
            return EscapeSequences.SET_BG_COLOR_GREEN;
        } else {
            return isBlack ? EscapeSequences.SET_BG_COLOR_YELLOW : EscapeSequences.SET_BG_COLOR_WHITE;
        }
    }
}
