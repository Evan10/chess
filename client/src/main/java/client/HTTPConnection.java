package client;

import chess.ChessGame;
import model.AuthData;

import java.net.http.HttpClient;
import java.util.Locale;

public class HTTPConnection{


    private static final HttpClient client = HttpClient.newHttpClient();

    private String host;
    private int port;
    private String url;

    private AuthData ClientData;

    public HTTPConnection(String host, int port){
        this.host = host;
        this.port = port;
        url = String.format(Locale.getDefault(),"http://%s:%d",host,port);

    }

    void logout(){

    }

    void login(String username, String password){

    }

    void register(String username, String password, String email){

    }

    void getGameList(){

    }

    void createGame(String name){

    }

    void joinGame(String gameID, ChessGame.TeamColor color){

    }

    void observeGame(String gameID){

    }





    public void close(){
        client.close();
    }

}
