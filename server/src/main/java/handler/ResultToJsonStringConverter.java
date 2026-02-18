package handler;

import com.google.gson.Gson;

public class ResultToJsonStringConverter {

    private static final Gson serializer = new Gson();
    private static ResultToJsonStringConverter instance = null;

    private ResultToJsonStringConverter(){}

    public String resToString(Object obj) {
        return serializer.toJson(obj);
    }

    public static ResultToJsonStringConverter getInstance(){
        if(instance == null){
            instance = new ResultToJsonStringConverter();
        }
        return instance;
    }

}
