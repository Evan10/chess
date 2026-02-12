package chess;

import java.util.Collection;
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
            case KING -> LegalMoveUtil.kingMoves(board, myPosition);
            case QUEEN -> LegalMoveUtil.queenMoves(board, myPosition);
            case ROOK -> LegalMoveUtil.rookMoves(board, myPosition);
            case KNIGHT -> LegalMoveUtil.knightMoves(board, myPosition);
            case BISHOP -> LegalMoveUtil.bishopMoves(board, myPosition);
            case PAWN -> LegalMoveUtil.pawnMoves(board, myPosition);
        };
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
