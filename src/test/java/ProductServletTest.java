import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.clevertec.config.DataSourceConfig;
import ru.clevertec.dao.ConnectionPoolManager;
import ru.clevertec.dao.impl.ProductDao;
import ru.clevertec.models.Product;
import ru.clevertec.servlets.CheckServlet;
import ru.clevertec.servlets.ProductServlet;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServletTest {

    private ProductServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ProductDao productDao;

    @BeforeEach
    void setUp() throws Exception {

        servlet = new ProductServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        productDao = mock(ProductDao.class);
        ConnectionPoolManager connectionPoolManager = mock(ConnectionPoolManager.class);
        Connection connection = mock(Connection.class);
        DataSourceConfig dataSourceConfig = mock(DataSourceConfig.class);
        DataSource dataSource = mock(DataSource.class);

        when(dataSourceConfig.getDataSource()).thenReturn(dataSource);
        when(connectionPoolManager.getConnection()).thenReturn(connection);

        Field dataSourceConfigField = ProductServlet.class.getDeclaredField("dataSourceConfig");
        dataSourceConfigField.setAccessible(true);
        dataSourceConfigField.set(servlet, dataSourceConfig);
        dataSourceConfigField.setAccessible(false);

        servlet.init();

        Field connectionPoolManagerField = ProductServlet.class.getDeclaredField("connectionPoolManager");
        connectionPoolManagerField.setAccessible(true);
        connectionPoolManagerField.set(servlet, connectionPoolManager);
        connectionPoolManagerField.setAccessible(false);

        Field daoField = ProductServlet.class.getDeclaredField("productDao");
        daoField.setAccessible(true);
        daoField.set(servlet, productDao);
        daoField.setAccessible(false);
    }

    @Test
    void testInit() throws Exception {
        servlet.destroy();
        DataSourceConfig dataSourceConfig = mock(DataSourceConfig.class);
        DataSource dataSource = mock(DataSource.class);
        when(dataSourceConfig.getDataSource()).thenReturn(dataSource);

        servlet.init();

        Field connectionPoolManagerField = ProductServlet
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
        when(productDao.getById(1)).thenReturn(new Product(
                1L,
                "description",
                1,
                1,
                false));

        servlet.doGet(request, response);

        verify(productDao, times(1)).getById(1);
        verify(response, times(1)).setContentType("application/json");

        writer.flush();
        assertTrue(stringWriter.toString().contains("\"id\":1"));
        assertTrue(stringWriter.toString().contains("\"description\":\"description\""));
        assertTrue(stringWriter.toString().contains("\"price\":1"));
        assertTrue(stringWriter.toString().contains("\"quantityInStock\":1"));
        assertTrue(stringWriter.toString().contains("\"wholesaleProduct\":false"));
    }

    @Test
    void testDoGetWithNullIdParam() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getParameter("id")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);
        when(productDao.getAll()).thenReturn(List.of(
                new Product(1L, "d1", 1, 1, false),
                new Product(2L, "d1", 1, 1, false),
                new Product(3L, "d1", 1, 1, false)
        ));

        servlet.doGet(request, response);

        verify(productDao, times(1)).getAll();
        verify(response, times(1)).setContentType("application/json");

        writer.flush();
        assertTrue(stringWriter.toString().contains("\"id\":1"));
        assertTrue(stringWriter.toString().contains("\"id\":2"));
        assertTrue(stringWriter.toString().contains("\"id\":3"));
    }

    @Test
    void testDoGetHandlesException() {
        when(request.getParameter("id")).thenReturn("1");
        when(productDao.getById(1)).thenThrow(new RuntimeException("Database error"));

        servlet.doGet(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void testDoPost() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getReader()).thenReturn(new BufferedReader(
                new StringReader("{" +
                        "\"id\":1," +
                        "\"description\":\"d1\"," +
                        "\"price\":1," +
                        "\"quantityInStock\":1," +
                        "\"wholesaleProduct\":false}"
                        )));
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productDao, times(1)).save(captor.capture());

        assertEquals(1, captor.getValue().getId());
        assertEquals("d1", captor.getValue().getDescription());
        assertEquals(1, captor.getValue().getPrice());
        assertEquals(1, captor.getValue().getQuantityInStock());
        assertFalse(captor.getValue().isWholesaleProduct());
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);

        writer.flush();
        assertTrue(stringWriter.toString().contains("\"id\":1"));
        assertTrue(stringWriter.toString().contains("\"description\":\"d1\""));
        assertTrue(stringWriter.toString().contains("\"price\":1"));
        assertTrue(stringWriter.toString().contains("\"quantityInStock\":1"));
        assertTrue(stringWriter.toString().contains("\"wholesaleProduct\":false"));

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
                new StringReader("{" +
                        "\"id\":1," +
                        "\"description\":\"d1\"," +
                        "\"price\":1," +
                        "\"quantityInStock\":1," +
                        "\"wholesaleProduct\":false}"
                )));
        when(response.getWriter()).thenReturn(writer);

        servlet.doPut(request, response);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productDao, times(1)).update(captor.capture());

        assertEquals(1, captor.getValue().getId());
        assertEquals("d1", captor.getValue().getDescription());
        assertEquals(1, captor.getValue().getPrice());
        assertEquals(1, captor.getValue().getQuantityInStock());
        assertFalse(captor.getValue().isWholesaleProduct());
        verify(response, times(1)).setContentType("application/json");

        writer.flush();
        assertTrue(stringWriter.toString().contains("\"id\":1"));
        assertTrue(stringWriter.toString().contains("\"description\":\"d1\""));
        assertTrue(stringWriter.toString().contains("\"price\":1"));
        assertTrue(stringWriter.toString().contains("\"quantityInStock\":1"));
        assertTrue(stringWriter.toString().contains("\"wholesaleProduct\":false"));
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

        verify(productDao, times(1)).delete(1);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);

        writer.flush();
        assertEquals("", stringWriter.toString());
    }

    @Test
    void testDoDeleteHandlesException() {
        when(request.getParameter("id")).thenReturn("1");
        doThrow(new RuntimeException("Database error")).when(productDao).delete(1);

        servlet.doDelete(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }


    @Test
    void testDestroy() throws Exception{
        ConnectionPoolManager connectionPoolManager = mock(ConnectionPoolManager.class);

        Field connectionPoolManagerField = ProductServlet
                .class
                .getDeclaredField("connectionPoolManager");

        connectionPoolManagerField.setAccessible(true);
        connectionPoolManagerField.set(servlet, connectionPoolManager);
        connectionPoolManagerField.setAccessible(false);

        servlet.destroy();

        verify(connectionPoolManager, times(1)).closeAllConnections();
    }
}
