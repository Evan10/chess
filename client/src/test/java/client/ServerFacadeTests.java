package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static ClientSessionData sessionData;
    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        sessionData = new ClientSessionData();
        facade = new ServerFacade("localhost",port,sessionData);
    }

    @BeforeEach
    public void setup(){
        try {
            facade.clearDatabase();
            facade.logout();
        } catch (FailResponseCodeException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
