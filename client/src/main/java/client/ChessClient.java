package client;

import java.util.Scanner;

public class ChessClient {
    private boolean running = false;

    RequestHandler requestHandler;
    private ClientState state;
    private final HTTPConnection serverConnection;

    public ChessClient(String host){
        state = ClientState.LOGGED_OUT;

        serverConnection = new HTTPConnection(host,8080);
        requestHandler = new RequestHandler(serverConnection, this);
        running = true;
        run();

    }


    private void run(){
        Scanner input = new Scanner(System.in);
        String value = "";
        System.out.println("Welcome To Chess Client! Type help to start");
        System.out.printf(state.name +" >>");
        while(running){
            value = input.nextLine();
            String output = requestHandler.handle(value);
            if(output.equals("quit")) {
                running = false;
                continue;
            }
            System.out.printf(output +"\n");
            System.out.printf(state.name +" >>");
        }
        serverConnection.close();
    }


    public void setState(ClientState state){
        this.state = state;
    }
    public ClientState getState(){
        return state;
    }

}
