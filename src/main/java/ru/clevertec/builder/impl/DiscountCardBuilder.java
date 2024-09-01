package ru.clevertec.builder.impl;

import ru.clevertec.builder.Builder;
import ru.clevertec.models.DiscountCard;

public class DiscountCardBuilder implements Builder<DiscountCard> {
    private long id;
    private int number;
    private short discountPercentage;

    public DiscountCardBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public DiscountCardBuilder setNumber(int number) {
        this.number = number;
        return this;
    }

    public DiscountCardBuilder setDiscountPercentage(short amount) {
        this.discountPercentage = amount;
        return this;
    }

    @Override
    public DiscountCard build() {
        return new DiscountCard(id, number, discountPercentage);
    }
}
