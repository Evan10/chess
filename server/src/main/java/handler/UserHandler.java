package handler;

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

    public String registerHandler(String json){
        RegisterRequest req = registerDeserializer.convert(json);
        RegisterResult res = userService.register(req);
        return serializer.resToString(res);
    }

    public String loginHandler(String json){
        LoginRequest req = loginDeserializer.convert(json);
        LoginResult res = userService.login(req);
        return serializer.resToString(res);
    }

    public String logoutHandler(String json){
        LogoutRequest req = logoutDeserializer.convert(json);
        LogoutResult res = userService.logout(req);
        return serializer.resToString(res);
    }

}
