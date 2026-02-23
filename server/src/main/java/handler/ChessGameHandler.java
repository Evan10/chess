package handler;

import io.javalin.http.Context;
import model.AuthData;
import requestResult.*;

import service.GameService;

public class ChessGameHandler {

    private final GameService gameService;

    private final JsonToRequestConverter<JoinGameRequest> joinDeserializer;
    private final JsonToRequestConverter<ListGamesRequest> listDeserializer;
    private final JsonToRequestConverter<CreateGameRequest> createDeserializer;

    private final ResultToJsonStringConverter serializer;

    public ChessGameHandler(GameService gameService){
        this.gameService=gameService;

        joinDeserializer = new JsonToRequestConverter<>(JoinGameRequest.class);
        listDeserializer = new JsonToRequestConverter<>(ListGamesRequest.class);
        createDeserializer = new JsonToRequestConverter<>(CreateGameRequest.class);

        serializer = ResultToJsonStringConverter.getInstance();
    }

    public void joinGameHandler(Context context){
        String authToken = AuthHandler.doAuth(context);
        if(authToken==null){
            return;
        }

        JoinGameRequest req = joinDeserializer
                .convertWithToken(context.body(),authToken);

        if(RequestFormHelper.isMissingFields(req)){
            RequestFormHelper.blockRequest(context);
            return;
        }
        JoinGameResult res = gameService.joinGame(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

    public void listGamesHandler(Context context){
        String authToken = AuthHandler.doAuth(context);
        if(authToken==null){
            return;
        }

        ListGamesRequest req = listDeserializer
                .convertWithToken(context.body(), authToken);
        if(RequestFormHelper.isMissingFields(req)){
            RequestFormHelper.blockRequest(context);
            return;
        }
        ListGamesResult res = gameService.listGames(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

    public void createGameHandler(Context context){
        String authToken = AuthHandler.doAuth(context);
        if(authToken==null){
            return;
        }

        CreateGameRequest req = createDeserializer
                .convertWithToken(context.body(), authToken);
        if(RequestFormHelper.isMissingFields(req)){
            RequestFormHelper.blockRequest(context);
            return;
        }

        CreateGameResult res = gameService.createGame(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }


}
