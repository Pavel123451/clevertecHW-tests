package ru.clevertec.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.clevertec.config.DataSourceConfig;
import ru.clevertec.dao.ConnectionPoolManager;
import ru.clevertec.dao.impl.DiscountCardDao;
import ru.clevertec.models.DiscountCard;
import ru.clevertec.utils.JsonUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/discountcards")
public class DiscountCardServlet extends HttpServlet {
    private DiscountCardDao discountCardDao;
    private ConnectionPoolManager connectionPoolManager;
    private DataSourceConfig dataSourceConfig = new DataSourceConfig();

    @Override
    public void init() throws ServletException {
        try {

            DataSource dataSource = dataSourceConfig.getDataSource();
            connectionPoolManager = new ConnectionPoolManager(dataSource, 10);
            discountCardDao = new DiscountCardDao(connectionPoolManager.getConnection());
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
                List<DiscountCard> discountCards = discountCardDao.getAll();
                writer.write(JsonUtil.toJson(discountCards));
            } else {
                int id = Integer.parseInt(idParam);
                DiscountCard discountCard = discountCardDao.getById(id);
                if (discountCard != null) {
                    writer.write(JsonUtil.toJson(discountCard));
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

            DiscountCard discountCard = JsonUtil.fromJson(req.getReader(), DiscountCard.class);
            discountCardDao.save(discountCard);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writer.write(JsonUtil.toJson(discountCard));
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
            DiscountCard discountCard = JsonUtil.fromJson(req.getReader(), DiscountCard.class);
            discountCard.setId(id);
            discountCardDao.update(discountCard);
            writer.write(JsonUtil.toJson(discountCard));
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
            discountCardDao.delete(id);
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


