package client;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.AuthData;
import model.GameData;
import model.endpointresults.ListGamesResult;
import model.endpointresults.LoginResult;
import model.endpointresults.RegisterResult;

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


    private static final CookieManager cookieManager = new CookieManager();
    static {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }
    private static final HttpClient client = HttpClient.newBuilder().cookieHandler(cookieManager).build();
    private static final Gson jsonConverter = new Gson();

    private final String url;

    private final ClientSessionData sessionData;

    public ServerFacade(String host, int port, ClientSessionData sessionData){
        url = String.format(Locale.getDefault(),"http://%s:%d",host,port);
        this.sessionData=sessionData;
    }

    void logout() throws FailResponseCodeException{
        HttpRequest req = withAuth(requestHelper("session")).DELETE().build();
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if(res.statusCode() == 401){
                throw new FailResponseCodeException("User wasn't logged in");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    AuthData login(String username, String password) throws FailResponseCodeException{
        String jsonBody = jsonConverter.toJson(Map.of("username", username, "password", password));
        HttpRequest req = requestHelper("session")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if(res.statusCode()!= 200){
                throw interpretErrorCode(res.statusCode());
            }
            LoginResult result = jsonConverter.fromJson(res.body(),LoginResult.class);
            return new AuthData(result.authToken(),result.username());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Server is not running");
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
            if(res.statusCode()!= 200){
                throw interpretErrorCode(res.statusCode());
            }
            RegisterResult result = jsonConverter.fromJson(res.body(), RegisterResult.class);
            return new AuthData(result.authToken(),result.username());
        }catch (IOException | InterruptedException e){
            throw new RuntimeException("Server is not running");
        }

    }

    Collection<GameData> getGameList() throws FailResponseCodeException{
        HttpRequest req = withAuth(requestHelper("game")).GET().build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if(res.statusCode()!= 200){
                throw interpretErrorCode(res.statusCode());
            }
            ListGamesResult result = jsonConverter.fromJson(res.body(), ListGamesResult.class);
            return result.games();
        } catch (IOException | InterruptedException e){
            throw new RuntimeException("Server is not running");
        }
    }

    GameData createGame(String name) throws FailResponseCodeException{
        withAuth(requestHelper("game"));
        return null;
    }

    GameData joinGame(String gameID, ChessGame.TeamColor color) throws FailResponseCodeException{
        withAuth(requestHelper("game"));
        return null;
    }

    GameData observeGame(String gameID) throws FailResponseCodeException{
        withAuth(requestHelper("game"));
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
    private HttpRequest.Builder withAuth(HttpRequest.Builder builder){
        AuthData authData =sessionData.getAuthData();
        if(authData == null) return builder;
        return builder.header("Authorization",authData.authToken());
    }


    private FailResponseCodeException interpretErrorCode(int code){
        return switch (code){
            case 400 -> new FailResponseCodeException("Bad request");
            case 401 -> new FailResponseCodeException("Not signed in");
            case 403 -> new FailResponseCodeException("Already in use");
            case 500 -> new FailResponseCodeException("Server error");
            default -> new FailResponseCodeException("Unknown Error");
        };
    }
}
