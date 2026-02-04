package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessBoard cboard;
    TeamColor teamTurn;
    public ChessGame() {
        cboard = new ChessBoard();
        cboard.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public void toggleTeamTurn(){
        teamTurn = teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = cboard.getPiece(startPosition);
        if(piece == null) return List.of();
        Collection<ChessMove> potentialMoves = piece.pieceMoves(cboard,startPosition);
        ArrayList<ChessMove> allowedMoves = new ArrayList<>();
        for(ChessMove move: potentialMoves){
            ChessBoard testBoard = cboard.copy();

            try {
                testBoard.movePiece(move);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }

            ChessPosition KingPos = testBoard.getKingPosition(piece.getTeamColor());
            if(!KingPos.isValid()) throw new RuntimeException("King not found");
            if(!ChessPiece.pieceTargeted(testBoard,KingPos)){
                allowedMoves.add(move);
            }
        }
        return allowedMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        ChessPiece piece = cboard.getPiece(move.getStartPosition());
        if(!legalMoves.contains(move)){
            throw new InvalidMoveException("The move "+ move +" is invalid");
        }else if(piece.getTeamColor() != getTeamTurn()){
            throw new InvalidMoveException("Incorrect player made a move");
        }
        else {
            cboard.movePiece(move);
            toggleTeamTurn();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition KingPos = cboard.getKingPosition(teamColor);
        if(!KingPos.isValid()) throw new RuntimeException("King not found");
        return ChessPiece.pieceTargeted(cboard,KingPos);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return noValidMoves(teamColor) && isInCheck(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return noValidMoves(teamColor) && !isInCheck(teamColor);
    }

    private boolean noValidMoves(TeamColor teamColor){
        Collection<ChessPosition> teamPieceLocs = cboard.getTeamPieceLocs(teamColor);
        if(teamPieceLocs.isEmpty()) return true;
        for(ChessPosition pos : teamPieceLocs){
            if(!validMoves(pos).isEmpty()) return false;
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        cboard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return cboard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(cboard, chessGame.cboard) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cboard, teamTurn);
    }
}
