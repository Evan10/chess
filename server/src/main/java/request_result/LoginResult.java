package request_result;

public record LoginResult(int responseCode, String message, String username, String authToken) {
}
