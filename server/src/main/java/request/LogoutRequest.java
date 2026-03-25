package request;

import model.AuthData;
import request.interfaces.Authorizable;
import request.interfaces.NullCheckable;

public record LogoutRequest(AuthData authData) implements NullCheckable, Authorizable<LogoutRequest> {
    public boolean containsNullField() {
        return authData == null || !authData.isValid();
    }

    @Override
    public LogoutRequest withAuth(AuthData authData) {
        return new LogoutRequest(authData);
    }
}
