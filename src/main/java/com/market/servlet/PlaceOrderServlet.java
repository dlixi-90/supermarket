package com.market.servlet;

import com.market.dao.OrderDAO;

import com.market.model.CartItem;
import com.market.model.CheckoutData;
import com.market.util.CartState;

import java.io.IOException;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/place-order")
public class PlaceOrderServlet extends HttpServlet {

    private OrderDAO orderDAO;

    @Override
    public void init() {

        orderDAO
                = new OrderDAO();
    }

    // =====================================
    // PLACE ORDER
    // =====================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        // =================================
        // SESSION CHECK
        // AuthFilter cũng kiểm tra,
        // nhưng vẫn nên defensive check.
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

        String userId
                = (String) session.getAttribute(
                        "userId"
                );

        CheckoutData checkoutData
                = (CheckoutData) session.getAttribute(
                        "checkoutData"
                );

        Map<String, CartItem> cart
                = getCart(
                        session
                );

        // =================================
        // CHECKOUT DATA
        // =================================
        if (checkoutData == null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/checkout"
            );

            return;
        }

        // =================================
        // CART
        // =================================
        if (cart == null
                || cart.isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/cart"
            );

            return;
        }

        try {

            String token = request.getParameter("checkoutToken");
            if (token == null || !token.equals(session.getAttribute("checkoutToken"))
                    || !CartState.signature(cart).equals(session.getAttribute("checkoutCartSignature"))) {
                session.setAttribute("checkoutError", "Giỏ hàng hoặc thông tin xác nhận đã thay đổi. Vui lòng xác nhận lại đơn hàng.");
                response.sendRedirect(request.getContextPath() + "/checkout");
                return;
            }

            // =================================
            // PLACE ORDER
            // =================================
            String orderId
                    = orderDAO.placeOrder(
                            userId,
                            checkoutData,
                            cart
                    );

            // =================================
            // SUCCESS
            //
            // Chỉ clear cart SAU KHI
            // transaction commit.
            // =================================
            session.removeAttribute(
                    "cart"
            );

            session.removeAttribute(
                    "checkoutData"
            );
            CartState.invalidateReview(session);


            /*
             * Dùng sau cho confirmation
             * hoặc My Orders.
             */
            session.setAttribute(
                    "lastOrderId",
                    orderId
            );

            // =================================
            // REDIRECT
            // =================================
            response.sendRedirect(
                    request.getContextPath()
                    + "/my-orders"
                    + "?placed=1"
            );

        } catch (IllegalArgumentException
                | IllegalStateException e) {
            CartState.invalidateReview(session);


            /*
             * Flash error.
             *
             * CheckoutServlet sẽ đọc
             * và xóa message này.
             */
            session.setAttribute(
                    "checkoutError",
                    e.getMessage()
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/checkout"
            );

        } catch (Exception e) {

            throw new ServletException(
                    "Không thể đặt hàng",
                    e
            );
        }
    }

    // =====================================
    // GET CART
    // =====================================
    @SuppressWarnings("unchecked")
    private Map<String, CartItem> getCart(
            HttpSession session) {

        if (session == null) {
            return null;
        }

        return (Map<String, CartItem>) session.getAttribute(
                "cart"
        );
    }

    // =====================================
    // Không cho đặt order bằng GET
    // =====================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
                request.getContextPath()
                + "/checkout"
        );
    }
}
