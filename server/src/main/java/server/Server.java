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
import util.Constants;
import util.MyLogger;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


public class Server {

    private final Javalin javalin;
    private final static Logger logger = MyLogger.getLogger();
    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        AuthDAO authDAO = DAOFactory.getAuthDAO();
        GameDAO gameDAO = DAOFactory.getGameDAO();
        UserDAO userDAO = DAOFactory.getUserDAO();


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
        javalin.before(context -> logger.info(context.body()))
                .before(authHandler)
                .delete("/db", clearApplicationHandler::handleClearApplication)
                .post("/user", userHandler::registerHandler)
                .post("/session", userHandler::loginHandler)
                .delete("/session", userHandler::logoutHandler)
                .get("/game", chessGameHandler::listGamesHandler)
                .post("/game", chessGameHandler::createGameHandler)
                .put("/game", chessGameHandler::joinGameHandler)
                .after(context -> logger.info(context.status() + " " + context.result()))
                .exception(DatabaseConnectivityException.class,((e, context) ->
                        context.status(Constants.SERVER_ERROR)
                                .result("{ \"message\": \"Error: Internal database error\"}")
                ));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
