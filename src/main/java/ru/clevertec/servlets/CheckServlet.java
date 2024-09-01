package ru.clevertec.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.clevertec.config.DataSourceConfig;
import ru.clevertec.dao.ConnectionPoolManager;
import ru.clevertec.dao.impl.DiscountCardDao;
import ru.clevertec.dao.impl.ProductDao;
import ru.clevertec.exceptions.BadRequestException;
import ru.clevertec.exceptions.NotEnoughMoneyException;
import ru.clevertec.services.CheckService;
import ru.clevertec.utils.JsonUtil;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;

@WebServlet("/check")
public class CheckServlet extends HttpServlet {
    private ConnectionPoolManager connectionPoolManager;
    private ProductDao productDao;
    private DiscountCardDao discountCardDao;
    private DataSourceConfig dataSourceConfig = new DataSourceConfig();

    @Override
    public void init() throws ServletException {
        try {
            DataSource dataSource = dataSourceConfig.getDataSource();
            connectionPoolManager = new ConnectionPoolManager(dataSource, 10);
            productDao = new ProductDao(connectionPoolManager.getConnection());
            discountCardDao = new DiscountCardDao(connectionPoolManager.getConnection());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)  {
        try {
            CheckRequest checkRequest = JsonUtil.fromJson(req.getReader(), CheckRequest.class);
            CheckService checkService = new CheckService(
                    productDao.getAll(),
                    discountCardDao.getByNumber(checkRequest.getDiscountCard()),
                    checkRequest.getBalanceDebitCard()
            );

            String csvCheck = checkService.generateCheck(checkRequest.getProducts(), productDao);

            resp.setContentType("text/csv");
            resp.setHeader("Content-Disposition", "attachment; filename=\"check.csv\"");
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(csvCheck);
            }
        } catch (BadRequestException | NotEnoughMoneyException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void destroy() {
        connectionPoolManager.closeAllConnections();
    }

    public static class CheckRequest {
        private List<ProductQuantity> products;
        private int discountCard;
        private double balanceDebitCard;

        public List<ProductQuantity> getProducts() {
            return products;
        }

        public void setProducts(List<ProductQuantity> products) {
            this.products = products;
        }

        public int getDiscountCard() {
            return discountCard;
        }

        public void setDiscountCard(int discountCard) {
            this.discountCard = discountCard;
        }

        public double getBalanceDebitCard() {
            return balanceDebitCard;
        }

        public void setBalanceDebitCard(double balanceDebitCard) {
            this.balanceDebitCard = balanceDebitCard;
        }

        public static class ProductQuantity {
            private int id;
            private int quantity;

            public ProductQuantity(int id, int quantity) {
                this.id = id;
                this.quantity = quantity;
            }

            public int getId() {
                return id;
            }

            public int getQuantity() {
                return quantity;
            }
        }
    }
}

