package client;

import static client.ClientCommands.*;
import static client.ClientState.LOGGED_OUT;

public class RequestHandler {

    private final HTTPConnection connection;
    private final ChessClient client;
    RequestHandler(HTTPConnection connection, ChessClient client){
        this.connection=connection;
        this.client=client;
    }

    public String handle(String message){
        String[] parts = message.split(" ");
        String command = parts[0].toLowerCase();
        return switch (command){
            case HELP -> handleHelp();
            case QUIT -> handleQuit();
            case LOGIN -> handleLogin(parts);
            case REGISTER -> handleRegister(parts);
            case LOGOUT -> "";
            case CREATE_GAME -> "";
            case LIST_GAMES -> "";
            case JOIN_GAME -> "";
            case OBSERVE_GAME -> "";
            default -> handleUnknown();
        };
    }

    private String handleUnknown(){
        return "Unknown command; Please use a valid command \n" + handleHelp();
    }

    private String handleHelp(){
        if(client.state == LOGGED_OUT){
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - chess client
                    help - commands
                    """;
        }else{
            return """
                    create <Name> - create a chess game
                    list - all chess games
                    join <GAME_ID> [BLACK|WHITE]
                    observe <GAME_ID> - a chess game
                    logout - of chess client
                    quit - chess client
                    help - commands
                    """;
        }

    }

    private String handleQuit(){
        return "quit";
    }

    private String handleLogin(String[] args){
        return "";
    }

    private String handleRegister(String[] args){
        return "";
    }

}
