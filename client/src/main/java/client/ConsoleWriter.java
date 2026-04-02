package client;

import chess.ChessGame;
import chess.ChessMove;
import model.GameData;
import ui.EscapeSequences;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static ui.UIChessBoardHelper.uiChessBoard;

public class ConsoleWriter {

    private String prefix = ClientState.LOGGED_OUT.name + ">>";
    private final ClientSessionData sessionData;
    private final StringBuilder messageQueue;
    ConsoleWriter(ClientSessionData sessionData){
        messageQueue = new StringBuilder();
        this.sessionData=sessionData;
    }

    public void writeAndFlushInitMessage(){
        messageQueue.append("Welcome To Chess Client! Type\033[3m help\033[23m to start\n");
        flushToConsole();
    }

    public void writeServerMessage(String message){
        messageQueue.append(EscapeSequences.ERASE_LINE);
        writeMessage(message);
    }
    public void writeServerErrorMessage(String error){
        messageQueue.append(EscapeSequences.ERASE_LINE);
        writeErrorMessage(error);
    }

    public void writeErrorMessage(String error){
        messageQueue.append(EscapeSequences.SET_TEXT_COLOR_RED);
        messageQueue.append(EscapeSequences.SET_TEXT_BOLD);
        messageQueue.append(error);
        messageQueue.append("\n");
        appendResetStringFormatting();
    }

    public void writeMessage(String message){
        messageQueue.append(message);
        messageQueue.append("\n");
    }

    public void writeBoard(ChessGame game){
        writeBoard(game, null);
    }
    public void writeBoard(ChessGame game, Collection<ChessMove> legalMoves){
        messageQueue.append("\n");
        messageQueue.append(uiChessBoard(game,sessionData.getColor(), legalMoves));
        messageQueue.append("\n");
    }

    public void writeGameList(Map<Integer, GameData> games){
        messageQueue.append("Games: ");
        messageQueue.append("\n");
        messageQueue.append(gameCollectionToString(games));
    }

    public void flushToConsole(){
        messageQueue.append(EscapeSequences.ERASE_LINE);
        appendNewCommandLine();
        System.out.printf(messageQueue.toString());
        messageQueue.setLength(0);
    }

    private void appendNewCommandLine(){
        appendResetStringFormatting();
        messageQueue.append(prefix);
    }

    private void appendResetStringFormatting(){
        messageQueue.append(EscapeSequences.RESET_BG_COLOR)
                .append(EscapeSequences.RESET_TEXT_COLOR)
                .append(EscapeSequences.RESET_TEXT_UNDERLINE)
                .append(EscapeSequences.RESET_TEXT_BOLD_FAINT)
                .append(EscapeSequences.RESET_TEXT_BLINKING);
    }

    public void setPrefix(String prefix){
        this.prefix = prefix + ">>";
    }

    private String gameCollectionToString(Map<Integer, GameData> games){
        return games.entrySet().stream().map((g ->
                g.getValue().gameName() +":\n"
                        + "    Position: " + g.getKey() + "\n"
                        + "    Black: " + g.getValue().blackUsername() + "\n"
                        + "    White: " + g.getValue().whiteUsername() + "\n"

        )).collect(Collectors.joining());

    }


}
