package handler;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import kotlin.Pair;
import model.AuthData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import util.Constants;

import java.util.Optional;

public class AuthHandler implements Handler {

    private final AuthService authService;

    public AuthHandler(AuthService authService){
        this.authService=authService;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        String authToken = context.header(Constants.AUTH_TOKEN);
        AuthData authData= authService.getAuth(authToken);
        if(authData == null || authData.username().isBlank()){
            context.attribute(Constants.AUTHENTICATED,null);
        } else {
            context.attribute(Constants.AUTHENTICATED, authData);
        }
    }

    public static String doAuth(Context context){
        if(!isAuth(context)
                || getAuthData(context) == null
                || getAuthData(context).authToken() == null){
            AuthHandler.blockRequest(context);
            return null;
        }
        return getAuthData(context).authToken();
    }

    private static AuthData getAuthData(Context context){
        return context.attribute(Constants.AUTHENTICATED);
    }

    public static boolean isAuth(@NotNull Context context){
        return context.attribute(Constants.AUTHENTICATED) != null;
    }

    private static void blockRequest(@NotNull Context context){
        context.status(Constants.UNAUTHORIZED);
        context.result("{\"message\":\"Error: unauthorized\"}");
    }
}
