package client;

import ui.EscapeSequences;

import java.util.Scanner;

public class ChessClient {
    private boolean running = false;

    RequestHandler requestHandler;
    public ClientSessionData sessionData;
    private final ServerFacade serverConnection;

    public ChessClient(String host){
        sessionData = new ClientSessionData();
        sessionData.setState(ClientState.LOGGED_OUT);

        serverConnection = new ServerFacade(host,8080, sessionData);
        requestHandler = new RequestHandler(serverConnection, sessionData);
        running = true;
        run();

    }


    private void run(){
        Scanner input = new Scanner(System.in);
        String value = "";
        System.out.println("Welcome To Chess Client! Type help to start");
        printNewCommandLine();
        while(running){
            value = input.nextLine();
            String output = requestHandler.handle(value);
            if(output.equals("quit")) {
                running = false;
                continue;
            }
            System.out.printf(output +"\n");
            printNewCommandLine();
        }
        serverConnection.close();
    }

    private void printNewCommandLine(){
        System.out.printf(EscapeSequences.RESET_BG_COLOR +
                EscapeSequences.RESET_TEXT_COLOR +
                EscapeSequences.RESET_TEXT_UNDERLINE +
                EscapeSequences.RESET_TEXT_BOLD_FAINT+
                sessionData.getState().name +" >>");
    }



}
