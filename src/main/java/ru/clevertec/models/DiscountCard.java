package ru.clevertec.models;

import java.util.Objects;

public class DiscountCard {
    private Long id;
    private int number;
    private short discountPercentage;

    public DiscountCard() {
    }

    public DiscountCard(Long id, int number, short discountPercentage) {
        this.id = id;
        this.number = number;
        this.discountPercentage = discountPercentage;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setDiscountPercentage(short discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Long getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public short getDiscountPercentage() {
        return discountPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscountCard that = (DiscountCard) o;
        return number == that.number
                && discountPercentage == that.discountPercentage
                && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, discountPercentage);
    }

    @Override
    public String toString() {
        return "DiscountCard{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", discountPercentage=" + discountPercentage +
                '}';
    }
}
