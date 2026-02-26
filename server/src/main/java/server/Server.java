package server;

import dataaccess.*;
import handler.AuthHandler;
import handler.ChessGameHandler;
import handler.ClearApplicationHandler;
import handler.UserHandler;
import io.javalin.Javalin;
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
        UserService userService = new UserService(userDAO, authDAO);

        Collection<DAO> allDAOs = List.of(authDAO, gameDAO, userDAO);
        ClearApplicationService clearApplicationService = new ClearApplicationService(allDAOs);


        ChessGameHandler chessGameHandler = new ChessGameHandler(gameService);
        ClearApplicationHandler clearApplicationHandler =
                new ClearApplicationHandler(clearApplicationService);
        UserHandler userHandler = new UserHandler(userService);

        AuthHandler authHandler = new AuthHandler(authService);

        // Register your endpoints and exception handlers here.
        javalin.before(context -> System.out.println(context.body()))
                .before(authHandler)
                .delete("/db", clearApplicationHandler::handleClearApplication)
                .post("/user", userHandler::registerHandler)
                .post("/session", userHandler::loginHandler)
                .delete("/session", userHandler::logoutHandler)
                .get("/game", chessGameHandler::listGamesHandler)
                .post("/game", chessGameHandler::createGameHandler)
                .put("/game", chessGameHandler::joinGameHandler)
                .after(context -> System.out.println(context.status() + " " + context.result()));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
