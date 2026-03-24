package client;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class ServerFacade {


    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson jsonConverter = new Gson();

    private final String url;

    public ServerFacade(String host, int port){
        url = String.format(Locale.getDefault(),"http://%s:%d",host,port);
    }

    void logout() throws FailResponseCodeException{
        HttpRequest req = requestHelper("session").DELETE().build();
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
            if(res.statusCode()==404){
                throw new FailResponseCodeException("Username or password was incorrect");
            }
            return jsonConverter.fromJson(res.body(),AuthData.class);
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
            if(res.statusCode() == 400){
                throw new FailResponseCodeException("Bad request");
            }else if(res.statusCode() !=200){
                throw new FailResponseCodeException("Username is not available");
            }
            return jsonConverter.fromJson(res.body(),AuthData.class);
        }catch (IOException | InterruptedException e){
            throw new RuntimeException("Server is not running");
        }

    }

    Collection<GameData> getGameList() throws FailResponseCodeException{
        HttpRequest req = requestHelper("game").GET().build();

        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if(res.statusCode()!= 200){
                throw FailResponseCodeException("")
            }
            Type type =  new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> body = jsonConverter.fromJson(res.body(), type);


        } catch (IOException | InterruptedException e){
            throw new RuntimeException("Server is not running");
        }
        return null;
    }

    GameData createGame(String name) throws FailResponseCodeException{
        requestHelper("game");
        return null;
    }

    GameData joinGame(String gameID, ChessGame.TeamColor color) throws FailResponseCodeException{
        requestHelper("game");
        return null;
    }

    GameData observeGame(String gameID) throws FailResponseCodeException{
        requestHelper("game");
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
}
