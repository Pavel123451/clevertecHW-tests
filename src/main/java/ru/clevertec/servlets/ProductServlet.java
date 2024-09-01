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
import ru.clevertec.models.Product;
import ru.clevertec.utils.JsonUtil;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    private ProductDao productDao;
    private ConnectionPoolManager connectionPoolManager;
    private DataSourceConfig dataSourceConfig = new DataSourceConfig();

    @Override
    public void init() throws ServletException {
        try {
            DataSource dataSource = dataSourceConfig.getDataSource();
            connectionPoolManager = new ConnectionPoolManager(dataSource, 10);
            productDao = new ProductDao(connectionPoolManager.getConnection());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String idParam = req.getParameter("id");

        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("application/json");

            if (idParam == null) {
                List<Product> products = productDao.getAll();
                writer.write(JsonUtil.toJson(products));
            } else {
                int id = Integer.parseInt(idParam);
                Product product = productDao.getById(id);
                if (product != null) {
                    writer.write(JsonUtil.toJson(product));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("application/json");

            Product product = JsonUtil.fromJson(req.getReader(), Product.class);
            productDao.save(product);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writer.write(JsonUtil.toJson(product));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) {
        String idParam = req.getParameter("id");

        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try (PrintWriter writer = resp.getWriter()) {
            resp.setContentType("application/json");

            Long id = Long.parseLong(idParam);
            Product product = JsonUtil.fromJson(req.getReader(), Product.class);
            product.setId(id);
            productDao.update(product);
            writer.write(JsonUtil.toJson(product));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String idParam = req.getParameter("id");

        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            resp.setContentType("application/json");

            int id = Integer.parseInt(idParam);
            productDao.delete(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    public void destroy() {
        connectionPoolManager.closeAllConnections();
    }
}


