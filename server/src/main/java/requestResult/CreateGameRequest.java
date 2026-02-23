package requestResult;

public record CreateGameRequest(String gameName, String authToken) implements NullCheckable, Authorizable<CreateGameRequest>{
    public boolean containsNullField(){
        return gameName == null || authToken == null;
    }

    @Override
    public CreateGameRequest withAuth(String authToken) {
        return new CreateGameRequest(gameName(), authToken);
    }
}
