package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow(), col = myPosition.getColumn();
        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();
        switch (piece.type){
            case PieceType.KING:
                for(int r = row - 1; r <= row+1;r++ ){
                    for(int c = col -1; c <= col+1;c++){
                        if(r==row && c==col) continue;
                        ChessPosition pos = new ChessPosition(r,c);
                        if(!pos.isValid()){continue;}
                        ChessPiece cp = board.getPiece(pos);
                        if(cp == null || !cp.pieceColor.equals(piece.pieceColor)){
                            ChessMove cm = new ChessMove(myPosition,pos,null);
                            validMoves.add(cm);
                        }
                    }
                }
                break;
            case PieceType.QUEEN:
                for(int r = - 1; r <= 1;r++ ){
                    for(int c = -1; c <= 1;c++){
                        if(r==0 && c==0) continue;
                        validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(r,c)));
                    }
                }
                break;
            case PieceType.ROOK:
                validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(1,0)));
                validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(-1,0)));
                validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(0,1)));
                validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(0,-1)));
                break;
            case PieceType.BISHOP:
                validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(1,1)));
                validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(-1,-1)));
                validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(-1,1)));
                validMoves.addAll(movesInLine(board,myPosition,new ChessMoveDirection(1,-1)));
                break;
            case PieceType.KNIGHT:

                break;
            case PieceType.PAWN:
                if(piece.pieceColor.equals(ChessGame.TeamColor.WHITE)){

                }else{

                }
                break;
            default:
                break;
        }

        return validMoves;
    }

    private Collection<ChessMove> movesInLine(ChessBoard board, ChessPosition myPosition,ChessMoveDirection dir){
        if(!dir.isValid()){throw new RuntimeException();}
        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessPosition cp = myPosition;
        for(int i = 0; i < 7;i++){
            cp = new ChessPosition(cp.getRow() +dir.getY(),
                    cp.getColumn()+ dir.getX() );
            if(!cp.isValid()) break;
            ChessPiece chessPiece = board.getPiece(cp);
            if(chessPiece == null){
                moves.add(new ChessMove(myPosition,cp,null));
            }else if(isSameTeam(chessPiece)){
                break;
            }else{
                moves.add(new ChessMove(myPosition,cp,null));
                break;
            }
        }
        return moves;
    }

    public boolean isSameTeam(ChessPiece chessPiece){
        return this.pieceColor.equals(chessPiece.pieceColor);
    }

    @Override
    public String toString() {
        return "ChessPiece{" + pieceColor + " " + type + '}';
    }
}
