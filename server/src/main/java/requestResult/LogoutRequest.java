package requestResult;

public record LogoutRequest(String authToken) implements NullCheckable, Authorizable<LogoutRequest>{
    public boolean containsNullField(){
        return authToken == null;
    }

    @Override
    public LogoutRequest withAuth(String authToken) {
        return new LogoutRequest(authToken);
    }
}
