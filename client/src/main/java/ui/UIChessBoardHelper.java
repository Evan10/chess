package ui;

import chess.*;

import java.util.Collection;

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
        uiBoard.append(drawLetters(orientation));
        int xVal, yVal;

        ChessPosition start = legalMoves != null && legalMoves.iterator().hasNext()
                ? legalMoves.iterator().next().getStartPosition()
                : null;
        for(int y = 1; y<=8 ; y++){
            yVal = orientation? 9-y:y;
            uiBoard.append(drawNumber(yVal));
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
            uiBoard.append(drawNumber(yVal));
            uiBoard.append("\n");
            tileBlack = !tileBlack;
            uiBoard.append(resetAll());
        }
        uiBoard.append(drawLetters(orientation));
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

    public static String drawNumber(int n){
        return resetAll()
                +(EscapeSequences.SET_BG_COLOR_BLACK)
                + EscapeSequences.SET_TEXT_BOLD
                + EscapeSequences.SET_TEXT_COLOR_WHITE
                + String.format(" %-2s",n);
    }
    public static String drawLetters(boolean orientation){
        return resetAll()
                +(EscapeSequences.SET_BG_COLOR_BLACK)
                + EscapeSequences.SET_TEXT_COLOR_WHITE
                + EscapeSequences.SET_TEXT_BOLD
                + EscapeSequences.EMPTY
                + (orientation ? " a  b   c   d  e   f  g   h":" h  g   f   e  d   c  b   a")
                + EscapeSequences.EMPTY
                +"\n"
                + resetAll();
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
