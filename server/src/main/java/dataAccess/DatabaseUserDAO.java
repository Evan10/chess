package dataaccess;


import model.UserData;
import util.MyLogger;

import java.sql.*;
import java.util.logging.Logger;

import static dataaccess.DatabaseManager.getConnection;

public class DatabaseUserDAO implements UserDAO {

    private final Logger logger = MyLogger.getLogger();

    protected DatabaseUserDAO() {
    }


    @Override
    public boolean usernameInUse(String username) {
        String statement = """
                SELECT EXISTS(SELECT username FROM users WHERE username = ?)
                """;
        try(PreparedStatement ps = getConnection().prepareStatement(statement)){
            ps.setString(1, username);
            ResultSet set = ps.executeQuery();
            return set.next();
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
        try(PreparedStatement ps = getConnection().prepareStatement(statement)){
            ps.setString(1, userData.username());
            ps.setString(2, userData.password());
            ps.setString(3, userData.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            if(e.getSQLState().startsWith("23")){ // SQLState
                throw new UnavailableRequestException("Error: username already in use");
            } else if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statement = """
                SELECT *
                FROM users
                WHERE username = ?
                """;
        try(PreparedStatement ps = getConnection().prepareStatement(statement)){
            ps.setString(1,username);
            ResultSet rs = ps.executeQuery();
            if(!rs.next()){
                throw new InvalidRequestException("Error: user with given username not found");
            }
            return new UserData(
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("email"));
        }catch (SQLException e){
            if (e.getSQLState().startsWith("08")) { // connectivity error
                throw new DatabaseConnectivityException("Error: Internal Database error");
            }
            throw new DataAccessException(e.toString());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = """
            DELETE FROM users
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
            return !s.executeQuery("SELECT 1 FROM users LIMIT 1;").next();
        }catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
