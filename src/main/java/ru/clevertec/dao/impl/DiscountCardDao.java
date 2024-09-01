package ru.clevertec.dao.impl;

import ru.clevertec.builder.impl.DiscountCardBuilder;
import ru.clevertec.dao.Dao;
import ru.clevertec.models.DiscountCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscountCardDao implements Dao<DiscountCard> {
    private final Connection connection;

    public DiscountCardDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public DiscountCard getById(long id) {
        String query = "SELECT * FROM public.discount_card WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDiscountCard(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching discount card by id", e);
        }
        return null;
    }

    public DiscountCard getByNumber(int number) {
        String query = "SELECT * FROM public.discount_card WHERE number = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, number);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDiscountCard(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching discount card by number", e);
        }
        return null;
    }

    @Override
    public List<DiscountCard> getAll() {
        List<DiscountCard> discountCards = new ArrayList<>();
        String query = "SELECT * FROM public.discount_card";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                discountCards.add(mapResultSetToDiscountCard(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all discount cards", e);
        }
        return discountCards;
    }

    @Override
    public void save(DiscountCard discountCard) {
        String query = "INSERT INTO public.discount_card (number, amount) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, discountCard.getNumber());
            statement.setShort(2, discountCard.getDiscountPercentage());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving discount card", e);
        }
    }

    @Override
    public void update(DiscountCard discountCard) {
        String query = "UPDATE public.discount_card SET number = ?, amount = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, discountCard.getNumber());
            statement.setShort(2, discountCard.getDiscountPercentage());
            statement.setLong(3, discountCard.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating discount card", e);
        }
    }

    @Override
    public void delete(long id) {
        String query = "DELETE FROM public.discount_card WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting discount card", e);
        }
    }

    @Override
    public void clear() {
        String query = "DELETE FROM public.discount_card";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting discount card", e);
        }
    }

    private DiscountCard mapResultSetToDiscountCard(ResultSet resultSet) throws SQLException {
        return new DiscountCardBuilder()
                .setId(resultSet.getLong("id"))
                .setNumber(resultSet.getInt("number"))
                .setDiscountPercentage(resultSet.getShort("amount"))
                .build();
    }
}