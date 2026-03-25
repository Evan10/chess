package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import ui.UIChessBoardHelper;

import java.util.Collection;

import static client.ClientCommands.*;
import static client.ClientState.LOGGED_IN;
import static client.ClientState.LOGGED_OUT;
import static client.ClientUtils.stringToTeamColor;

public class RequestHandler {


    private static final String NOT_LOGGED_IN_MESSAGE = "Unable to use command, \n" +
            "user must be logged in";
    private static final String GAME_NOT_FOUND_MESSAGE = """
                     Game was not found in local cache; try using the command:
                        list
                     To list the game
                     """;
    private final ServerFacade connection;
    private final ClientSessionData sessionData;
    RequestHandler(ServerFacade connection, ClientSessionData sessionData){
        this.connection=connection;
        this.sessionData=sessionData;
    }

    public String handle(String message){
        String[] parts = message.split(" ");
        String command = parts[0].toLowerCase();
        return switch (command){
            case HELP -> handleHelp();
            case QUIT -> handleQuit();
            case LOGIN -> handleLogin(parts);
            case REGISTER -> handleRegister(parts);
            case LOGOUT -> handleLogout();
            case CREATE_GAME -> handleCreateGame(parts);
            case LIST_GAMES -> handleListGames();
            case JOIN_GAME -> handleJoinGame(parts);
            case OBSERVE_GAME -> handleObserveGame(parts);
            default -> handleUnknown();
        };
    }



    private String handleUnknown(){
        return "Unknown command; Please use a valid command \n" + handleHelp();
    }

    private String handleHelp(){
        if(sessionData.getState() == LOGGED_OUT){
            return """
                    register <Username> <Password> <Email> - create an account
                    login <Username> <Password> - to play chess
                    quit - chess client
                    help - commands
                    """;
        }else{
            return """
                    create <Name> - create a chess game
                    list - all chess games
                    join <GameID> [BLACK|WHITE]
                    observe <GameID> - a chess game
                    logout - of chess client
                    quit - chess client
                    help - commands
                    """;
        }

    }

    private String handleQuit(){
        if(sessionData.getState() == LOGGED_IN) {
            try {
                connection.logout();
            } catch (FailResponseCodeException _) {
            }
        }
        return "quit";
    }

    private String handleLogout(){
        if(sessionData.getState() == LOGGED_OUT){
            return NOT_LOGGED_IN_MESSAGE;
        }
        sessionData.setAuthData(null);
        sessionData.setState(LOGGED_OUT);
        try {
            connection.logout();
        } catch (FailResponseCodeException e) {
            return e.getMessage();
        }

        return "";
    }

    private String handleLogin(String[] args){
        if(args.length != 3){
            return """
                    Invalid login command\s
                    Must be of format:\s
                        login <username> <password>""";
        }
        String username = args[1];
        String password = args[2];
        try {
            AuthData authData = connection.login(username, password);
            sessionData.setAuthData(authData);
            sessionData.setState(LOGGED_IN);
            return authData.username() +" is logged in";
        } catch (FailResponseCodeException e) {
            return e.getMessage();
        }
    }

    private String handleRegister(String[] args){
        if(args.length != 4){
            return """
                    Invalid register command\s
                    Must be of format:\s
                        register <username> <password> <email>""";
        }
        String username = args[1];
        String password = args[2];
        String email = args[3];
        try {
            AuthData authData= connection.register(username,password,email);
            sessionData.setAuthData(authData);
            sessionData.setState(LOGGED_IN);
            return authData.username() +" registered";
        } catch (FailResponseCodeException e) {
            return e.getMessage();
        }
    }


    private String handleCreateGame(String[] args){
        if(sessionData.getState() == LOGGED_OUT){
            return NOT_LOGGED_IN_MESSAGE;
        }
        if(args.length != 2){
            return """
                    Invalid create command\s
                    Must be of format:\s
                        create <Name>""";
        }
        String name = args[1];
        try {
            GameData gameData = connection.createGame(name);
            if(gameData==null) {
                return "Game wasn't created";
            }
            return "Game created with ID: " + gameData.gameID();
        } catch (FailResponseCodeException e) {
            return e.getMessage();
        }
    }

    private String handleJoinGame(String[] args){
        if(sessionData.getState() == LOGGED_OUT){
            return NOT_LOGGED_IN_MESSAGE;
        }
        if(args.length != 3){
            return """
                    Invalid join command\s
                    Must be of format:\s
                        join <GameID> [BLACK|WHITE]""";
        }
        String gameID = args[1];
        ChessGame.TeamColor team;
        try{
            team = stringToTeamColor(args[2]);
        }catch (IllegalArgumentException e){
            return "Invalid color provided must be \"Black\" or \"White\"";
        }
        if(!sessionData.isValidGame(gameID)){
            return """
                     Game was not found in local cache; try using the command:
                        list
                     To list the game
                     """;
        }
        if(!sessionData.isValidGame(gameID)){
            return GAME_NOT_FOUND_MESSAGE;
        }
        try {
            connection.joinGame(gameID,team);
            GameData gameData = sessionData.getGameFromCache(gameID);
            sessionData.setCurrentGame(gameData);
            return "Joined game: " + gameData.gameName() + "\n"
                    + UIChessBoardHelper.uiChessBoard(gameData.game(), team);
        } catch (FailResponseCodeException e) {
            return e.getMessage();
        }
    }

    private String handleObserveGame(String[] args){
        if(sessionData.getState() == LOGGED_OUT){
            return NOT_LOGGED_IN_MESSAGE;
        }
        if(args.length != 2){
            return """
                    Invalid observe command\s
                    Must be of format:\s
                        observe <GameID>""";
        }
        String gameID = args[1];
        if(!sessionData.isValidGame(gameID)){
            return GAME_NOT_FOUND_MESSAGE;
        }
        try {
            GameData gameData = connection.observeGame(gameID);
            sessionData.setCurrentGameID(gameID);
            if(gameData == null){
                return "Game not observed";
            }
            return "Observing game: " + gameData.gameName() + "\n"
                    + UIChessBoardHelper.uiChessBoard(gameData.game(), ChessGame.TeamColor.WHITE);
        } catch (FailResponseCodeException e) {
            return e.getMessage();
        }
    }

    private String handleListGames(){
        if(sessionData.getState() == LOGGED_OUT){
            return NOT_LOGGED_IN_MESSAGE;
        }
        try {
            Collection<GameData> games = connection.getGameList();
            sessionData.setGames(games);
            return "Games";
        } catch (FailResponseCodeException e) {
            return e.getMessage();
        }

    }

    private String validGameCheck(String gameID){

        return null;
    }

}
