package client;

import jakarta.websocket.DeploymentException;
import ui.EscapeSequences;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class ChessClient {
    private boolean running;

    RequestHandler requestHandler;
    public ClientSessionData sessionData;
    private final ServerFacade httpConnection;
    private final WsClient wsConnection;
    private final ConsoleWriter consoleWriter;
    private final ClientMessageHandler messageHandler;

    public ChessClient(String host){
        int port = 8080;
        sessionData = new ClientSessionData();
        sessionData.setState(ClientState.LOGGED_OUT);

        consoleWriter = new ConsoleWriter(sessionData);
        httpConnection = new ServerFacade(host, port, sessionData);

        messageHandler = new ClientMessageHandler(sessionData,consoleWriter);
        try {
            wsConnection = new WsClient(host, port, messageHandler, sessionData);
        } catch (URISyntaxException | IOException | DeploymentException e) {
            throw new RuntimeException(e);
        }

        requestHandler = new RequestHandler(httpConnection,wsConnection, sessionData, consoleWriter);
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
        httpConnection.close();
        wsConnection.close();
    }



}
