package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClientSessionData {

    private ClientState state;
    private AuthData authData;


    private final Map<String,Integer> gameIDPositionMap;
    private final Map<Integer, GameData> positionGameDataMap;

    private static int nextPosition = 0;
    private String currentGameID;


    private ChessGame.TeamColor color = ChessGame.TeamColor.WHITE; // default to white for observing games
    private GameData currentGame;

    ClientSessionData(){
        gameIDPositionMap = new HashMap<>();
        positionGameDataMap = new HashMap<>();
    }

    public ClientState getState() {
        return state;
    }

    public void setState(ClientState state) {
        this.state = state;
    }

    public AuthData getAuthData() {
        return authData;
    }

    public void setAuthData(AuthData authData) {
        this.authData = authData;
    }

    public Map<Integer, GameData> getGames() {
        return positionGameDataMap;
    }

    public String getGameIDFromPosition(int position){
        return positionGameDataMap.get(position).gameID();
    }

    public boolean isValidGame(int position){
        return positionGameDataMap.containsKey(position);
    }

    public GameData getGameFromCache(int position){
        return positionGameDataMap.getOrDefault(position, null);
    }

    public void addGames(Collection<GameData> games) {
        for(GameData g: games){
            int position;
            if(!gameIDPositionMap.containsKey(g.gameID())){
                position = getNextPosition();
                gameIDPositionMap.put(g.gameID(),position);
            }else {
                position = gameIDPositionMap.get(g.gameID());
            }
            positionGameDataMap.put(position, g);
        }
    }

    public GameData getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(GameData currentGame) {
        currentGameID = currentGame.gameID();
        this.currentGame = currentGame;
    }

    public String getCurrentGameID(){
        return currentGameID;
    }
    public void setCurrentGameID(String gameID){
        this.currentGameID = gameID;
    }

    public int getNextPosition(){
        return nextPosition++;
    }

    public ChessGame.TeamColor getColor() {
        return color;
    }

    public void setColor(ChessGame.TeamColor color) {
        this.color = color;
    }
}
