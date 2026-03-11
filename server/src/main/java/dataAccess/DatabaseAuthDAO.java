package dataaccess;

import dataaccess.exception.DataAccessException;
import dataaccess.exception.InvalidRequestException;
import model.AuthData;
import util.MyLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static dataaccess.DatabaseManager.getConnection;
import static dataaccess.exception.SQLStateToErrorConverter.SQLStateToError;

public class DatabaseAuthDAO implements AuthDAO {

    private final static Logger LOGGER = MyLogger.getLogger();

    protected DatabaseAuthDAO() {
    }

    @Override
    public AuthData getAuthDataWithAuthToken(String authToken) throws DataAccessException {
        String statement = """
                SELECT authenticationToken, username
                FROM authentication
                WHERE authenticationToken = ?
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new InvalidRequestException("Error: invalid authToken");
                }
                return new AuthData(
                        rs.getString("authenticationToken"),
                        rs.getString("username"));
            }
        } catch (SQLException e) {
            LOGGER.warning(e.toString());
            throw SQLStateToError(e);
        }
    }

    @Override
    public void addAuthData(AuthData userData) throws DataAccessException {
        String statement = """
                INSERT INTO authentication
                (authenticationToken, username)
                VALUES (?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, userData.authToken());
            ps.setString(2, userData.username());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warning(e.toString());
            throw SQLStateToError(e);
        }
    }

    @Override
    public void removeAuthData(String authToken) throws DataAccessException {
        String statement = """
                DELETE FROM authentication
                WHERE authenticationToken = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, authToken);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new InvalidRequestException("Error: invalid request");
            }
        } catch (SQLException e) {
            LOGGER.warning(e.toString());
            throw SQLStateToError(e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = """
                DELETE FROM authentication
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warning(e.toString());
            throw SQLStateToError(e);
        }
    }

    @Override
    public boolean isEmpty() {
        try (Statement s = getConnection().createStatement()) {
            return !s.executeQuery("SELECT 1 FROM authentication LIMIT 1;").next();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
