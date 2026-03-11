package util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {

    private static Logger logger;

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("ChessLogger");
            try {
                ensureLogSetup();
                FileHandler handler = new FileHandler("log/Chess.log", true);
                handler.setFormatter(new SimpleFormatter());
                logger.addHandler(handler);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return logger;
    }

    private static void ensureLogSetup() throws IOException {
        File dir = new File("log");
        if(!dir.exists()) {
            if(!dir.mkdir()){
                throw new RuntimeException("Couldn't make directory for logger file");
            }
        }
        File gitAddFile = new File("log/.gitkeep");
        if(!gitAddFile.exists()) {
            if(!gitAddFile.createNewFile()){
                throw new RuntimeException("Couldn't make directory for logger file");
            }
        }
    }

}
