import org.junit.jupiter.api.Test;
import ru.clevertec.models.DiscountCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DiscountCardTest {

    @Test
    public void testGettersAndSetters() {
        DiscountCard card = new DiscountCard();
        card.setId(1L);
        card.setNumber(12345);
        card.setDiscountPercentage((short) 10);

        assertEquals(1L, card.getId());
        assertEquals(12345, card.getNumber());
        assertEquals(10, card.getDiscountPercentage());
    }

    @Test
    public void testConstructor() {
        DiscountCard card = new DiscountCard(1L, 12345, (short) 10);

        assertEquals(1L, card.getId());
        assertEquals(12345, card.getNumber());
        assertEquals(10, card.getDiscountPercentage());
    }

    @Test
    public void testEqualsAndHashCode() {
        DiscountCard card1 = new DiscountCard(1L, 12345, (short) 10);
        DiscountCard card2 = new DiscountCard(1L, 12345, (short) 10);
        DiscountCard card3 = new DiscountCard(2L, 54321, (short) 5);

        assertEquals(card1, card2);
        assertNotEquals(card1, card3);
        assertEquals(card1.hashCode(), card2.hashCode());
        assertNotEquals(card1.hashCode(), card3.hashCode());
    }

    @Test
    public void testToString() {
        DiscountCard card = new DiscountCard(1L, 12345, (short) 10);
        String expected = "DiscountCard{id=1, number='12345', discountPercentage=10}";
        assertEquals(expected, card.toString());
    }
}