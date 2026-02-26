package requestresult;

public record CreateGameResult(int responseCode, String message, String gameID) {
    public CreateGameResult(int responseCode, String message) {
        this(responseCode, message, null);
    }
}
