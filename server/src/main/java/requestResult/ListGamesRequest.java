package requestResult;

public record ListGamesRequest(String authToken) implements NullCheckable, Authorizable<ListGamesRequest>{
    public boolean containsNullField(){
        return authToken == null;
    }

    @Override
    public ListGamesRequest withAuth(String authToken) {
        return new ListGamesRequest(authToken);
    }
}
