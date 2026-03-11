package dataaccess;

import model.AuthData;
import util.MyLogger;

import java.sql.*;
import java.util.logging.Logger;

import static dataaccess.DatabaseManager.getConnection;

public class DatabaseAuthDAO implements AuthDAO{

    private static Logger logger = MyLogger.getLogger();

    protected DatabaseAuthDAO() {
    }

    @Override
    public AuthData getAuthDataWithAuthToken(String authToken) throws DataAccessException {
        String statement = """
            SELECT authenticationToken, username
            FROM authentication
            WHERE authenticationToken = ?
            """;

        try(PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1,authToken);
            try(ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new InvalidRequestException("Error: invalid authToken");
                }
                return new AuthData(
                        rs.getString("authenticationToken"),
                        rs.getString("username"));
            }
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            }else if(e.getSQLState().startsWith("23")){
                throw new InvalidRequestException("Error: Invalid request");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void addAuthData(AuthData userData) throws DataAccessException {
        String statement = """
            INSERT INTO authentication
            (authenticationToken, username)
            VALUES (?, ?)
            """;
        try(PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1,userData.authToken());
            ps.setString(2,userData.username());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            }else if(e.getSQLState().startsWith("23")){
                throw new InvalidRequestException("Error: Invalid request");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void removeAuthData(String authToken) throws DataAccessException {
        String statement = """
            DELETE FROM authentication
            WHERE authenticationToken = ?
            """;
        try(PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1,authToken);
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected==0){
                throw new InvalidRequestException("Error: invalid request");
            }
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            }else if(e.getSQLState().startsWith("23")){
                throw new InvalidRequestException("Error: Invalid request");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = """
            DELETE FROM authentication
            """;
        try(PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning(e.toString());
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public boolean isEmpty() {
        try(Statement s = getConnection().createStatement()){
            return !s.executeQuery("SELECT 1 FROM authentication LIMIT 1;").next();
        }catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
