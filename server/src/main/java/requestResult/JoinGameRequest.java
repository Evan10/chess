package requestResult;

import model.AuthData;

public record JoinGameRequest(String playerColor, String gameID, AuthData authData) implements NullCheckable, Authorizable<JoinGameRequest>{
    public boolean containsNullField(){
        return playerColor == null || gameID == null || authData == null
        || authData.authToken() == null || authData.username() == null;
    }

    @Override
    public JoinGameRequest withAuth(AuthData authData) {
        return new JoinGameRequest(playerColor(), gameID(), authData);
    }
}
