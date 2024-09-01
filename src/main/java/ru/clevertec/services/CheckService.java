package ru.clevertec.services;


import ru.clevertec.dao.impl.ProductDao;
import ru.clevertec.exceptions.BadRequestException;
import ru.clevertec.exceptions.NotEnoughMoneyException;
import ru.clevertec.models.DiscountCard;
import ru.clevertec.models.Product;

import static ru.clevertec.servlets.CheckServlet.CheckRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CheckService {
    private final static int QTY_FOR_WHOLESALE = 5;
    private List<Product> products;
    private DiscountCard discountCard;
    private double balance;

    public CheckService(List<Product> products, DiscountCard discountCard, double balance) {
        this.products = products;
        this.discountCard = discountCard;
        this.balance = balance;
    }

    public String generateCheck(
            List<CheckRequest.ProductQuantity> productQuantities,
            ProductDao productDao)
            throws BadRequestException, NotEnoughMoneyException, SQLException {
        validateProducts(productQuantities);

        List<ProductInfo> productInfoList = calculateProductInfo(productQuantities);
        BigDecimal totalCost = calculateTotalCost(productInfoList);
        if (balance < totalCost.doubleValue()) {
            throw new NotEnoughMoneyException("Not enough money on the debit card");
        }

        StringBuilder csvContent = generateCheckContent(productInfoList, totalCost);

        updateProductQuantities(productQuantities, productDao);

        return csvContent.toString();
    }

    private void validateProducts(List<CheckRequest.ProductQuantity> productQuantities)
            throws BadRequestException {
        for (CheckRequest.ProductQuantity pq : productQuantities) {
            Product product = getProductById(pq.getId());
            if (pq.getQuantity() > product.getQuantityInStock()) {
                throw new BadRequestException("Not enough stock for product id: " + pq.getId());
            }
        }
    }

    private Product getProductById(int id) throws BadRequestException {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Product with id " + id + " not found"));
    }

    private List<ProductInfo> calculateProductInfo(
            List<CheckRequest.ProductQuantity> productQuantities
    ) throws BadRequestException {
        List<ProductInfo> productInfoList = new ArrayList<>();
        for (CheckRequest.ProductQuantity pq : productQuantities) {
            Product product = getProductById(pq.getId());
            int quantity = pq.getQuantity();

            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal productTotalCost = productPrice.multiply(BigDecimal.valueOf(quantity));
            StringBuilder discountDetails = new StringBuilder();
            BigDecimal discount = calculateDiscount(product, quantity, discountDetails);

            BigDecimal costAfterDiscount = productTotalCost.subtract(discount);

            productInfoList.add(new ProductInfo(
                    product,
                    quantity,
                    productPrice,
                    productTotalCost,
                    discount,
                    costAfterDiscount
            ));
        }
        return productInfoList;
    }

    private BigDecimal calculateTotalCost(List<ProductInfo> productInfoList) {
        return productInfoList.stream()
                .map(ProductInfo::costAfterDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private StringBuilder generateCheckContent(List<ProductInfo> productInfoList, BigDecimal totalCost) {
        StringBuilder csvContent = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();

        csvContent.append("Date;Time\n")
                .append(dateFormat.format(now)).append(";")
                .append(timeFormat.format(now)).append("\n\n")
                .append("QTY;DESCRIPTION;PRICE;DISCOUNT;TOTAL\n");

        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalWithoutDiscount = BigDecimal.ZERO;

        for (ProductInfo productInfo : productInfoList) {
            csvContent.append(String.format("%d;%s;%.2f$;%.2f$;%.2f$\n",
                    productInfo.quantity(),
                    productInfo.product().getDescription(),
                    productInfo.productPrice(),
                    productInfo.discount(),
                    productInfo.productTotalCost()));

            totalDiscount = totalDiscount.add(productInfo.discount());
            totalWithoutDiscount = totalWithoutDiscount.add(productInfo.productTotalCost());
        }

        if (discountCard != null) {
            csvContent.append("\nDISCOUNT CARD;DISCOUNT PERCENTAGE\n")
                    .append(discountCard.getNumber()).append(";")
                    .append(discountCard.getDiscountPercentage()).append("%\n");
        }

        csvContent.append("\nTOTAL PRICE;TOTAL DISCOUNT;TOTAL WITH DISCOUNT\n")
                .append(String.format("%.2f$;%.2f$;%.2f$\n",
                        totalWithoutDiscount, totalDiscount, totalCost));

        return csvContent;
    }

    private void updateProductQuantities(
            List<CheckRequest.ProductQuantity> productQuantities,
            ProductDao productDao
    ) throws SQLException, BadRequestException {
        for (CheckRequest.ProductQuantity pq : productQuantities) {
            Product product = getProductById(pq.getId());
            product.setQuantityInStock(product.getQuantityInStock() - pq.getQuantity());
            productDao.updateProductQuantity(product.getId(), product.getQuantityInStock());
        }
    }

    private BigDecimal calculateDiscount(
            Product product,
            int quantity,
            StringBuilder discountDetails) {
        BigDecimal discount = BigDecimal.ZERO;

        if (product.isWholesaleProduct() && quantity >= QTY_FOR_WHOLESALE) {
            discount = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(quantity))
                    .multiply(BigDecimal.valueOf(0.1));
            discountDetails.append("Wholesale discount applied: ").append(discount).append("$");
        } else if (discountCard != null) {
            discount = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(quantity))
                    .multiply(BigDecimal.valueOf(discountCard.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            discountDetails.append("Discount card applied: ").append(discount).append("$");
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private record ProductInfo(Product product,
                               int quantity,
                               BigDecimal productPrice,
                               BigDecimal productTotalCost,
                               BigDecimal discount,
                               BigDecimal costAfterDiscount) {
    }
}