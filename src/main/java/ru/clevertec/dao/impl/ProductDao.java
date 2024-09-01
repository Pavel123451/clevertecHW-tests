package ru.clevertec.dao.impl;


import ru.clevertec.builder.impl.ProductBuilder;
import ru.clevertec.dao.Dao;
import ru.clevertec.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao implements Dao<Product> {
    private final Connection connection;

    public ProductDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Product getById(long id) {
        String query = "SELECT * FROM public.product WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToProduct(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching product by id", e);
        }
        return null;
    }

    @Override
    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM public.product";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(mapResultSetToProduct(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all products", e);
        }
        return products;
    }

    @Override
    public void save(Product product) {
        String query = "INSERT INTO public.product (description, price, " +
                "quantity_in_stock, wholesale_product) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, product.getDescription());
            statement.setDouble(2, product.getPrice());
            statement.setInt(3, product.getQuantityInStock());
            statement.setBoolean(4, product.isWholesaleProduct());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product", e);
        }
    }

    @Override
    public void update(Product product) {
        String query = "UPDATE public.product SET description = ?, price = ?, " +
                "quantity_in_stock = ?, wholesale_product = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, product.getDescription());
            statement.setDouble(2, product.getPrice());
            statement.setInt(3, product.getQuantityInStock());
            statement.setBoolean(4, product.isWholesaleProduct());
            statement.setLong(5, product.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        }
    }

    @Override
    public void delete(long id) {
        String query = "DELETE FROM public.product WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }

    @Override
    public void clear() {
        String query = "DELETE FROM public.product";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }

    public void updateProductQuantity(Long productId, int newQuantity) throws SQLException {
        String query = "UPDATE public.product SET quantity_in_stock = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newQuantity);
            stmt.setLong(2, productId);
            stmt.executeUpdate();
        }
    }

    private Product mapResultSetToProduct(ResultSet resultSet) throws SQLException {
        return new ProductBuilder()
                .setId(resultSet.getLong("id"))
                .setDescription(resultSet.getString("description"))
                .setPrice(resultSet.getDouble("price"))
                .setQuantityInStock(resultSet.getInt("quantity_in_stock"))
                .setWholesaleProduct(resultSet.getBoolean("wholesale_product"))
                .build();
    }
}
