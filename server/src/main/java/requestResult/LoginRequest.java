package requestResult;

public record LoginRequest(String username, String password) implements NullCheckable{
    public boolean containsNullField(){
        return username == null || password == null;
    }

}
