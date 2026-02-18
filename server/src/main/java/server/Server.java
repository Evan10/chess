package server;

import dataAccess.*;
import handler.ChessGameHandler;
import handler.ClearApplicationHandler;
import handler.UserHandler;
import io.javalin.*;
import service.AuthService;
import service.ClearApplicationService;
import service.GameService;
import service.UserService;

import java.util.Collection;
import java.util.List;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        UserDAO userDAO = new MemoryUserDAO();

        AuthService authService = new AuthService(authDAO);
        GameService gameService = new GameService(gameDAO);
        UserService userService = new UserService(userDAO);

        Collection<DAO> DAOs = List.of(authDAO, gameDAO, userDAO);
        ClearApplicationService clearApplicationService = new ClearApplicationService(DAOs);


        ChessGameHandler chessGameHandler = new ChessGameHandler(gameService);
        ClearApplicationHandler clearApplicationHandler =
                new ClearApplicationHandler(clearApplicationService);
        UserHandler userHandler = new UserHandler(userService);

        // Register your endpoints and exception handlers here.
        javalin .before(context -> {})
                .delete("/db", context -> {})
                .post("/user",context -> {})
                .post("/session", context -> {})
                .delete("/session",context -> {})
                .get("/game",context -> {})
                .post("/game", context -> {})
                .put("/game", context -> {});
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
