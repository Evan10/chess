package requestResult;

import model.AuthData;

public record JoinGameRequest(String playerColor, String gameID, AuthData authData) implements NullCheckable, Authorizable<JoinGameRequest>{
    public boolean containsNullField(){
        return playerColor == null || playerColor.isBlank()
                || gameID == null || gameID.isBlank()
                || authData == null || !authData.isValid();
    }

    @Override
    public JoinGameRequest withAuth(AuthData authData) {
        return new JoinGameRequest(playerColor(), gameID(), authData);
    }
}
