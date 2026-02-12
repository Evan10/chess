package chess;

import java.util.Collection;
import java.util.List;

public class kingTargetedUtil {

    kingTargetedUtil(){
    }

    public static boolean pieceTargeted(ChessBoard board, ChessPosition myPosition){
        Collection<ChessDirection> directions = List.of(ChessDirection.UP,
                ChessDirection.DOWN,ChessDirection.LEFT,
                ChessDirection.RIGHT,ChessDirection.DOWN_LEFT,
                ChessDirection.DOWN_RIGHT,ChessDirection.UP_RIGHT,
                ChessDirection.UP_LEFT);
        for(ChessDirection dir : directions){// Checks for Queen, Rook and Bishop check
            if(isTargetedLine(board,dir,myPosition)) return true;
        }

        return underKnightTarget(board,myPosition) ||
                underKingTarget(board, myPosition) ||
                underPawnTarget(board, myPosition);
    }

    private static boolean isTargetedLine(ChessBoard board, ChessDirection dir, ChessPosition pos){
        ChessPiece myPiece = board.getPiece(pos);
        if(myPiece==null) throw new RuntimeException("Null returned where ChessPiece was expected");

        ChessPosition cp = pos;
        boolean isDiagonal = dir.getX() != 0 && dir.getY() != 0;

        for (int i = 0; i < 8; i++) {
            cp = new ChessPosition(cp.getRow() + dir.getY(), cp.getColumn() + dir.getX());
            if (!cp.isValid()) {
                return false;
            }
            ChessPiece p = board.getPiece(cp);
            if(p == null) continue;
            if (p.getTeamColor() != myPiece.getTeamColor()) {
                if ((isDiagonal?
                        (p.getPieceType() == ChessPiece.PieceType.BISHOP):
                        (p.getPieceType() == ChessPiece.PieceType.ROOK)) ||
                        p.getPieceType() == ChessPiece.PieceType.QUEEN){
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private static boolean underKnightTarget(ChessBoard board, ChessPosition pos){
        ChessPiece myPiece = board.getPiece(pos);
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y += 2) {
                if (x == 0) {
                    continue;
                }
                int newX = pos.getColumn() + x;
                int newY = pos.getRow() + (y * (Math.abs(x) > 1 ? 1 : 2));
                ChessPosition cp = new ChessPosition(newY, newX);
                if (!cp.isValid()) {
                    continue;
                }
                ChessPiece p = board.getPiece(cp);
                if(p!= null && p.getTeamColor()!=myPiece.getTeamColor()){
                    if(p.getPieceType() == ChessPiece.PieceType.KNIGHT){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean underKingTarget(ChessBoard board, ChessPosition pos){
        ChessPiece myPiece = board.getPiece(pos);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) continue;

                ChessPosition cp = new ChessPosition(pos.getRow() + y, pos.getColumn() + x);
                if (!cp.isValid()) continue;

                ChessPiece p = board.getPiece(cp);
                if (p != null && p.getTeamColor() != myPiece.getTeamColor()) {
                    if(p.getPieceType()== ChessPiece.PieceType.KING) return true;
                }
            }
        }
        return false;
    }

    private static boolean underPawnTarget(ChessBoard board, ChessPosition pos){
        ChessPiece myPiece = board.getPiece(pos);
        int direction = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;

        ChessPosition left = new ChessPosition(pos.getRow() + direction, pos.getColumn() - 1);
        ChessPosition right = new ChessPosition(pos.getRow() + direction, pos.getColumn() + 1);
        if(left.isValid()){
            ChessPiece piece = board.getPiece(left);
            if(piece !=null && piece.getTeamColor() !=myPiece.getTeamColor() && piece.getPieceType() == ChessPiece.PieceType.PAWN){
                return true;
            }
        }
        if(right.isValid()){
            ChessPiece piece = board.getPiece(right);
            return piece != null && piece.getTeamColor() != myPiece.getTeamColor() && piece.getPieceType() == ChessPiece.PieceType.PAWN;
        }

        return false;
    }

}
