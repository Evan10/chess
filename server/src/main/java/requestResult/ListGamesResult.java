package requestResult;

public record ListGamesResult(int responseCode, String message, String games) {
    public ListGamesResult(int responseCode, String message){
        this(responseCode, message,null);
    }
}
