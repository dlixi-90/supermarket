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


@WebServlet("/shop")
public class ShopServlet extends HttpServlet {

    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;


    @Override
    public void init() {

        productDAO =
                new ProductDAO();

        categoryDAO =
                new CategoryDAO();
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            // =================================
            // LOAD CATEGORIES
            // =================================

            List<Category> categories =
                    categoryDAO.getAll();


            Map<String, String>
                    categoryMap =
                    new HashMap<>();


            for (Category category :
                    categories) {

                categoryMap.put(
                        category.getId(),
                        category.getName()
                );
            }


            // =================================
            // LOAD ACTIVE PRODUCTS
            // =================================

            List<Product> products =
                    productDAO
                            .getActiveProducts();


            // =================================
            // CATEGORY NAME FOR VIEW
            // =================================

            for (Product product :
                    products) {

                String categoryName =
                        categoryMap.get(
                                product.getCategoryId()
                        );


                if (categoryName == null) {

                    categoryName =
                            "Không rõ danh mục";
                }


                product.setCategoryName(
                        categoryName
                );
            }


            request.setAttribute(
                    "products",
                    products
            );

            request.setAttribute(
                    "categories",
                    categories
            );


            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/shop.jsp"
                    )
                    .forward(
                            request,
                            response
                    );


        } catch (Exception e) {

            throw new ServletException(
                    "Cannot load shop",
                    e
            );
        }
    }
}
