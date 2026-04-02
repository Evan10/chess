package client;

import java.util.Scanner;

public class ChessClient {
    private boolean running;

    RequestHandler requestHandler;
    public ClientSessionData sessionData;
    private final ServerFacade httpConnection;
    private WsClient wsConnection;
    private final ConsoleWriter consoleWriter;

    public ChessClient(String host){
        int port = 8080;
        sessionData = new ClientSessionData();
        sessionData.setState(ClientState.LOGGED_OUT);

        consoleWriter = new ConsoleWriter(sessionData);
        httpConnection = new ServerFacade(host, port, sessionData);

        ClientMessageHandler messageHandler = new ClientMessageHandler(sessionData, consoleWriter);
        try {
            wsConnection = new WsClient(host, port, messageHandler, sessionData);
        } catch (Exception e) {
            consoleWriter.writeErrorMessage("Error: Unable to connect to websocket server;\n server may not be running");
            consoleWriter.flushToConsole();
            return;
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
