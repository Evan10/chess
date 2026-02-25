package dataAccess;

@SuppressWarnings("RedundantThrows")
public interface DAO {
    void clear() throws DataAccessException;
    boolean isEmpty();
}
