package client;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.AuthData;
import model.GameData;
import model.endpointresults.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ServerFacade {

    private static final HttpClient client = HttpClient.newBuilder().build();
    private static final Gson jsonConverter = new Gson();

    private final String url;

    private final ClientSessionData sessionData;

    public ServerFacade(String host, int port, ClientSessionData sessionData){
        url = String.format(Locale.getDefault(),"http://%s:%d",host,port);
        this.sessionData=sessionData;
    }

    void clearDatabase() throws FailResponseCodeException{
        HttpRequest req = requestHelper("db").DELETE().build();
        try{
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new FailResponseCodeException("Server is not running");
        }
    }

    void logout() throws FailResponseCodeException{
        HttpRequest req;
        try {
            req = withAuth(requestHelper("session")).DELETE().build();
        }catch (NullPointerException e){
            return;
        }
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if(res.statusCode() == 401){
                throw new FailResponseCodeException("User wasn't logged in");
            }
        } catch (IOException | InterruptedException e) {
            throw new FailResponseCodeException("Server is not running");
        }
    }

    AuthData login(String username, String password) throws FailResponseCodeException{
        String jsonBody = jsonConverter.toJson(Map.of("username", username, "password", password));
        HttpRequest req = requestHelper("session")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            LoginResult result = jsonConverter.fromJson(res.body(),LoginResult.class);
            if(res.statusCode()!= 200){
                throw new FailResponseCodeException(result.message());
            }

            return new AuthData(result.authToken(),result.username());
        } catch (IOException | InterruptedException e) {
            throw new FailResponseCodeException("Server is not running");
        }
    }

    AuthData register(String username, String password, String email) throws FailResponseCodeException{
        String jsonBody = jsonConverter.toJson(Map.of
                        ("username", username,
                        "password", password,
                        "email", email));
        HttpRequest req = requestHelper("user")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try{
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            RegisterResult result = jsonConverter.fromJson(res.body(), RegisterResult.class);
            if(res.statusCode()!= 200){
                throw new FailResponseCodeException(result.message());
            }

            return new AuthData(result.authToken(),result.username());
        }catch (IOException | InterruptedException e){
            throw new FailResponseCodeException("Server is not running");
        }

    }

    Collection<GameData> getGameList() throws FailResponseCodeException{
        HttpRequest req = withAuth(requestHelper("game")).GET().build();
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            ListGamesResult result = jsonConverter.fromJson(res.body(), ListGamesResult.class);
            if(res.statusCode()!= 200){
                throw new FailResponseCodeException(result.message());
            }
            return result.games();
        } catch (IOException | InterruptedException e){
            throw new FailResponseCodeException("Server is not running");
        }
    }

    String createGame(String name) throws FailResponseCodeException{
        String jsonBody = jsonConverter.toJson(Map.of
                ("gameName", name));
        HttpRequest req = withAuth(requestHelper("game"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            CreateGameResult result = jsonConverter.fromJson(res.body(), CreateGameResult.class);
            if(res.statusCode()!= 200){
                throw new FailResponseCodeException(result.message());
            }
            return result.gameID();
        } catch (IOException | InterruptedException e){
            throw new FailResponseCodeException("Server is not running");
        }
    }

    void joinGame(String gameID, ChessGame.TeamColor color) throws FailResponseCodeException{
        String jsonBody = jsonConverter.toJson(Map.of
                ("gameID", gameID, "playerColor", color.name()));
        HttpRequest req =withAuth(requestHelper("game"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            JoinGameResult result = jsonConverter.fromJson(res.body(), JoinGameResult.class);
            if(res.statusCode()!= 200){
                throw new FailResponseCodeException(result.message());
            }
        } catch (IOException | InterruptedException e){
            throw new FailResponseCodeException("Server is not running");
        }
    }

    GameData observeGame(String gameID) throws FailResponseCodeException{
        String jsonBody = jsonConverter.toJson(Map.of
                ("gameID", gameID));
        if(sessionData.getAuthData()== null){
            throw new FailResponseCodeException("No auth");
        }
        //works with websockets
        return null;
    }

    public void close(){
        client.close();
    }


    private HttpRequest.Builder requestHelper(String endpoint){
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(url+"/"+endpoint))
                    .header("content-type", "application/json")
                    .timeout(java.time.Duration.ofMillis(5000));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    private HttpRequest.Builder withAuth(HttpRequest.Builder builder) throws NullPointerException{
        AuthData authData =sessionData.getAuthData();
        if(authData == null) throw new NullPointerException();
        return builder.header("Authorization",authData.authToken());
    }


}
