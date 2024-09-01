import org.junit.jupiter.api.Test;
import ru.clevertec.models.Product;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testProductConstructorAndGetters() {
        Product product = new Product(
                1L,
                "Test Product",
                99.99,
                100,
                true);

        assertEquals(1L, product.getId());
        assertEquals("Test Product", product.getDescription());
        assertEquals(99.99, product.getPrice());
        assertEquals(100, product.getQuantityInStock());
        assertTrue(product.isWholesaleProduct());
    }

    @Test
    void testSettersAndGetters() {
        Product product = new Product();

        product.setId(2L);
        product.setDescription("Another Product");
        product.setPrice(49.99);
        product.setQuantityInStock(50);
        product.setWholesaleProduct(false);

        assertEquals(2L, product.getId());
        assertEquals("Another Product", product.getDescription());
        assertEquals(49.99, product.getPrice());
        assertEquals(50, product.getQuantityInStock());
        assertFalse(product.isWholesaleProduct());
    }

    @Test
    void testToString() {
        Product product = new Product(
                3L,
                "Sample Product",
                19.99,
                10,
                false);

        String expectedString = "Product{" +
                "id=3, " +
                "description='Sample Product', " +
                "price=19.99, " +
                "quantityInStock=10, " +
                "wholesaleProduct=false}";
        assertEquals(expectedString, product.toString());
    }

    @Test
    void testEqualityAndHashCode() {
        Product product1 = new Product(
                4L,
                "Equal Product",
                29.99, 20,
                true);
        Product product2 = new Product(
                4L,
                "Equal Product",
                29.99,
                20,
                true);

        assertEquals(product1, product2);
        assertEquals(product1.hashCode(), product2.hashCode());
    }

    @Test
    void testInequality() {
        Product product1 = new Product(
                5L,
                "Product 1",
                39.99,
                30,
                false);
        Product product2 = new Product(
                6L,
                "Product 2",
                49.99,
                40,
                true);

        assertNotEquals(product1, product2);
    }
}