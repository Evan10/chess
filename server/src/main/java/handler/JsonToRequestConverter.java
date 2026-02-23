package handler;

import com.google.gson.Gson;
import requestResult.Authorizable;

public class JsonToRequestConverter<T>{

    private static final Gson serializer = new Gson();
    private final Class<T> type;
    public JsonToRequestConverter(Class<T> type) {
        this.type=type;
    }
    public T convert(String json){
        return serializer.fromJson(json, type);
    }

    public  T convertWithToken(String json, String authToken){
        String src = json;
        if(json==null || json.isBlank()) {
            src = "{}";
        }
        T t = convert(src);

        if(t instanceof Authorizable<?> a){// Authorizable<?> "a" will only every be type <T> the compiler cant see that
            @SuppressWarnings("unchecked")
            T auth = (T) a.withAuth(authToken);
            return auth;
        }
        return t;
    }
}
