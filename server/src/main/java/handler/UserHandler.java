package handler;

import io.javalin.http.Context;
import model.AuthData;
import requestResult.*;
import service.UserService;

public class UserHandler {

    private final UserService userService;

    private final JsonToRequestConverter<RegisterRequest> registerDeserializer;
    private final JsonToRequestConverter<LoginRequest> loginDeserializer;
    private final JsonToRequestConverter<LogoutRequest> logoutDeserializer;

    private final ResultToJsonStringConverter serializer;

    public UserHandler(UserService userService){
        this.userService=userService;

        registerDeserializer = new JsonToRequestConverter<>(RegisterRequest.class);
        loginDeserializer = new JsonToRequestConverter<>(LoginRequest.class);
        logoutDeserializer = new JsonToRequestConverter<>(LogoutRequest.class);

        serializer = ResultToJsonStringConverter.getInstance();
    }

    public void registerHandler(Context context){
        RegisterRequest req = registerDeserializer.convert(context.body());
        if(RequestFormHelper.isMissingFields(req)){
            RequestFormHelper.blockRequest(context);
            return;
        }
        RegisterResult res = userService.register(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

    public void loginHandler(Context context){
        LoginRequest req = loginDeserializer.convert(context.body());
        if(RequestFormHelper.isMissingFields(req)){
            RequestFormHelper.blockRequest(context);
            return;
        }
        LoginResult res = userService.login(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

    public void logoutHandler(Context context){
        AuthData authData = AuthHandler.doAuth(context);
        if(authData==null || !authData.isValid()){
            return;
        }
        LogoutRequest req = logoutDeserializer
                .convertWithToken(context.body(),authData);
        System.out.println(req);
        if(RequestFormHelper.isMissingFields(req)){
            RequestFormHelper.blockRequest(context);
            return;
        }
        LogoutResult res = userService.logout(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

}
