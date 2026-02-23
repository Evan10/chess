package requestResult;

import model.AuthData;

public record LogoutRequest(AuthData authData) implements NullCheckable, Authorizable<LogoutRequest>{
    public boolean containsNullField(){
        return authData == null || authData.authToken() == null || authData.username() == null;
    }

    @Override
    public LogoutRequest withAuth(AuthData authData) {
        return new LogoutRequest(authData);
    }
}
