package client;

import chess.*;
import client.dataaccess.HTTPConnection;

import java.util.Scanner;

public class ClientMain {

    private boolean running = false;
    HTTPConnection serverConnection;
    public static void main(String[] args) {


    }

    ClientMain(){
        serverConnection = new HTTPConnection();
        running = true;
        run();

    }


    private void run(){

        Scanner input = new Scanner(System.in);
        String value = "";
        while(running){
            value = input.nextLine();
            System.out.printf(getFormattedResponse(value));
        }
    }

    private String getFormattedResponse(String message){


        return "unknown Input try again";

    }





}
