<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%@ page import="com.market.model.Order" %>
<%@ page import="com.market.model.OrderItem" %>
<%@ page import="com.market.util.ViewUtil" %>

<%!

    private String escapeHtml(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String statusClass(
            String status) {

        if (status == null) {
            return "status-default";
        }

        if ("Pending".equalsIgnoreCase(status)) {
            return "status-pending";
        }

        if ("Processing".equalsIgnoreCase(status)) {
            return "status-processing";
        }

        if ("Shipped".equalsIgnoreCase(status)) {
            return "status-shipped";
        }

        if ("Delivered".equalsIgnoreCase(status)) {
            return "status-delivered";
        }

        if ("Cancelled".equalsIgnoreCase(status)) {
            return "status-cancelled";
        }

        return "status-default";
    }

    private String statusLabel(String status) {
        if ("Pending".equalsIgnoreCase(status)) return "Chờ xác nhận";
        if ("Processing".equalsIgnoreCase(status)) return "Đang xử lý";
        if ("Shipped".equalsIgnoreCase(status)) return "Đang giao";
        if ("Delivered".equalsIgnoreCase(status)) return "Đã giao";
        if ("Cancelled".equalsIgnoreCase(status)) return "Đã hủy";
        return "Không xác định";
    }

    private String paymentLabel(String method) {
        if ("COD".equalsIgnoreCase(method)) return "Thanh toán khi nhận hàng (COD)";
        if ("QR".equalsIgnoreCase(method)) return "Thanh toán bằng mã QR";
        return method == null ? "" : method;
    }

%>

<%

    List<Order> orders =
            (List<Order>)
            request.getAttribute(
                    "orders"
            );

    String success =
            (String)
            request.getAttribute(
                    "success"
            );

    String userName =
            (String)
            session.getAttribute(
                    "userName"
            );

    SimpleDateFormat dateFormat =
            new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm"
            );

%>

<!DOCTYPE html>

<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Đơn hàng của tôi</title>

<jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>

<body class="msm-page msm-my-orders">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content">

<!-- =====================================
     HEADER
===================================== -->

<!-- =====================================
     PAGE
===================================== -->

<main class="container">

    <h1>
        Đơn hàng của tôi
    </h1>

    <!-- SUCCESS -->

    <% if (success != null) { %>

        <div class="success">

            <%= escapeHtml(success) %>

        </div>

    <% } %>

    <!-- =================================
         EMPTY ORDERS
    ================================== -->

    <%

        if (orders == null
                || orders.isEmpty()) {

    %>

        <div class="empty">

            <h2>
                Bạn chưa có đơn hàng.
            </h2>

            <p>
                Khám phá các sản phẩm
                và đặt đơn hàng đầu tiên của bạn.
            </p>

            <a
                class="shop-button"

                href="<%=
                    request.getContextPath()
                %>/shop"
            >
                Đến cửa hàng
            </a>

        </div>

    <%

        } else {

            for (Order order :
                    orders) {

    %>

    <!-- =================================
         ORDER
    ================================== -->

    <section class="order-card">

        <!-- =============================
             HEADER
        ============================== -->

        <div class="order-header">

            <!-- ORDER ID -->

            <div>

                <div class="label">

                    Mã đơn hàng

                </div>

                <div class="value">

                    <%=
                        escapeHtml(
                            order.getId()
                        )
                    %>

                </div>

            </div>

            <!-- DATE -->

            <div>

                <div class="label">

                    Ngày đặt hàng

                </div>

                <div class="value">

                    <%=
                        order.getOrderDate()
                                != null

                        ? dateFormat.format(
                                order.getOrderDate()
                          )

                        : ""
                    %>

                </div>

            </div>

            <!-- PAYMENT -->

            <div>

                <div class="label">

                    Thanh toán

                </div>

                <div class="value">

                    <%=
                        escapeHtml(paymentLabel(order.getPaymentMethod()))
                    %>

                </div>

            </div>

            <!-- STATUS -->

            <div>

                <div class="label">

                    Trạng thái

                </div>

                <span
                    class="status <%=
                        statusClass(
                            order.getStatus()
                        )
                    %>"
                >

                    <%=
                        escapeHtml(statusLabel(order.getStatus()))
                    %>

                </span>

            </div>

        </div>

        <!-- =============================
             ORDER ITEMS
        ============================== -->

        <div class="order-body">

            <h3>
                Sản phẩm
            </h3>

            <%

                List<OrderItem> items =
                        order.getItems();

                if (items != null) {

                    for (
                        OrderItem item :
                        items
                    ) {

                        String imageUrl = ViewUtil.resolveImageUrl(
                                item.getImageUrl(),
                                request.getContextPath()
                        );

            %>

            <div class="item">

                <% if (!imageUrl.isEmpty()) { %>
                <img
                    class="order-product-image"
                    src="<%= escapeHtml(imageUrl) %>"
                    alt="<%= escapeHtml(item.getProductName()) %>"
                    loading="lazy"
                    decoding="async"
                >
                <% } else { %>
                <div class="order-product-image order-product-image-placeholder" aria-hidden="true">
                    <i class="ri-image-line"></i>
                </div>
                <% } %>

                <!-- NAME -->

                <div class="item-details">

                    <div class="product-name">

                        <%=
                            escapeHtml(
                                item.getProductName()
                            )
                        %>

                    </div>

                </div>

                <!-- PRICE -->

                <div class="money">

                    <%= ViewUtil.formatCurrency(item.getPrice()) %>

                </div>

                <!-- QUANTITY -->

                <div>

                    SL:

                    <%=
                        item.getQuantity()
                    %>

                </div>

                <!-- SUBTOTAL -->

                <div class="money">

                    <strong>

                        <%= ViewUtil.formatCurrency(item.getSubtotal()) %>

                    </strong>

                </div>

            </div>

            <%

                    }

                }

            %>

            <!-- =========================
                 SHIPPING
            ========================== -->

            <div class="shipping">

                <div class="shipping-box">

                    <h3>
                        Thông tin giao hàng
                    </h3>

                    <div class="shipping-row">

                        <div class="label">
                            Người nhận
                        </div>

                        <div>

                            <%=
                                escapeHtml(
                                    order
                                        .getRecipientName()
                                )
                            %>

                        </div>

                    </div>

                    <div class="shipping-row">

                        <div class="label">
                            Số điện thoại
                        </div>

                        <div>

                            <%=
                                escapeHtml(
                                    order.getPhone()
                                )
                            %>

                        </div>

                    </div>

                    <div class="shipping-row">

                        <div class="label">
                            Địa chỉ
                        </div>

                        <div>

                            <%=
                                escapeHtml(
                                    order
                                        .getShippingAddress()
                                )
                            %>

                        </div>

                    </div>

                </div>

                <div class="shipping-box">

                    <h3>
                        Thông tin thanh toán
                    </h3>

                    <div class="shipping-row">

                        <div class="label">
                            Phương thức thanh toán
                        </div>

                        <div>

                            <%=
                                escapeHtml(paymentLabel(order.getPaymentMethod()))
                            %>

                        </div>

                    </div>

                    <div class="shipping-row">

                        <div class="label">
                            Trạng thái đơn hàng
                        </div>

                        <div>

                            <%=
                                escapeHtml(statusLabel(order.getStatus()))
                            %>

                        </div>

                    </div>

                </div>

            </div>

        </div>

        <!-- =============================
             TOTAL
        ============================== -->

        <div class="order-footer">

            <span>
                Tổng cộng:
            </span>

            <span class="total">

                <%= ViewUtil.formatCurrency(order.getTotalAmount()) %>

            </span>

        </div>

    </section>

    <%

            }

        }

    %>

</main>

</div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
<jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>

</html>
