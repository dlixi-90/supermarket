package com.market.servlet;

import com.market.dao.DashboardDAO;
import com.market.dao.OrderDAO;

import com.market.model.Order;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private DashboardDAO dashboardDAO;
    private OrderDAO orderDAO;


    @Override
    public void init() {

        dashboardDAO =
                new DashboardDAO();

        orderDAO =
                new OrderDAO();
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            // =================================
            // USERS
            // =================================

            long totalCustomers =
                    dashboardDAO
                            .countCustomers();


            // =================================
            // PRODUCTS
            // =================================

            long totalProducts =
                    dashboardDAO
                            .countProducts();


            long activeProducts =
                    dashboardDAO
                            .countActiveProducts();


            // =================================
            // ORDERS
            // =================================

            long totalOrders =
                    dashboardDAO
                            .countOrders();


            long pendingOrders =
                    dashboardDAO
                            .countOrdersByStatus(
                                    "Pending"
                            );


            long processingOrders =
                    dashboardDAO
                            .countOrdersByStatus(
                                    "Processing"
                            );


            long shippedOrders =
                    dashboardDAO
                            .countOrdersByStatus(
                                    "Shipped"
                            );


            long deliveredOrders =
                    dashboardDAO
                            .countOrdersByStatus(
                                    "Delivered"
                            );


            long cancelledOrders =
                    dashboardDAO
                            .countOrdersByStatus(
                                    "Cancelled"
                            );


            // =================================
            // REVENUE
            // =================================

            long deliveredRevenue =
                    dashboardDAO
                            .getDeliveredRevenue();


            // =================================
            // LOW STOCK
            //
            // <= 5 được xem là low stock
            // =================================

            long lowStockProducts =
                    dashboardDAO
                            .countLowStockProducts(
                                    5
                            );


            // =================================
            // RECENT ORDERS
            // =================================

            List<Order> recentOrders =
                    orderDAO
                            .getRecentOrders(
                                    5
                            );


            // =================================
            // REQUEST ATTRIBUTES
            // =================================

            request.setAttribute(
                    "totalCustomers",
                    totalCustomers
            );


            request.setAttribute(
                    "totalProducts",
                    totalProducts
            );


            request.setAttribute(
                    "activeProducts",
                    activeProducts
            );


            request.setAttribute(
                    "totalOrders",
                    totalOrders
            );


            request.setAttribute(
                    "pendingOrders",
                    pendingOrders
            );


            request.setAttribute(
                    "processingOrders",
                    processingOrders
            );


            request.setAttribute(
                    "shippedOrders",
                    shippedOrders
            );


            request.setAttribute(
                    "deliveredOrders",
                    deliveredOrders
            );


            request.setAttribute(
                    "cancelledOrders",
                    cancelledOrders
            );


            request.setAttribute(
                    "deliveredRevenue",
                    deliveredRevenue
            );


            request.setAttribute(
                    "lowStockProducts",
                    lowStockProducts
            );


            request.setAttribute(
                    "recentOrders",
                    recentOrders
            );


            // =================================
            // VIEW
            // =================================

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/dashboard.jsp"
                    )
                    .forward(
                            request,
                            response
                    );


        } catch (Exception e) {

            throw new ServletException(
                    "Cannot load dashboard",
                    e
            );
        }
    }
}