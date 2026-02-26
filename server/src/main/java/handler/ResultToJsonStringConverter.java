package handler;

import com.google.gson.Gson;

public class ResultToJsonStringConverter {

    private static final Gson SERIALIZER = new Gson();
    private static ResultToJsonStringConverter instance = null;

    private ResultToJsonStringConverter() {
    }

    public String resToString(Object obj) {
        return SERIALIZER.toJson(obj);
    }

    public static ResultToJsonStringConverter getInstance() {
        if (instance == null) {
            instance = new ResultToJsonStringConverter();
        }
        return instance;
    }

}
