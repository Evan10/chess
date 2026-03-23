package client;

import chess.ChessGame;

public class ClientUtils {
    public static String buildConsoleChessboard(){
        return "";
    }


    public static ChessGame.TeamColor stringToTeamColor(String str) throws IllegalArgumentException{
        String team = str.toLowerCase();
        if(team.equals("black") || team.equals("b")){
            return ChessGame.TeamColor.BLACK;
        }else if(team.equals("white") || team.equals("w")){
            return ChessGame.TeamColor.WHITE;
        }else{
            throw new IllegalArgumentException("Invalid team color was provided");
        }
    }
}
