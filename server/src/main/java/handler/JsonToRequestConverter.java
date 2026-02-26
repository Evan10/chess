package handler;

import com.google.gson.Gson;
import model.AuthData;
import requestresult.interfaces.Authorizable;

public class JsonToRequestConverter<T> {

    private static final Gson SERIALIZER = new Gson();
    private final Class<T> type;

    public JsonToRequestConverter(Class<T> type) {
        this.type = type;
    }

    public T convert(String json) {
        return SERIALIZER.fromJson(json, type);
    }

    public T convertWithToken(String json, AuthData authData) {
        String src = json;
        if (json == null || json.isBlank()) {
            src = "{}";
        }
        T t = convert(src);

        if (t instanceof Authorizable<?> a) {// Authorizable<?> "a" will only every be type <T> the compiler cant see that
            @SuppressWarnings("unchecked")
            T auth = (T) a.withAuth(authData);
            return auth;
        }
        return t;
    }
}
