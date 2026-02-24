package requestResult;

import model.AuthData;

public record CreateGameRequest(String gameName, AuthData authData) implements NullCheckable, Authorizable<CreateGameRequest>{
    public boolean containsNullField(){
        return gameName == null || authData == null
                || !authData.isValid();
    }

    @Override
    public CreateGameRequest withAuth(AuthData authData) {
        return new CreateGameRequest(gameName(), authData);
    }
}
