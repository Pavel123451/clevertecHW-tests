package ru.clevertec.builder.impl;

import ru.clevertec.builder.Builder;
import ru.clevertec.models.Product;

public class ProductBuilder implements Builder<Product> {
    private long id;
    private String description;
    private double price;
    private int quantityInStock;
    private boolean wholesaleProduct;

    public ProductBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public ProductBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ProductBuilder setPrice(double price) {
        this.price = price;
        return this;
    }

    public ProductBuilder setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
        return this;
    }

    public ProductBuilder setWholesaleProduct(boolean wholesaleProduct) {
        this.wholesaleProduct = wholesaleProduct;
        return this;
    }

    @Override
    public Product build() {
        return new Product(id, description, price, quantityInStock, wholesaleProduct);
    }
}