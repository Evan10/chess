package requestResult;

public record LoginResult(int responseCode, String message, String username, String authToken) {
}
