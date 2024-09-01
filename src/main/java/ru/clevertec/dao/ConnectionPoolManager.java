package ru.clevertec.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPoolManager {
    private final List<Connection> connectionPool = new ArrayList<>();
    private final DataSource dataSource;
    private final int poolSize;

    public ConnectionPoolManager(DataSource dataSource, int poolSize) {
        this.dataSource = dataSource;
        this.poolSize = poolSize;
        initializeConnectionPool();
    }

    public int getCurrentPoolSize() {
        return connectionPool.size();
    }

    private void initializeConnectionPool() {
        try {
            for (int i = 0; i < poolSize; i++) {
                connectionPool.add(createNewConnectionForPool());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing connection pool", e);
        }
    }

    private Connection createNewConnectionForPool() throws SQLException {
        return dataSource.getConnection();
    }

    public synchronized Connection getConnection() {
        if (connectionPool.isEmpty()) {
            throw new RuntimeException("No available connection in the pool");
        }
        return connectionPool.removeLast();
    }

    public synchronized void releaseConnection(Connection connection) throws SQLException {
        if(getCurrentPoolSize() < poolSize) {
            connectionPool.add(connection);
        } else {
            throw new SQLException("Pool is full");
        }

    }

    public synchronized void closeAllConnections() {
        for (Connection connection : connectionPool) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

