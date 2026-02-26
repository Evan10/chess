package requestresult;

import model.GameData;

import java.util.Collection;

public record ListGamesResult(int responseCode, String message, Collection<GameData> games) {
    public ListGamesResult(int responseCode, String message) {
        this(responseCode, message, null);
    }
}
