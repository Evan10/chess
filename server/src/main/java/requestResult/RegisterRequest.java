package requestResult;

public record RegisterRequest(String username, String password, String email) implements NullCheckable{
    public boolean containsNullField(){
        return username == null || password == null || email == null;
    }
}
