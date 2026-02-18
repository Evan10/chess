package handler;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import kotlin.Pair;
import model.AuthData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.AuthService;

import java.util.Optional;

public class AuthHandler implements Handler {

    private final AuthService authService;

    public AuthHandler(AuthService authService){
        this.authService=authService;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        String authToken = context.header("authorization");
        AuthData authData= authService.isAuth(authToken);
        if(authData.username()==null || authData.username().isBlank()){
            context.attribute("authenticated",null);
        } else {
            context.attribute("authenticated", authData);
        }

    }
}
