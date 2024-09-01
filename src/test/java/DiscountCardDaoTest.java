import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.clevertec.dao.impl.DiscountCardDao;
import ru.clevertec.models.DiscountCard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DiscountCardDaoTest {

    private DiscountCardDao discountCardDao;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        discountCardDao = spy(new DiscountCardDao(connection));

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(preparedStatement.executeUpdate()).thenReturn(1);
    }

    @Test
    void testGetById() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getInt("number")).thenReturn(123456);
        when(resultSet.getShort("amount")).thenReturn((short) 10);

        DiscountCard card = discountCardDao.getById(1L);

        verify(connection).prepareStatement("SELECT * FROM public.discount_card WHERE id = ?");
        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();

        assertNotNull(card);
        assertEquals(1L, card.getId());
        assertEquals(123456, card.getNumber());
        assertEquals(10, card.getDiscountPercentage());
    }

    @Test
    void testSave() throws SQLException {
        DiscountCard card = new DiscountCard(null, 123456, (short) 10);

        discountCardDao.save(card);

        ArgumentCaptor<Integer> numberCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Short> discountCaptor = ArgumentCaptor.forClass(Short.class);

        verify(preparedStatement).setInt(eq(1), numberCaptor.capture());
        verify(preparedStatement).setShort(eq(2), discountCaptor.capture());
        verify(preparedStatement).executeUpdate();

        assertEquals(123456, numberCaptor.getValue());
        assertEquals(10, discountCaptor.getValue().intValue());
    }

    @Test
    void testUpdate() throws SQLException {
        DiscountCard card = new DiscountCard(1L, 123456, (short) 15);

        discountCardDao.update(card);

        verify(connection).prepareStatement("UPDATE public.discount_card SET number = ?, amount = ? WHERE id = ?");
        verify(preparedStatement).setInt(1, 123456);
        verify(preparedStatement).setShort(2, (short) 15);
        verify(preparedStatement).setLong(3, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testDelete() throws SQLException {
        discountCardDao.delete(1L);

        verify(connection).prepareStatement("DELETE FROM public.discount_card WHERE id = ?");
        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testGetAll() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getInt("number")).thenReturn(123456, 654321);
        when(resultSet.getShort("amount")).thenReturn((short) 10, (short) 20);

        List<DiscountCard> cards = discountCardDao.getAll();

        verify(connection).prepareStatement("SELECT * FROM public.discount_card");
        verify(preparedStatement).executeQuery();
        verify(resultSet, times(3)).next();

        assertEquals(2, cards.size());
        assertEquals(1L, cards.get(0).getId());
        assertEquals(123456, cards.get(0).getNumber());
        assertEquals(10, cards.get(0).getDiscountPercentage());

        assertEquals(2L, cards.get(1).getId());
        assertEquals(654321, cards.get(1).getNumber());
        assertEquals(20, cards.get(1).getDiscountPercentage());
    }

    @Test
    void testClear() throws SQLException {
        discountCardDao.clear();

        verify(connection).prepareStatement("DELETE FROM public.discount_card");
        verify(preparedStatement).executeUpdate();
    }
}
