package client;

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
            case "help" -> handleHelp();
            case "quit" -> handleQuit();
            case "login" -> handleLogin(parts);
            case "register" -> handleRegister(parts);
            default -> handleHelp();
        };
    }

    private static String handleUnknown(){

    }

    private static String handleHelp(){
        return "";
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
