package client;

import java.util.Scanner;

public class ChessClient {
    private boolean running = false;

    RequestHandler requestHandler;
    public ClientState state;


    public ChessClient(String host){
        state = ClientState.LOGGED_OUT;

        HTTPConnection serverConnection = new HTTPConnection(host,8080);
        requestHandler = new RequestHandler(ChessClient, serverConnection);
        running = true;
        run();

    }


    private void run(){
        Scanner input = new Scanner(System.in);
        String value = "";
        while(running){
            value = input.nextLine();
            String output = requestHandler.handle(value);
            if(output.equals("quit")) {
                running = false;
                continue;
            }
            System.out.printf(output);
        }
        serverConnection.close();
    }

}
