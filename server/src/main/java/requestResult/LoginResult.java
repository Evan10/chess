package requestResult;

public record LoginResult(int responseCode, String message, String username, String authToken) {
    public LoginResult(int responseCode, String message){
        this(responseCode,message,null,null);
    }
}
