package handler;

import com.google.gson.Gson;

public class JsonToRequestConverter<T>{

    private static final Gson serializer = new Gson();
    private final Class<T> type;
    public JsonToRequestConverter(Class<T> type) {
        this.type=type;
    }
    public T convert(String str){
        return serializer.fromJson(str, type);
    }

}
