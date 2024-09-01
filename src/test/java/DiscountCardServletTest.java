import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.clevertec.config.DataSourceConfig;
import ru.clevertec.dao.ConnectionPoolManager;
import ru.clevertec.dao.impl.DiscountCardDao;
import ru.clevertec.models.DiscountCard;
import ru.clevertec.servlets.DiscountCardServlet;
import ru.clevertec.servlets.ProductServlet;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscountCardServletTest {

    private DiscountCardServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private DiscountCardDao discountCardDao;

    @BeforeEach
    void setUp() throws Exception {
        System.setProperty("datasource.url", "jdbc:postgresql://localhost:5432/testdb");
        System.setProperty("datasource.username", "postgres");
        System.setProperty("datasource.password", "root");

        servlet = new DiscountCardServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        discountCardDao = mock(DiscountCardDao.class);
        ConnectionPoolManager connectionPoolManager = mock(ConnectionPoolManager.class);
        Connection connection = mock(Connection.class);
        DataSourceConfig dataSourceConfig = mock(DataSourceConfig.class);
        DataSource dataSource = mock(DataSource.class);

        when(dataSourceConfig.getDataSource()).thenReturn(dataSource);
        when(connectionPoolManager.getConnection()).thenReturn(connection);

        Field dataSourceConfigField = DiscountCardServlet.class.getDeclaredField("dataSourceConfig");
        dataSourceConfigField.setAccessible(true);
        dataSourceConfigField.set(servlet, dataSourceConfig);
        dataSourceConfigField.setAccessible(false);



        servlet.init();

        Field connectionPoolManagerField = DiscountCardServlet.class.getDeclaredField("connectionPoolManager");
        connectionPoolManagerField.setAccessible(true);
        connectionPoolManagerField.set(servlet, connectionPoolManager);
        connectionPoolManagerField.setAccessible(false);

        Field daoField = DiscountCardServlet.class.getDeclaredField("discountCardDao");
        daoField.setAccessible(true);
        daoField.set(servlet, discountCardDao);
        daoField.setAccessible(false);
    }

    @Test
    void testInit() throws Exception {
        servlet.destroy();
        DataSourceConfig dataSourceConfig = mock(DataSourceConfig.class);
        DataSource dataSource = mock(DataSource.class);
        when(dataSourceConfig.getDataSource()).thenReturn(dataSource);

        servlet.init();

        Field connectionPoolManagerField = DiscountCardServlet
                .class
                .getDeclaredField("connectionPoolManager");
        connectionPoolManagerField.setAccessible(true);
        assertNotNull(connectionPoolManagerField.get(servlet));
        connectionPoolManagerField.setAccessible(false);
    }

    @Test
    void testDoGet() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getParameter("id")).thenReturn("1");
        when(response.getWriter()).thenReturn(writer);
        when(discountCardDao.getById(1)).thenReturn(new DiscountCard(1L, 123456, (short) 10));

        servlet.doGet(request, response);

        verify(discountCardDao, times(1)).getById(1);
        verify(response, times(1)).setContentType("application/json");

        writer.flush();
        assertTrue(stringWriter.toString().contains("\"id\":1"));
        assertTrue(stringWriter.toString().contains("\"number\":123456"));
        assertTrue(stringWriter.toString().contains("\"discountPercentage\":10"));
    }

    @Test
    void testDoGetWithNullIdParam() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getParameter("id")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);
        when(discountCardDao.getAll()).thenReturn(List.of(
                new DiscountCard(1L, 1111, (short)0),
                new DiscountCard(2L, 2222, (short)10),
                new DiscountCard(3L, 3333, (short)10)
        ));

        servlet.doGet(request, response);

        verify(discountCardDao, times(1)).getAll();
        verify(response, times(1)).setContentType("application/json");

        writer.flush();
        assertTrue(stringWriter.toString().contains("\"id\":1"));
        assertTrue(stringWriter.toString().contains("\"id\":2"));
        assertTrue(stringWriter.toString().contains("\"id\":3"));
    }

    @Test
    void testDoGetHandlesException() {
        when(request.getParameter("id")).thenReturn("1");
        when(discountCardDao.getById(1)).thenThrow(new RuntimeException("Database error"));

        servlet.doGet(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void testDoPost() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getReader()).thenReturn(new BufferedReader(
                new StringReader("{\"number\":123456,\"discountPercentage\":15}")));
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);

        ArgumentCaptor<DiscountCard> captor = ArgumentCaptor.forClass(DiscountCard.class);
        verify(discountCardDao, times(1)).save(captor.capture());

        assertEquals(123456, captor.getValue().getNumber());
        assertEquals(15, captor.getValue().getDiscountPercentage());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);

        writer.flush();
        assertTrue(stringWriter.toString().contains("\"number\":123456"));
        assertTrue(stringWriter.toString().contains("\"discountPercentage\":15"));
    }

    @Test
    void testDoPostHandlesException() throws Exception {
        when(request.getReader()).thenThrow(new IOException("IO Error"));

        servlet.doPost(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void testDoPut() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getParameter("id")).thenReturn("1");
        when(request.getReader()).thenReturn(new BufferedReader(
                new StringReader("{\"number\":123456,\"discountPercentage\":20}")));
        when(response.getWriter()).thenReturn(writer);

        servlet.doPut(request, response);

        ArgumentCaptor<DiscountCard> captor = ArgumentCaptor.forClass(DiscountCard.class);
        verify(discountCardDao, times(1)).update(captor.capture());

        assertEquals(123456, captor.getValue().getNumber());
        assertEquals(20, captor.getValue().getDiscountPercentage());
        verify(response, times(1)).setContentType("application/json");

        writer.flush();
        assertTrue(stringWriter.toString().contains("\"number\":123456"));
        assertTrue(stringWriter.toString().contains("\"discountPercentage\":20"));
    }

    @Test
    void testDoPutHandlesException() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        when(request.getReader()).thenThrow(new IOException("IO Error"));

        servlet.doPut(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void testDoDelete() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getParameter("id")).thenReturn("1");
        when(response.getWriter()).thenReturn(writer);

        servlet.doDelete(request, response);

        verify(discountCardDao, times(1)).delete(1);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);

        writer.flush();
        assertEquals("", stringWriter.toString());
    }

    @Test
    void testDoDeleteHandlesException() throws Exception {
        when(request.getParameter("id")).thenReturn("1");
        doThrow(new RuntimeException("Database error")).when(discountCardDao).delete(1);

        servlet.doDelete(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }


    @Test
    void testDestroy() throws Exception{
        ConnectionPoolManager connectionPoolManager = mock(ConnectionPoolManager.class);

        Field connectionPoolManagerField = DiscountCardServlet
                .class
                .getDeclaredField("connectionPoolManager");

        connectionPoolManagerField.setAccessible(true);
        connectionPoolManagerField.set(servlet, connectionPoolManager);
        connectionPoolManagerField.setAccessible(false);

        servlet.destroy();

        verify(connectionPoolManager, times(1)).closeAllConnections();
    }
}
