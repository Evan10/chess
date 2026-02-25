import server.Server;

public class ServerMain {
    public static void main(String[] args) {

        Server server = new Server();
        int port = 8080;
        port = server.run(port);

        System.out.println("♕ 240 Chess Server running on: "+port );
    }
}
