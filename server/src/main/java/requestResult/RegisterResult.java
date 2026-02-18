package requestResult;

public record RegisterResult(int responseCode, String message, String username, String authToken) {
}
