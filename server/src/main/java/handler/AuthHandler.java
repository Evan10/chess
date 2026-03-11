package handler;

import dataaccess.DataAccessException;
import dataaccess.DatabaseConnectivityException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.AuthData;
import org.jetbrains.annotations.NotNull;
import service.AuthService;
import util.Constants;

public class AuthHandler implements Handler {

    private final AuthService authService;

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(@NotNull Context context) throws DataAccessException{
        String authToken = context.header(Constants.AUTH_TOKEN);
        try{
            AuthData authData = authService.getAuth(authToken);
            if(authData.isValid()) {
                context.attribute(Constants.AUTHENTICATED, authData);
            } else{
                context.attribute(Constants.AUTHENTICATED, null);
            }
        }catch (DatabaseConnectivityException e){
            throw e;
        }
        catch (DataAccessException e){
            context.attribute(Constants.AUTHENTICATED, null);
        }
    }

    public static AuthData doAuth(Context context) {
        if (!isAuth(context)
                || getAuthData(context) == null
                || getAuthData(context).authToken() == null) {
            AuthHandler.blockRequest(context);
            return null;
        }
        return getAuthData(context);
    }

    private static AuthData getAuthData(Context context) {
        return context.attribute(Constants.AUTHENTICATED);
    }

    public static boolean isAuth(@NotNull Context context) {
        return context.attribute(Constants.AUTHENTICATED) != null;
    }

    private static void blockRequest(@NotNull Context context) {
        context.status(Constants.UNAUTHORIZED);
        context.result("{\"message\":\"Error: unauthorized\"}");
    }
}
