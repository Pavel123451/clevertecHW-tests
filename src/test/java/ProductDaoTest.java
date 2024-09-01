import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.clevertec.dao.impl.ProductDao;
import ru.clevertec.models.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductDaoTest {

    private ProductDao productDao;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        productDao = spy(new ProductDao(connection));

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(preparedStatement.executeUpdate()).thenReturn(1);
    }

    @Test
    void testGetById() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("description")).thenReturn("Test Product");
        when(resultSet.getDouble("price")).thenReturn(100.0);
        when(resultSet.getInt("quantity_in_stock")).thenReturn(50);
        when(resultSet.getBoolean("wholesale_product")).thenReturn(false);

        Product product = productDao.getById(1L);

        verify(connection).prepareStatement("SELECT * FROM public.product WHERE id = ?");
        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();

        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertEquals("Test Product", product.getDescription());
        assertEquals(100.0, product.getPrice());
        assertEquals(50, product.getQuantityInStock());
        assertFalse(product.isWholesaleProduct());
    }

    @Test
    void testGetAll() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("description")).thenReturn("Product 1", "Product 2");
        when(resultSet.getDouble("price")).thenReturn(100.0, 200.0);
        when(resultSet.getInt("quantity_in_stock")).thenReturn(50, 100);
        when(resultSet.getBoolean("wholesale_product")).thenReturn(false, true);

        List<Product> products = productDao.getAll();

        verify(connection).prepareStatement("SELECT * FROM public.product");
        verify(preparedStatement).executeQuery();
        verify(resultSet, times(3)).next();

        assertEquals(2, products.size());

        Product product1 = products.get(0);
        assertEquals(1L, product1.getId());
        assertEquals("Product 1", product1.getDescription());
        assertEquals(100.0, product1.getPrice());
        assertEquals(50, product1.getQuantityInStock());
        assertFalse(product1.isWholesaleProduct());

        Product product2 = products.get(1);
        assertEquals(2L, product2.getId());
        assertEquals("Product 2", product2.getDescription());
        assertEquals(200.0, product2.getPrice());
        assertEquals(100, product2.getQuantityInStock());
        assertTrue(product2.isWholesaleProduct());
    }

    @Test
    void testSave() throws SQLException {
        Product product = new Product(null, "New Product", 150.0, 30, true);

        productDao.save(product);

        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> priceCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Integer> quantityCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> wholesaleCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(preparedStatement).setString(eq(1), descriptionCaptor.capture());
        verify(preparedStatement).setDouble(eq(2), priceCaptor.capture());
        verify(preparedStatement).setInt(eq(3), quantityCaptor.capture());
        verify(preparedStatement).setBoolean(eq(4), wholesaleCaptor.capture());
        verify(preparedStatement).executeUpdate();

        assertEquals("New Product", descriptionCaptor.getValue());
        assertEquals(150.0, priceCaptor.getValue());
        assertEquals(30, quantityCaptor.getValue());
        assertTrue(wholesaleCaptor.getValue());
    }

    @Test
    void testUpdate() throws SQLException {
        Product product = new Product(1L, "Updated Product", 200.0, 40, false);

        productDao.update(product);

        verify(connection).prepareStatement("UPDATE public.product SET " +
                "description = ?, " +
                "price = ?, " +
                "quantity_in_stock = ?, " +
                "wholesale_product = ? " +
                "WHERE id = ?");
        verify(preparedStatement).setString(1, "Updated Product");
        verify(preparedStatement).setDouble(2, 200.0);
        verify(preparedStatement).setInt(3, 40);
        verify(preparedStatement).setBoolean(4, false);
        verify(preparedStatement).setLong(5, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete() throws SQLException {
        productDao.delete(1L);

        verify(connection).prepareStatement("DELETE FROM public.product WHERE id = ?");
        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdateProductQuantity() throws SQLException {
        productDao.updateProductQuantity(1L, 100);

        verify(connection).prepareStatement("UPDATE public.product SET quantity_in_stock = ? WHERE id = ?");
        verify(preparedStatement).setInt(1, 100);
        verify(preparedStatement).setLong(2, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testClear() throws SQLException {
        productDao.clear();

        verify(connection).prepareStatement("DELETE FROM public.product");
        verify(preparedStatement).executeUpdate();
    }
}