package com.market.servlet;

import com.market.dao.CategoryDAO;
import com.market.dao.ProductDAO;
import com.market.model.Category;
import com.market.model.Product;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;

    @Override
    public void init() {
        categoryDAO = new CategoryDAO();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<Category> categories = categoryDAO.getAll();
            List<Product> products = productDAO.getActiveProducts();
            Map<String, String> categoryNames = new HashMap<>();
            Map<String, Integer> categoryCounts = new HashMap<>();

            for (Category category : categories) {
                categoryNames.put(category.getId(), category.getName());
            }
            for (Product product : products) {
                product.setCategoryName(categoryNames.getOrDefault(product.getCategoryId(), "Không rõ danh mục"));
                categoryCounts.merge(product.getCategoryId(), 1, Integer::sum);
            }

            request.setAttribute("categories", categories);
            request.setAttribute("products", products);
            request.setAttribute("categoryCounts", categoryCounts);
        } catch (Exception e) {
            throw new ServletException("Cannot load home", e);
        }

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/home.jsp"
                )
                .forward(
                        request,
                        response
                );
    }
}
