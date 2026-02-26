package requestresult;

import requestresult.interfaces.NullCheckable;

public record ClearApplicationRequest() implements NullCheckable {
    public boolean containsNullField() {
        return false;
    }
}
