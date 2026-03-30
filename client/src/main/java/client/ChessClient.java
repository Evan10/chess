package client;

import ui.EscapeSequences;
import java.util.Scanner;

public class ChessClient {
    private boolean running;

    RequestHandler requestHandler;
    public ClientSessionData sessionData;
    private final ServerFacade serverConnection;
    private final ConsoleWriter consoleWriter;

    public ChessClient(String host){
        sessionData = new ClientSessionData();
        sessionData.setState(ClientState.LOGGED_OUT);

        consoleWriter = new ConsoleWriter(sessionData);
        serverConnection = new ServerFacade(host,8080, sessionData);
        requestHandler = new RequestHandler(serverConnection, sessionData, consoleWriter);
        running = true;
        run();
    }


    private void run(){
        Scanner input = new Scanner(System.in);
        String value;
        consoleWriter.writeAndFlushInitMessage();
        while(running){
            value = input.nextLine();
            boolean quit = requestHandler.handle(value);
            if(quit) {
                running = false;
                continue;
            }
            consoleWriter.flushToConsole();
        }
        serverConnection.close();
    }



}
