package handler;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import requestResult.NullCheckable;
import util.Constants;

public class RequestFormHelper {

    protected static boolean isMissingFields(NullCheckable req){
        return req == null || req.containsNullField();
    }


    public static void blockRequest(@NotNull Context context){
        context.status(Constants.BAD_REQUEST);
        context.result("{\"message\":\"Error: bad request\"}");
    }
}
