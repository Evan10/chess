package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece cp = board.getPiece(myPosition);
        return switch (cp.type) {
            case KING -> kingMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case BISHOP -> bishopMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
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
                if (p == null || p.pieceColor != myPiece.pieceColor) {
                    cm.add(new ChessMove(myPosition, cp, null));
                }
            }
        }
        return cm;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
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

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        cm.addAll(getValidInLine(board, ChessDirection.UP, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.RIGHT, myPosition));
        return cm;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
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
                if (p == null || p.pieceColor != myPiece.pieceColor) {
                    cm.add(new ChessMove(myPosition, cp, null));
                }
            }
        }

        return cm;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        cm.addAll(getValidInLine(board, ChessDirection.UP_LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.UP_RIGHT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN_LEFT, myPosition));
        cm.addAll(getValidInLine(board, ChessDirection.DOWN_RIGHT, myPosition));
        return cm;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> cm = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        /*
         *     |  |X |  |
         *     |O |X |O |
         *     |  |P |  |
         *     |  |  |  |
         *
         * */

        int direction = myPiece.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;

        ChessPosition straight = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        ChessPosition doubleStraight = new ChessPosition(myPosition.getRow() + (direction * 2), myPosition.getColumn());
        ChessPosition left = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() - 1);
        ChessPosition right = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + 1);

        if (straight.isValid()) {
            ChessPiece p = board.getPiece(straight);
            if (p == null) {
                if (willPawnPromote(straight, myPiece.pieceColor)) {
                    cm.addAll(pawnPromotions(myPosition, straight));
                } else {
                    cm.add(new ChessMove(myPosition, straight, null));
                    if (doubleStraight.isValid() && pawnCanLongJump(board, myPosition)) {
                        p = board.getPiece(doubleStraight); // double can only occur if a straight move is valid
                        if (p == null) {
                            cm.add(new ChessMove(myPosition, doubleStraight, null));
                        }
                    }
                }
            }
        }

        if (left.isValid()) {
            ChessPiece p = board.getPiece(left);
            if (p != null && p.pieceColor != myPiece.pieceColor) {
                if (willPawnPromote(left, myPiece.pieceColor)) {
                    cm.addAll(pawnPromotions(myPosition, left));
                } else {
                    cm.add(new ChessMove(myPosition, left, null));
                }
            }
        }

        if (right.isValid()) {
            ChessPiece p = board.getPiece(right);
            if (p != null && p.pieceColor != myPiece.pieceColor) {
                if (willPawnPromote(right, myPiece.pieceColor)) {
                    cm.addAll(pawnPromotions(myPosition, right));
                } else {
                    cm.add(new ChessMove(myPosition, right, null));
                }
            }
        }

        return cm;
    }

    private boolean willPawnPromote(ChessPosition cp, ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE ? cp.getRow() == 8 : cp.getRow() == 1;
    }

    private boolean pawnCanLongJump(ChessBoard board, ChessPosition myPosition) {
        ChessPiece cp = board.getPiece(myPosition);
        if (cp.type != PieceType.PAWN) {
            return false;
        } else {
            return cp.pieceColor == ChessGame.TeamColor.WHITE ? myPosition.getRow() == 2 : myPosition.getRow() == 7;
        }
    }

    private Collection<ChessMove> pawnPromotions(ChessPosition start, ChessPosition end) {
        return List.of(new ChessMove(start, end, PieceType.BISHOP),
                new ChessMove(start, end, PieceType.KNIGHT),
                new ChessMove(start, end, PieceType.ROOK),
                new ChessMove(start, end, PieceType.QUEEN));
    }

    private Collection<ChessMove> getValidInLine(ChessBoard board, ChessDirection dir, ChessPosition pos) {
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
            } else if (p.pieceColor != myPiece.pieceColor) {
                cm.add(new ChessMove(pos, cp, null));
                return cm;
            } else {
                return cm;
            }
        }
        return cm;
    }


    @Override
    public String toString() {
        return "[" + pieceColor + ", " + type + "]";
    }

    public String toStringShort() {
        String color = pieceColor == ChessGame.TeamColor.WHITE ? "W" : "B";
        String t = switch (type) {
            case PieceType.KING -> "K";
            case PieceType.QUEEN -> "Q";
            case PieceType.ROOK -> "R";
            case PieceType.KNIGHT -> "Kn";
            case PieceType.BISHOP -> "B";
            case PieceType.PAWN -> "P";
        };
        return color + ":" + t;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
