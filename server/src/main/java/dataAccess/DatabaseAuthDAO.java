package dataaccess;

import model.AuthData;

import java.sql.*;

public class DatabaseAuthDAO implements AuthDAO{

    private final Connection connection;

    public DatabaseAuthDAO() {
        try {
            this.connection = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthData getAuthDataWithAuthToken(String authToken) throws DataAccessException {
        String statement = """
            SELECT authenticationToken, username
            FROM authentication
            WHERE authenticationToken = ?
            """;

        try(PreparedStatement ps = connection.prepareStatement(statement)) {
            ps.setString(1,authToken);
            try(ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AuthData(
                        rs.getString("authenticationToken"),
                        rs.getString("username"));
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void addAuthData(AuthData userData) throws DataAccessException {
        String statement = """
            INSERT INTO authentication
            (authenticationToken, username)
            VALUES (?, ?)
            """;
        try(PreparedStatement ps = connection.prepareStatement(statement)) {
            ps.setString(1,userData.authToken());
            ps.setString(2,userData.username());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void removeAuthData(String authToken) throws DataAccessException {
        String statement = """
            DELETE FROM authentication
            WHERE authenticationToken = ?
            """;
        try(PreparedStatement ps = connection.prepareStatement(statement)) {
            ps.setString(1,authToken);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = """
            DELETE FROM authentication
            """;

        try(PreparedStatement ps = connection.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
