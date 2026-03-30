package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import ui.UIChessBoardHelper;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final ConsoleWriter consoleWriter;
    RequestHandler(ServerFacade connection, ClientSessionData sessionData, ConsoleWriter consoleWriter){
        this.connection=connection;
        this.sessionData=sessionData;
        this.consoleWriter=consoleWriter;
    }

    public boolean handle(String message){
        String[] parts = message.split(" ");
        String command = parts[0].toLowerCase();
        boolean quit = false;
        switch (command){
            case HELP -> handleHelp();
            case QUIT -> {
                handleQuit();
                quit = true;
            }
            case LOGIN -> handleLogin(parts);
            case REGISTER -> handleRegister(parts);
            case LOGOUT -> handleLogout();
            case CREATE_GAME -> handleCreateGame(parts);
            case LIST_GAMES -> handleListGames();
            case JOIN_GAME -> handleJoinGame(parts);
            case OBSERVE_GAME -> handleObserveGame(parts);
            default -> handleUnknown();
        };
        return quit;
    }



    private void handleUnknown(){
        consoleWriter.writeErrorMessage("Unknown command; Please use a valid command \n");
        handleHelp();
    }


    private void handleHelp(){
        if(sessionData.getState() == LOGGED_OUT){
            consoleWriter.writeMessage("""
                    register <Username> <Password> <Email> - create an account
                    login <Username> <Password> - to play chess
                    quit - chess client
                    help - commands
                    """);
        }else{
            consoleWriter.writeMessage("""
                    create <Name> - create a chess game
                    list - all chess games
                    join <GamePosition> [BLACK|WHITE]
                    observe <GamePosition> - a chess game
                    logout - of chess client
                    quit - chess client
                    help - commands
                    """);
        }

    }

    private void handleQuit(){
        if(sessionData.getState() == LOGGED_IN) {
            try {
                connection.logout();
            } catch (FailResponseCodeException e) {
                consoleWriter.writeErrorMessage(e.getMessage());
            }
        }
    }

    private void handleLogout(){
        if(sessionData.getState() == LOGGED_OUT){
            consoleWriter.writeErrorMessage(NOT_LOGGED_IN_MESSAGE);
            return;
        }
        try {
            connection.logout();
            sessionData.setAuthData(null);
            consoleWriter.setPrefix(LOGGED_OUT.name);
            sessionData.setState(LOGGED_OUT);
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
            return;
        }

        consoleWriter.writeMessage("User logged out");
    }

    private void handleLogin(String[] args){
        if(args.length != 3){
            consoleWriter.writeErrorMessage("Invalid login command");
            consoleWriter.writeMessage("""
                    Must be of format:\s
                        login <username> <password>""");
            return;
        }
        String username = args[1];
        String password = args[2];
        try {
            AuthData authData = connection.login(username, password);
            sessionData.setAuthData(authData);
            sessionData.setState(LOGGED_IN);
            consoleWriter.setPrefix(authData.username());
            consoleWriter.writeMessage(String.format("Welcome %s!",authData.username()));
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }
    }

    private void handleRegister(String[] args){
        if(args.length != 4){
            consoleWriter.writeErrorMessage("Invalid register command");
            consoleWriter.writeMessage("""
                    Must be of format:\s
                        register <username> <password> <email>""");
            return;
        }
        String username = args[1];
        String password = args[2];
        String email = args[3];
        try {
            AuthData authData= connection.register(username,password,email);
            sessionData.setAuthData(authData);
            sessionData.setState(LOGGED_IN);
            consoleWriter.setPrefix(authData.username());
            consoleWriter.writeMessage(authData.username() +" registered");
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }
    }


    private void handleCreateGame(String[] args){
        if(sessionData.getState() == LOGGED_OUT){
            consoleWriter.writeErrorMessage(NOT_LOGGED_IN_MESSAGE);
            return;
        }
        if(args.length != 2){
            consoleWriter.writeErrorMessage("Invalid join command");
            consoleWriter.writeMessage("""
                    Must be of format:\s
                        create <Name>""");
            return;
        }
        String name = args[1];
        try {
            connection.createGame(name);
            consoleWriter.writeMessage("Game created with name: " + name);
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }
    }

    private void handleJoinGame(String[] args){
        if(sessionData.getState() == LOGGED_OUT){
            consoleWriter.writeErrorMessage(NOT_LOGGED_IN_MESSAGE);
            return;
        }
        if(args.length != 3){
            consoleWriter.writeErrorMessage("Invalid join command");
            consoleWriter.writeMessage("""
                    Must be of format:\s
                        join <GamePosition> [BLACK|WHITE]""");
            return;
        }
        int gamePos = Integer.parseInt(args[1]);
        ChessGame.TeamColor team;
        try{
            team = stringToTeamColor(args[2]);
        }catch (IllegalArgumentException e){
            consoleWriter.writeErrorMessage("Invalid color provided must be \"Black\" or \"White\"");
            return;
        }
        if(!sessionData.isValidGame(gamePos)){
            consoleWriter.writeMessage(GAME_NOT_FOUND_MESSAGE);
            return;
        }
        try {
            String gameID = sessionData.getGameIDFromPosition(gamePos);
            connection.joinGame(gameID,team);
            GameData gameData = sessionData.getGameFromCache(gamePos);
            sessionData.setCurrentGame(gameData);
            sessionData.setColor(team);
            consoleWriter.writeMessage(String.format("Joined game %s:",gameData.gameName()));
            consoleWriter.writeBoard(gameData.game());
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }
    }

    private void handleObserveGame(String[] args){
        if(sessionData.getState() == LOGGED_OUT){
             consoleWriter.writeErrorMessage(NOT_LOGGED_IN_MESSAGE);
             return;
        }
        if(args.length != 2){
            consoleWriter.writeMessage("""
                    Invalid observe command\s
                    Must be of format:\s
                        observe <GamePosition>""");
            return;
        }
        int gamePos = Integer.parseInt(args[1]);
        if(!sessionData.isValidGame(gamePos)){
            consoleWriter.writeErrorMessage(GAME_NOT_FOUND_MESSAGE);
            return;
        }
        try {
            String gameID = sessionData.getGameIDFromPosition(gamePos);
            connection.observeGame(gameID);
            sessionData.setCurrentGameID(gameID);
            GameData gameData = sessionData.getGameFromCache(gamePos);
            if(gameData == null){
                consoleWriter.writeErrorMessage("Game not observed");
                return;
            }
            consoleWriter.writeMessage("Observing game: " + gameData.gameName());
            consoleWriter.writeBoard(gameData.game());
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }
    }

    private void handleListGames(){
        if(sessionData.getState() == LOGGED_OUT){
            consoleWriter.writeErrorMessage(NOT_LOGGED_IN_MESSAGE);
            return;
        }
        try {
            Collection<GameData> games = connection.getGameList();
            sessionData.addGames(games);
            consoleWriter.writeGameList(sessionData.getGames());
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }

    }


}
