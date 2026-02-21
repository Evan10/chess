package server;

import dataAccess.*;
import handler.AuthHandler;
import handler.ChessGameHandler;
import handler.ClearApplicationHandler;
import handler.UserHandler;
import io.javalin.*;
import model.AuthData;
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
        UserService userService = new UserService(userDAO,authDAO);

        Collection<DAO> DAOs = List.of(authDAO, gameDAO, userDAO);
        ClearApplicationService clearApplicationService = new ClearApplicationService(DAOs);


        ChessGameHandler chessGameHandler = new ChessGameHandler(gameService);
        ClearApplicationHandler clearApplicationHandler =
                new ClearApplicationHandler(clearApplicationService);
        UserHandler userHandler = new UserHandler(userService);

        AuthHandler authHandler = new AuthHandler(authService);

        // Register your endpoints and exception handlers here.
        javalin .before(authHandler)
                .delete("/db", context -> {
                    context.result(clearApplicationHandler.handleClearApplication(context.body()));
                })
                .post("/user",context -> {
                    context.result(userHandler.registerHandler(context.body()));
                })
                .post("/session", context -> {
                    context.result(userHandler.loginHandler(context.body()));
                })
                .delete("/session",context -> {
                    if(!AuthHandler.isAuth(context)) return;
                    context.result(userHandler.logoutHandler(context.body()));
                })
                .get("/game",context -> {
                    if(!AuthHandler.isAuth(context)) return;
                    context.result(chessGameHandler.listGamesHandler(context.body()));
                })
                .post("/game", context -> {
                    if(!AuthHandler.isAuth(context)) return;
                    context.result(chessGameHandler.createGameHandler(context.body()));
                })
                .put("/game", context -> {
                    if(!AuthHandler.isAuth(context)) return;
                    context.result(chessGameHandler.joinGameHandler(context.body()));
                });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
