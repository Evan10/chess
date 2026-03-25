package client;

import model.AuthData;
import model.GameData;

import java.util.Collection;

public class ClientSessionData {

    private ClientState state;
    private AuthData authData;

    private Collection<GameData> games;
    private String currentGameID;
    private GameData currentGame;

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

    public Collection<GameData> getGames() {
        return games;
    }

    public boolean isValidGame(String gameID){
        if(games == null) return false;
        for(GameData g : games){
            if(g.gameID().equals(gameID)){
                return true;
            }
        }
        return false;
    }

    public GameData getGameFromCache(String gameID){
        if(games == null) return null;
        for(GameData g : games){
            if(g.gameID().equals(gameID)){
                return g;
            }
        }
        return null;
    }

    public void setGames(Collection<GameData> games) {
        this.games = games;
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
}
