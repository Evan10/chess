package handler;

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

    public String joinGameHandler(String json){
        JoinGameRequest req = joinDeserializer.convert(json);
        JoinGameResult res = gameService.joinGame(req);
        return serializer.resToString(res);
    }

    public String listGamesHandler(String json){
        ListGamesRequest req = listDeserializer.convert(json);
        ListGamesResult res = gameService.listGames(req);
        return serializer.resToString(res);
    }

    public String createGameHandler(String json){
        CreateGameRequest req = createDeserializer.convert(json);
        CreateGameResult res = gameService.createGame(req);
        return serializer.resToString(res);
    }


}
