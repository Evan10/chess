package handler;

import io.javalin.http.Context;
import model.AuthData;
import requestresult.*;
import service.GameService;

public class ChessGameHandler {

    private final GameService gameService;

    private final JsonToRequestConverter<JoinGameRequest> joinDeserializer;
    private final JsonToRequestConverter<ListGamesRequest> listDeserializer;
    private final JsonToRequestConverter<CreateGameRequest> createDeserializer;

    private final ResultToJsonStringConverter serializer;

    public ChessGameHandler(GameService gameService) {
        this.gameService = gameService;

        joinDeserializer = new JsonToRequestConverter<>(JoinGameRequest.class);
        listDeserializer = new JsonToRequestConverter<>(ListGamesRequest.class);
        createDeserializer = new JsonToRequestConverter<>(CreateGameRequest.class);

        serializer = ResultToJsonStringConverter.getInstance();
    }

    public void joinGameHandler(Context context) {
        AuthData authData = AuthHandler.doAuth(context);
        if (authData == null || !authData.isValid()) {
            return;
        }

        JoinGameRequest req = joinDeserializer
                .convertWithToken(context.body(), authData);

        if (RequestFormHelper.isMissingFields(req)) {
            RequestFormHelper.blockRequest(context);
            return;
        }
        JoinGameResult res = gameService.joinGame(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

    public void listGamesHandler(Context context) {
        AuthData authData = AuthHandler.doAuth(context);
        if (authData == null || !authData.isValid()) {
            return;
        }

        ListGamesRequest req = listDeserializer
                .convertWithToken(context.body(), authData);
        if (RequestFormHelper.isMissingFields(req)) {
            RequestFormHelper.blockRequest(context);
            return;
        }
        ListGamesResult res = gameService.listGames(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }

    public void createGameHandler(Context context) {
        AuthData authData = AuthHandler.doAuth(context);
        if (authData == null || !authData.isValid()) {
            return;
        }

        CreateGameRequest req = createDeserializer
                .convertWithToken(context.body(), authData);
        if (RequestFormHelper.isMissingFields(req)) {
            RequestFormHelper.blockRequest(context);
            return;
        }

        CreateGameResult res = gameService.createGame(req);
        context.status(res.responseCode());
        context.result(serializer.resToString(res));
    }


}
