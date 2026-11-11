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


@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private OrderDAO orderDAO;


    @Override
    public void init() {

        orderDAO =
                new OrderDAO();
    }


    // =====================================
    // SHOW ALL ORDERS
    // =====================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            HttpSession session =
                    request.getSession(false);


            // =============================
            // FLASH MESSAGE
            // =============================

            if (session != null) {

                String message =
                        (String)
                        session.getAttribute(
                                "orderMessage"
                        );

                String error =
                        (String)
                        session.getAttribute(
                                "orderError"
                        );


                if (message != null) {

                    request.setAttribute(
                            "message",
                            message
                    );

                    session.removeAttribute(
                            "orderMessage"
                    );
                }


                if (error != null) {

                    request.setAttribute(
                            "error",
                            error
                    );

                    session.removeAttribute(
                            "orderError"
                    );
                }
            }


            // =============================
            // LOAD ALL ORDERS
            // =============================

            List<Order> orders =
                    orderDAO.getAll();

            String statusFilter = request.getParameter("status");
            if (statusFilter != null && List.of("Pending", "Processing", "Shipped", "Delivered", "Cancelled").contains(statusFilter)) {
                orders = orders.stream()
                        .filter(order -> statusFilter.equals(order.getStatus()))
                        .toList();
                request.setAttribute("statusFilter", statusFilter);
            }


            request.setAttribute(
                    "orders",
                    orders
            );


            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/order.jsp"
                    )
                    .forward(
                            request,
                            response
                    );


        } catch (Exception e) {

            throw new ServletException(
                    "Cannot load orders",
                    e
            );
        }
    }


    // =====================================
    // UPDATE STATUS
    // =====================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );


        HttpSession session =
                request.getSession();


        String action =
                request.getParameter(
                        "action"
                );


        if (!"updateStatus".equals(
                action)) {

            setError(
                    session,
                    "Thao tác không hợp lệ."
            );

            redirect(
                    request,
                    response
            );

            return;
        }


        String orderId =
                request.getParameter(
                        "orderId"
                );


        String newStatus =
                request.getParameter(
                        "status"
                );


        try {

            // =============================
            // LOAD CURRENT ORDER
            // =============================

            Order order =
                    orderDAO.getById(
                            orderId
                    );


            if (order == null) {

                setError(
                        session,
                        "Không tìm thấy đơn hàng."
                );

                redirect(
                        request,
                        response
                );

                return;
            }


            String currentStatus =
                    order.getStatus();


            // =============================
            // SAME STATUS
            // =============================

            if (currentStatus != null
                    && currentStatus.equals(
                            newStatus
                    )) {

                setMessage(
                        session,
                        "Trạng thái đơn hàng chưa thay đổi."
                );

                redirect(
                        request,
                        response
                );

                return;
            }


            // =============================
            // VALIDATE TRANSITION
            // =============================

            if (!canTransition(
                    currentStatus,
                    newStatus
            )) {

                setError(
                        session,
                        "Không thể chuyển trạng thái đơn hàng từ "
                        + statusLabel(currentStatus)
                        + " → "
                        + statusLabel(newStatus)
                );

                redirect(
                        request,
                        response
                );

                return;
            }


            boolean success;


            // =============================
            // CANCEL
            // =============================

            if ("Cancelled".equals(
                    newStatus)) {

                success =
                        orderDAO.cancelOrder(
                                orderId
                        );

            } else {

                // =========================
                // NORMAL STATUS UPDATE
                // =========================

                success =
                        orderDAO
                                .updateStatusIfCurrent(
                                        orderId,
                                        currentStatus,
                                        newStatus
                                );
            }


            if (success) {

                setMessage(
                        session,
                        "Đã cập nhật trạng thái đơn hàng: "
                        + statusLabel(currentStatus)
                        + " → "
                        + statusLabel(newStatus)
                );

            } else {

                setError(
                        session,
                        "Không thể cập nhật đơn hàng. Trạng thái đơn có thể đã thay đổi."
                );
            }


            redirect(
                    request,
                    response
            );


        } catch (Exception e) {

            setError(
                    session,
                    "Cập nhật đơn hàng thất bại: "
                    + safeMessage(e)
            );


            redirect(
                    request,
                    response
            );
        }
    }


    // =====================================
    // STATUS TRANSITION
    // =====================================
    private boolean canTransition(
            String currentStatus,
            String newStatus) {


        if (currentStatus == null
                || newStatus == null) {

            return false;
        }


        // Pending
        if ("Pending".equals(
                currentStatus)) {

            return "Processing".equals(
                        newStatus
                    )
                    || "Cancelled".equals(
                        newStatus
                    );
        }


        // Processing
        if ("Processing".equals(
                currentStatus)) {

            return "Shipped".equals(
                        newStatus
                    )
                    || "Cancelled".equals(
                        newStatus
                    );
        }


        // Shipped
        if ("Shipped".equals(
                currentStatus)) {

            return "Delivered".equals(
                    newStatus
            );
        }


        // Delivered = final
        // Cancelled = final

        return false;
    }


    private String statusLabel(String status) {
        if ("Pending".equals(status)) return "Chờ xác nhận";
        if ("Processing".equals(status)) return "Đang xử lý";
        if ("Shipped".equals(status)) return "Đang giao";
        if ("Delivered".equals(status)) return "Đã giao";
        if ("Cancelled".equals(status)) return "Đã hủy";
        return "Không xác định";
    }


    // =====================================
    // FLASH MESSAGE
    // =====================================
    private void setMessage(
            HttpSession session,
            String message) {

        session.setAttribute(
                "orderMessage",
                message
        );
    }


    private void setError(
            HttpSession session,
            String message) {

        session.setAttribute(
                "orderError",
                message
        );
    }


    // =====================================
    // REDIRECT
    // =====================================
    private void redirect(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/orders"
        );
    }


    // =====================================
    // SAFE ERROR MESSAGE
    // =====================================
    private String safeMessage(
            Exception e) {

        if (e.getMessage() == null
                || e.getMessage()
                    .trim()
                    .isEmpty()) {

            return "Đã xảy ra lỗi không xác định.";
        }

        return e.getMessage();
    }
}
