package dataaccess.exception;

import util.MyLogger;

import java.sql.SQLException;
import java.util.logging.Logger;

public class SQLStateToErrorConverter {
    private static final Logger LOGGER = MyLogger.getLogger();
    public static DataAccessException convertSQLStateToError(SQLException e){
        LOGGER.info("["+e.getSQLState()+":"+e.getErrorCode()+"] "+e.getMessage());
        if (e.getSQLState().startsWith("08")) { // connectivity error
            return new DatabaseConnectivityException("Error: Internal database error");
        } else if (e.getSQLState().startsWith("23") && e.getErrorCode() == 1062) {
            return new UnavailableRequestException("Error: Unique value already in use");
        } else if (e.getSQLState().startsWith("23")) { // SQLState
            return new InvalidRequestException("Error: Invalid request");
        }else if (e.getSQLState().startsWith("42")) {
            return new InvalidRequestException("Error: Bad request");
        }
        return new DataAccessException(e.getMessage());
    }
}
