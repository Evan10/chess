package requestResult;

public record JoinGameRequest(String playerColor, String gameID, String authToken) implements NullCheckable, Authorizable<JoinGameRequest>{
    public boolean containsNullField(){
        return playerColor == null || gameID == null || authToken == null;
    }

    @Override
    public JoinGameRequest withAuth(String authToken) {
        return new JoinGameRequest(playerColor(), gameID(), authToken);
    }
}
