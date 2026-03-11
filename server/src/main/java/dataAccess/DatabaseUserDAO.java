package dataaccess;


import dataaccess.exception.DataAccessException;
import dataaccess.exception.InvalidRequestException;
import model.UserData;
import util.MyLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static dataaccess.DatabaseManager.getConnection;
import static dataaccess.exception.SQLStateToErrorConverter.SQLStateToError;

public class DatabaseUserDAO implements UserDAO {

    private final static Logger LOGGER = MyLogger.getLogger();

    protected DatabaseUserDAO() {
    }


    @Override
    public boolean usernameInUse(String username) {
        String statement = """
                SELECT EXISTS(SELECT username FROM users WHERE username = ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, username);
            ResultSet set = ps.executeQuery();
            set.next();
            return set.getBoolean(1);
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUser(UserData userData) throws DataAccessException {
        String statement = """
                INSERT INTO users (username, password_hash, email)
                VALUES (?,?,?);
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, userData.username());
            ps.setString(2, userData.password());
            ps.setString(3, userData.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw SQLStateToError(e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statement = """
                SELECT *
                FROM users
                WHERE username = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(statement)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new InvalidRequestException("Error: user with given username not found");
            }
            return new UserData(
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("email"));
        } catch (SQLException e) {
            throw SQLStateToError(e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = """
                DELETE FROM users
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
            return !s.executeQuery("SELECT 1 FROM users LIMIT 1;").next();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
