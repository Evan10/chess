package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LegalMoveUtil {

    LegalMoveUtil() {
    }

    public static Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        /*
         *       |  |  |  |
         *       |  |K |  |
         *       |  |  |  |
         * */
        ChessPosition cp;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                cp = new ChessPosition(myPosition.getRow() + y, myPosition.getColumn() + x);
                if (!cp.isValid()) {
                    continue;
                }
                ChessPiece p = board.getPiece(cp);
                if (p == null || p.getTeamColor() != myPiece.getTeamColor()) {
                    cm.add(new ChessMove(myPosition, cp, null));
                }
            }
        }
        return cm;
    }

    public static Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        cm.addAll(getValidInLine(board, ChessDirection.UP, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.RIGHT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.UP_LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.UP_RIGHT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN_LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN_RIGHT, myPosition));
        return cm;
    }

    public static Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        cm.addAll(getValidInLine(board, ChessDirection.UP, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.RIGHT, myPosition));
        return cm;
    }

    public static Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        /*
         *     |  |X |  |X |  |
         *     |X |  |  |  |X |
         *     |  |  |Kn|  |  |
         *     |X |  |  |  |X |
         *     |  |X |  |X |  |
         * */

        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y += 2) {
                if (x == 0) {
                    continue;
                }
                int newX = myPosition.getColumn() + x;
                int newY = myPosition.getRow() + (y * (Math.abs(x) > 1 ? 1 : 2));
                ChessPosition cp = new ChessPosition(newY, newX);
                if (!cp.isValid()) {
                    continue;
                }
                ChessPiece p = board.getPiece(cp);
                if (p == null || p.getTeamColor() != myPiece.getTeamColor()) {
                    cm.add(new ChessMove(myPosition, cp, null));
                }
            }
        }

        return cm;
    }

    public static Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        cm.addAll(getValidInLine(board, ChessDirection.UP_LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.UP_RIGHT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN_LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN_RIGHT, myPosition));
        return cm;
    }

    public static Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        /*
         *     |  |X |  |
         *     |O |X |O |
         *     |  |P |  |
         *     |  |  |  |
         *
         * */

        int direction = myPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;

        ChessPosition straight = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        ChessPosition doubleStraight = new ChessPosition(myPosition.getRow() + (direction * 2), myPosition.getColumn());
        ChessPosition left = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() - 1);
        ChessPosition right = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + 1);

        if (straight.isValid()) {
            ChessPiece p = board.getPiece(straight);
            if (p == null) {
                if (willPawnPromote(straight, myPiece.getTeamColor())) {
                    cm.addAll(pawnPromotions(myPosition, straight));
                } else {
                    cm.add(new ChessMove(myPosition, straight, null));
                    if (doubleStraight.isValid() && pawnCanLongJump(board, myPosition) && board.getPiece(doubleStraight) == null) {
                        cm.add(new ChessMove(myPosition, doubleStraight, null));
                    }
                }
            }
        }

        if (left.isValid()) {
            ChessPiece p = board.getPiece(left);
            if (p != null && p.getTeamColor() != myPiece.getTeamColor()) {
                if (willPawnPromote(left, myPiece.getTeamColor())) {
                    cm.addAll(pawnPromotions(myPosition, left));
                } else {
                    cm.add(new ChessMove(myPosition, left, null));
                }
            }
        }

        if (right.isValid()) {
            ChessPiece p = board.getPiece(right);
            if (p != null && p.getTeamColor() != myPiece.getTeamColor()) {
                if (willPawnPromote(right, myPiece.getTeamColor())) {
                    cm.addAll(pawnPromotions(myPosition, right));
                } else {
                    cm.add(new ChessMove(myPosition, right, null));
                }
            }
        }

        return cm;
    }

    private static boolean willPawnPromote(ChessPosition cp, ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE ? cp.getRow() == 8 : cp.getRow() == 1;
    }

    private static boolean pawnCanLongJump(ChessBoard board, ChessPosition myPosition) {
        ChessPiece cp = board.getPiece(myPosition);
        if (cp.getPieceType() != ChessPiece.PieceType.PAWN) {
            return false;
        } else {
            return cp.getTeamColor() == ChessGame.TeamColor.WHITE ? myPosition.getRow() == 2 : myPosition.getRow() == 7;
        }
    }

    private static Collection<ChessMove> pawnPromotions(ChessPosition start, ChessPosition end) {
        return List.of(new ChessMove(start, end, ChessPiece.PieceType.BISHOP), new ChessMove(start, end, ChessPiece.PieceType.KNIGHT), new ChessMove(start, end, ChessPiece.PieceType.ROOK), new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
    }

    private static Collection<ChessMove> getValidInLine(ChessBoard board, ChessDirection dir, ChessPosition pos) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(pos);
        ChessPosition cp = pos;
        for (int i = 0; i < 8; i++) {
            cp = new ChessPosition(cp.getRow() + dir.getY(), cp.getColumn() + dir.getX());
            if (!cp.isValid()) {
                return cm;
            }
            ChessPiece p = board.getPiece(cp);
            if (p == null) {
                cm.add(new ChessMove(pos, cp, null));
            } else if (p.getTeamColor() != myPiece.getTeamColor()) {
                cm.add(new ChessMove(pos, cp, null));
                return cm;
            } else {
                return cm;
            }
        }
        return cm;
    }
}
