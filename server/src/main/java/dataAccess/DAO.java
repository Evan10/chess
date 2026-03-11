package dataaccess;

import dataaccess.exception.DataAccessException;

@SuppressWarnings("RedundantThrows")
public interface DAO {
    void clear() throws DataAccessException;

    boolean isEmpty();
}
