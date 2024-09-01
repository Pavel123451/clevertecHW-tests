import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.config.DataSourceConfig;
import ru.clevertec.dao.ConnectionPoolManager;
import ru.clevertec.dao.impl.DiscountCardDao;
import ru.clevertec.dao.impl.ProductDao;
import ru.clevertec.models.DiscountCard;
import ru.clevertec.models.Product;
import ru.clevertec.servlets.CheckServlet;
import ru.clevertec.utils.JsonUtil;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CheckServletTest {

    private CheckServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ConnectionPoolManager connectionPoolManager;
    private ProductDao productDao;
    private DiscountCardDao discountCardDao;

    @BeforeEach
    void setUp() throws Exception {

        servlet = new CheckServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        connectionPoolManager = mock(ConnectionPoolManager.class);
        Connection connection = mock(Connection.class);
        productDao = mock(ProductDao.class);
        discountCardDao = mock(DiscountCardDao.class);
        DataSourceConfig dataSourceConfig = mock(DataSourceConfig.class);
        DataSource dataSource = mock(DataSource.class);

        when(dataSourceConfig.getDataSource()).thenReturn(dataSource);
        when(connectionPoolManager.getConnection()).thenReturn(connection);

        Field dataSourceConfigField = CheckServlet.class.getDeclaredField("dataSourceConfig");
        dataSourceConfigField.setAccessible(true);
        dataSourceConfigField.set(servlet, dataSourceConfig);
        dataSourceConfigField.setAccessible(false);

        servlet.init();

        Field connectionPoolManagerField = CheckServlet.class.getDeclaredField("connectionPoolManager");
        connectionPoolManagerField.setAccessible(true);
        connectionPoolManagerField.set(servlet, connectionPoolManager);
        connectionPoolManagerField.setAccessible(false);
    }

    @Test
    void testInit() throws Exception {
        servlet.destroy();
        DataSourceConfig dataSourceConfig = mock(DataSourceConfig.class);
        DataSource dataSource = mock(DataSource.class);
        when(dataSourceConfig.getDataSource()).thenReturn(dataSource);

        servlet.init();

        Field connectionPoolManagerField = CheckServlet.class.getDeclaredField("connectionPoolManager");
        connectionPoolManagerField.setAccessible(true);
        assertNotNull(connectionPoolManagerField.get(servlet));
        connectionPoolManagerField.setAccessible(false);
    }

    @Test
    void testDoPostSuccess() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        CheckServlet.CheckRequest checkRequest = new CheckServlet.CheckRequest();
        checkRequest.setProducts(List.of(new CheckServlet.CheckRequest.ProductQuantity(1, 2)));
        checkRequest.setDiscountCard(123456);
        checkRequest.setBalanceDebitCard(500.0);

        when(request.getReader()).thenReturn(new BufferedReader(
                new StringReader(JsonUtil.toJson(checkRequest))));
        when(response.getWriter()).thenReturn(writer);
        when(productDao.getAll()).thenReturn(List.of(new Product(
                1L,
                "description",
                1,
                3,
                false
        )));
        when(discountCardDao.getByNumber(123456)).thenReturn(new DiscountCard(
                1L,
                123456,
                (short) 10));

        Field productDaoField = CheckServlet.class.getDeclaredField("productDao");
        productDaoField.setAccessible(true);
        productDaoField.set(servlet, productDao);
        productDaoField.setAccessible(false);

        Field discountCardDaoField = CheckServlet.class.getDeclaredField("discountCardDao");
        discountCardDaoField.setAccessible(true);
        discountCardDaoField.set(servlet, discountCardDao);
        discountCardDaoField.setAccessible(false);

        servlet.doPost(request, response);

        verify(response, times(1)).setContentType("text/csv");
        verify(response, times(1)).setHeader(
                "Content-Disposition",
                "attachment; filename=\"check.csv\"");

        writer.flush();
        assertTrue(stringWriter.toString().contains("description"));
    }

    @Test
    void testDoPostBadRequestWithNotEnoughMoneyException() throws Exception {
        servlet.doPost(request, response);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        CheckServlet.CheckRequest checkRequest = new CheckServlet.CheckRequest();
        checkRequest.setProducts(List.of(new CheckServlet.CheckRequest.ProductQuantity(1, 2)));
        checkRequest.setDiscountCard(123456);
        checkRequest.setBalanceDebitCard(500.0);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(JsonUtil.toJson(checkRequest))));
        when(response.getWriter()).thenReturn(writer);
        when(productDao.getAll()).thenReturn(List.of(new Product(
                1L,
                "description",
                10000,
                3,
                false
        )));
        when(discountCardDao.getByNumber(123456)).thenReturn(new DiscountCard(
                1L,
                123456,
                (short) 10));

        Field productDaoField = CheckServlet.class.getDeclaredField("productDao");
        productDaoField.setAccessible(true);
        productDaoField.set(servlet, productDao);
        productDaoField.setAccessible(false);

        Field discountCardDaoField = CheckServlet.class.getDeclaredField("discountCardDao");
        discountCardDaoField.setAccessible(true);
        discountCardDaoField.set(servlet, discountCardDao);
        discountCardDaoField.setAccessible(false);

        servlet.doPost(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);

    }

    @Test
    void testDoPostBadRequestWithBadRequestException() throws Exception {
        servlet.doPost(request, response);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        CheckServlet.CheckRequest checkRequest = new CheckServlet.CheckRequest();
        checkRequest.setProducts(List.of(new CheckServlet.CheckRequest.ProductQuantity(1, 2)));
        checkRequest.setDiscountCard(123456);
        checkRequest.setBalanceDebitCard(500.0);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(JsonUtil.toJson(checkRequest))));
        when(response.getWriter()).thenReturn(writer);
        when(productDao.getAll()).thenReturn(List.of(new Product(
                1L,
                "description",
                1,
                1,
                false
        )));
        when(discountCardDao.getByNumber(123456)).thenReturn(new DiscountCard(
                1L,
                123456,
                (short) 10));

        Field productDaoField = CheckServlet.class.getDeclaredField("productDao");
        productDaoField.setAccessible(true);
        productDaoField.set(servlet, productDao);
        productDaoField.setAccessible(false);

        Field discountCardDaoField = CheckServlet.class.getDeclaredField("discountCardDao");
        discountCardDaoField.setAccessible(true);
        discountCardDaoField.set(servlet, discountCardDao);
        discountCardDaoField.setAccessible(false);

        servlet.doPost(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_BAD_REQUEST);

    }

    @Test
    void testDoPostInternalServerError() throws Exception {
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));
        when(connectionPoolManager.getConnection()).thenThrow(new RuntimeException("Database error"));

        servlet.doPost(request, response);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void testDestroy() throws Exception {
        Field connectionPoolManagerField = CheckServlet.class.getDeclaredField("connectionPoolManager");
        connectionPoolManagerField.setAccessible(true);
        connectionPoolManagerField.set(servlet, connectionPoolManager);
        connectionPoolManagerField.setAccessible(false);

        servlet.destroy();

        verify(connectionPoolManager, times(1)).closeAllConnections();
    }
}