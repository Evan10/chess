package requestResult;

import model.AuthData;

public record ListGamesRequest(AuthData authData) implements NullCheckable, Authorizable<ListGamesRequest>{
    public boolean containsNullField(){
        return authData == null || authData.username() == null || authData.authToken() == null;
    }

    @Override
    public ListGamesRequest withAuth(AuthData authData) {
        return new ListGamesRequest(authData);
    }
}
