package requestResult;

public record RegisterResult(int responseCode, String message, String username, String authToken) {
    public RegisterResult(int responseCode, String message){
        this(responseCode,message,null,null);
    }

}
