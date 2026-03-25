package request;

import request.interfaces.NullCheckable;

public record ClearApplicationRequest() implements NullCheckable {
    public boolean containsNullField() {
        return false;
    }
}
