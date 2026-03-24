import client.ChessClient;


public class ClientMain {

    ChessClient client;
    public static void main(String[] args) {
        String host = "localhost";
        if(args.length>0){
            host = args[0];
        }
        new ChessClient(host);
    }







}
