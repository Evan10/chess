package request;

import model.AuthData;
import request.interfaces.Authorizable;
import request.interfaces.NullCheckable;

public record ListGamesRequest(AuthData authData) implements NullCheckable, Authorizable<ListGamesRequest> {
    public boolean containsNullField() {
        return authData == null || !authData.isValid();
    }

    @Override
    public ListGamesRequest withAuth(AuthData authData) {
        return new ListGamesRequest(authData);
    }
}
