package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import model.AuthData;
import model.GameData;
import ui.UIChessBoardHelper;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static client.ChessMoveParser.parseChessPosition;
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
    private final ServerFacade httpConnection;
    private final WsClient wsConnection;
    private final ClientSessionData sessionData;
    private final ConsoleWriter consoleWriter;

    RequestHandler(ServerFacade httpConnection,WsClient wsConnection, ClientSessionData sessionData, ConsoleWriter consoleWriter){
        this.httpConnection=httpConnection;
        this.wsConnection=wsConnection;
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
            case REDRAW_GAME -> handleRedrawGame();
            case LEAVE_GAME -> handleLeaveGame();
            case MAKE_MOVE -> handleMakeMoveInGame(parts);
            case RESIGN_FROM_GAME -> handleResignFromGame();
            case HIGHLIGHT_MOVES -> handleHighlightMoves(parts);
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
                    """);
        }else{
            if(canUseGameCommands()){
                consoleWriter.writeMessage("""
                        redraw - current game
                        leave - current game
                        highlight <position> - highlight valid moves
                        """);
            } else {
                consoleWriter.writeMessage("""
                        create <Name> - create a chess game
                        list - all chess games
                        join <GamePosition> [BLACK|WHITE]
                        observe <GamePosition> - a chess game
                        logout - of chess client
                        """);
            }
            if(isPlayerInGame()) {
                consoleWriter.writeMessage("""
                        resign - from current game
                        move <position-start> <position-end> <*promotion>
                        """);
            }
        }

        consoleWriter.writeMessage("""
                    quit - chess client
                    help - commands
                """);
    }

    private void handleQuit(){
        if(sessionData.getState() == LOGGED_IN) {
            try {
                httpConnection.logout();
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
            httpConnection.logout();
            sessionData.clearSessionData();
            consoleWriter.setPrefix(LOGGED_OUT.name);
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
            AuthData authData = httpConnection.login(username, password);
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
            AuthData authData= httpConnection.register(username,password,email);
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
            httpConnection.createGame(name);
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
            httpConnection.joinGame(gameID,team);
            wsConnection.connectToGame(Integer.parseInt(gameID));
            GameData gameData = sessionData.getGameFromCache(gamePos);
            sessionData.setCurrentGame(gameData);
            sessionData.setColor(team);
            consoleWriter.writeMessage(String.format("Joined game %s:",gameData.gameName()));
            consoleWriter.writeBoard(gameData.game());
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }catch (IOException e){
            consoleWriter.writeErrorMessage("Error: unable to connect websocket");
        }
    }

    private void handleObserveGame(String[] args) {
        if (sessionData.getState() == LOGGED_OUT) {
            consoleWriter.writeErrorMessage(NOT_LOGGED_IN_MESSAGE);
            return;
        }
        if (args.length != 2) {
            consoleWriter.writeMessage("""
                    Invalid observe command\s
                    Must be of format:\s
                        observe <GamePosition>""");
            return;
        }
        int gamePos = Integer.parseInt(args[1]);
        if (!sessionData.isValidGame(gamePos)) {
            consoleWriter.writeErrorMessage(GAME_NOT_FOUND_MESSAGE);
            return;
        }
        try {
            String gameID = sessionData.getGameIDFromPosition(gamePos);

            wsConnection.connectToGame(Integer.parseInt(gameID));

            sessionData.setCurrentGameID(gameID);
            GameData gameData = sessionData.getGameFromCache(gamePos);
            if (gameData == null) {
                consoleWriter.writeErrorMessage("Game not observed");
                return;
            }
            consoleWriter.writeMessage("Observing game: " + gameData.gameName());
            consoleWriter.writeBoard(gameData.game());
        } catch (IOException e) {
            consoleWriter.writeErrorMessage("Error: unable to connect websocket");
        }
    }

    private void handleListGames(){
        if(sessionData.getState() == LOGGED_OUT){
            consoleWriter.writeErrorMessage(NOT_LOGGED_IN_MESSAGE);
            return;
        }
        try {
            Collection<GameData> games = httpConnection.getGameList();
            sessionData.addGames(games);
            consoleWriter.writeGameList(sessionData.getGames());
        } catch (FailResponseCodeException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }

    }


    private void handleRedrawGame(){
        if(!canUseGameCommands()){
            consoleWriter.writeErrorMessage("Error: Not in game");
        }
        consoleWriter.writeBoard(sessionData.getCurrentGame().game());
    }

    private void handleLeaveGame(){
        if(!canUseGameCommands()){
            consoleWriter.writeErrorMessage("Error: Not in game");
            return;
        }
        try {
            wsConnection.leaveGame();
            consoleWriter.writeMessage("Left game");
        } catch (IOException e) {
            consoleWriter.writeErrorMessage("Error: unable to leave game");
        }
    }

    private void handleMakeMoveInGame(String[] args){
        if(args.length<3||args.length>4){
            consoleWriter.writeErrorMessage("Error: invalid format");
            consoleWriter.writeMessage("""
                    Must be of format
                        move <position-start> <position-end> <*promotion>
                    where each position is of format
                        [a-h][1-8]
                    examples
                        a1
                        c5
                        b8
                    where promotion *(if applicable) is
                        the name of the piece type or first letter
                        with the exception of the knight which is
                        abbreviated to "kn"
                    """);
            return;
        }
        if(!canUseGameCommands()){
            consoleWriter.writeErrorMessage("Error: Not in game");
            return;
        } else if(!isPlayerInGame()){
            consoleWriter.writeErrorMessage("Error: not a player");
            return;
        } else if(!canMakeMove()){
            consoleWriter.writeErrorMessage("Error: not your turn");
            return;
        }
        String start = args[1];
        String end = args[2];
        String promotion = "";
        if(args.length == 4){
            promotion = args[3];
        }
        try {
            ChessMove move = ChessMoveParser.parseChessMove(start,end,promotion);
            wsConnection.makeMoveInGame(move);
            // stops client from double sending moves before hearing back from the server
            sessionData.getCurrentGame().game().toggleTeamTurn();
            consoleWriter.writeMessage("Move made");
        } catch (InvalidMoveException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        } catch (IOException e) {
            consoleWriter.writeErrorMessage("Error: unable to connect websocket");
        }
    }

    private void handleResignFromGame(){
        if(!canUseGameCommands()){
            consoleWriter.writeErrorMessage("Error: Not in game");
            return;
        } else if(!isPlayerInGame()){
            consoleWriter.writeErrorMessage("Error: not a player");
            return;
        }
        try {
            wsConnection.resignFromGame();
            consoleWriter.writeMessage("Successfully resigned");
        } catch (IOException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }
    }

    private void handleHighlightMoves(String[] args){
        if(args.length!=2){
            consoleWriter.writeErrorMessage("Error: invalid format");
            consoleWriter.writeMessage("""
                    Must be of format
                        highlight <position>
                    where position is of format
                        [a-h][1-8]
                    examples
                        a1
                        c5
                        b8
                    """);
            return;
        }
        if(!canUseGameCommands()){
            consoleWriter.writeErrorMessage("Error: Not in game");
            return;
        }
        String start = args[1];
        try {
            ChessPosition startPos = parseChessPosition(start);
            Collection<ChessMove> legalMoves = sessionData.getCurrentGame().game().validMoves(startPos);
            consoleWriter.writeBoard(sessionData.getCurrentGame().game(), legalMoves);
        } catch (InvalidMoveException e) {
            consoleWriter.writeErrorMessage(e.getMessage());
        }
    }

    private boolean canUseGameCommands(){
        return sessionData.getState() == LOGGED_IN && sessionData.isInGame();
    }

    private boolean isPlayerInGame(){
        AuthData authData = sessionData.getAuthData();
        if(authData == null) {return false;}
        String username = authData.username();
        if(username == null) {return false;}
        boolean isWhite = sessionData.getColor().equals(ChessGame.TeamColor.WHITE);
        return  isWhite ?
                sessionData.getCurrentGame().whiteUsername().equals(username):
                sessionData.getCurrentGame().blackUsername().equals(username);
    }

    private boolean canMakeMove(){
        ChessGame game = sessionData.getCurrentGame().game();
        if(game == null){
            return false;
        }
        return  isPlayerInGame()
                && game.getTeamTurn().equals(sessionData.getColor())
                && !game.isInCheckmate(sessionData.getColor())
                && !game.isInStalemate(sessionData.getColor());

    }


}
