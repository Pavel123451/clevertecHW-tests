import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.dao.ConnectionPoolManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionPoolManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private ConnectionPoolManager connectionPoolManager;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);

        connectionPoolManager = new ConnectionPoolManager(dataSource, 2);
    }

    @Test
    void testInitializeConnectionPool() throws SQLException {
        verify(dataSource, times(2)).getConnection();
    }

    @Test
    void testGetConnectionSuccess() {
        Connection conn1 = connectionPoolManager.getConnection();
        Connection conn2 = connectionPoolManager.getConnection();

        assertNotNull(conn1);
        assertNotNull(conn2);

        assertThrows(RuntimeException.class, () -> connectionPoolManager.getConnection());
    }

    @Test
    void testReleaseConnection() throws SQLException {
        Connection conn1 = connectionPoolManager.getConnection();
        connectionPoolManager.releaseConnection(conn1);

        assertDoesNotThrow(() -> connectionPoolManager.getConnection());
    }

    @Test
    void testCloseAllConnections() throws SQLException {
        connectionPoolManager.closeAllConnections();

        verify(connection, times(2)).close();
    }

    @Test
    void testInitializeConnectionPoolThrowsSQLException() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Test SQLException"));

        assertThrows(RuntimeException.class, () -> new ConnectionPoolManager(dataSource, 2));
    }

    @Test
    void testGetConnectionThrowsExceptionWhenPoolIsEmpty() {
        connectionPoolManager.getConnection();
        connectionPoolManager.getConnection();

        assertThrows(RuntimeException.class, () -> connectionPoolManager.getConnection());
    }

    @Test
    void testReleaseConnectionThrowsExceptionWhenPoolIsFool() throws SQLException {
        Connection conn1 = connectionPoolManager.getConnection();
        connectionPoolManager.releaseConnection(conn1);

        assertThrows(SQLException.class, () -> connectionPoolManager.releaseConnection(conn1));
    }
}