package com.market.servlet;

import com.market.dao.OrderDAO;
import com.market.model.Order;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/my-orders")
public class MyOrdersServlet extends HttpServlet {

    private OrderDAO orderDAO;


    @Override
    public void init() {

        orderDAO =
                new OrderDAO();
    }


    // =====================================
    // SHOW CUSTOMER ORDERS
    // =====================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        HttpSession session =
                request.getSession(false);


        // =================================
        // DEFENSIVE LOGIN CHECK
        //
        // AuthFilter đã bảo vệ route này,
        // nhưng vẫn kiểm tra thêm.
        // =================================

        if (session == null
                || session.getAttribute(
                        "userId"
                ) == null) {

            response.sendRedirect(
                    request.getContextPath()
                            + "/login"
            );

            return;
        }


        String userId =
                (String)
                session.getAttribute(
                        "userId"
                );


        try {

            // =================================
            // GET ORDERS OF CURRENT USER
            // =================================

            List<Order> orders =
                    orderDAO.getByUserId(
                            userId
                    );


            request.setAttribute(
                    "orders",
                    orders
            );


            // =================================
            // SUCCESS MESSAGE
            // =================================

            boolean placed =
                    "1".equals(
                            request.getParameter(
                                    "placed"
                            )
                    );


            if (placed) {

                request.setAttribute(
                        "success",
                        "Đặt hàng thành công."
                );
            }


            // =================================
            // VIEW
            // =================================

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/my-orders.jsp"
                    )
                    .forward(
                            request,
                            response
                    );


        } catch (Exception e) {

            throw new ServletException(
                    "Cannot load your orders",
                    e
            );
        }
    }
}
