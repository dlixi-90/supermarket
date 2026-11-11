<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%@ page import="com.market.model.Order" %>
<%@ page import="com.market.model.OrderItem" %>

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

        if ("Pending".equals(status)) {
            return "pending";
        }

        if ("Processing".equals(status)) {
            return "processing";
        }

        if ("Shipped".equals(status)) {
            return "shipped";
        }

        if ("Delivered".equals(status)) {
            return "delivered";
        }

        if ("Cancelled".equals(status)) {
            return "cancelled";
        }

        return "unknown";
    }

    private boolean canUpdate(
            String status) {

        return "Pending".equals(status)
                || "Processing".equals(status)
                || "Shipped".equals(status);
    }

    private String statusLabel(String status) {
        if ("Pending".equals(status)) return "Chờ xác nhận";
        if ("Processing".equals(status)) return "Đang xử lý";
        if ("Shipped".equals(status)) return "Đang giao";
        if ("Delivered".equals(status)) return "Đã giao";
        if ("Cancelled".equals(status)) return "Đã hủy";
        return "Không xác định";
    }

    private String paymentLabel(String method) {
        if ("COD".equals(method)) return "Thanh toán khi nhận hàng (COD)";
        if ("QR".equals(method)) return "Thanh toán bằng mã QR";
        return method == null ? "" : method;
    }

%>

<%

    List<Order> orders
            = (List<Order>) request.getAttribute(
                    "orders"
            );

    String message
            = (String) request.getAttribute(
                    "message"
            );

    String error
            = (String) request.getAttribute(
                    "error"
            );

    String userName
            = (String) session.getAttribute(
                    "userName"
            );

    SimpleDateFormat dateFormat
            = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm"
            );

%>

<!DOCTYPE html>

<html lang="vi">

    <head>

        <meta charset="UTF-8">

        <title>Quản lý đơn hàng</title>

    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>

    <body class="msm-page msm-order">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content admin-layout">
<jsp:include page="/WEB-INF/views/includes/admin-nav.jsp" />

        <!-- =====================================
             HEADER
        ===================================== -->

        <!-- =====================================
             CONTENT
        ===================================== -->

        <main class="container">

            <h1>
                Quản lý đơn hàng
            </h1>

            <nav class="list-filters" aria-label="Lọc đơn hàng theo trạng thái">
                <a href="<%= request.getContextPath() %>/orders" aria-current="<%= request.getAttribute("statusFilter") == null ? "page" : "false" %>">Tất cả đơn hàng</a>
                <% for (String filterStatus : new String[]{"Pending", "Processing", "Shipped", "Delivered", "Cancelled"}) { %>
                <a href="<%= request.getContextPath() %>/orders?status=<%= filterStatus %>" aria-current="<%= filterStatus.equals(request.getAttribute("statusFilter")) ? "page" : "false" %>"><%= statusLabel(filterStatus) %></a>
                <% } %>
            </nav>

            <% if (message != null) {%>

            <div class="message">

                <%= escapeHtml(message)%>

            </div>

            <% } %>

            <% if (error != null) {%>

            <div class="error">

                <%= escapeHtml(error)%>

            </div>

            <% } %>

            <%

                if (orders == null
                        || orders.isEmpty()) {

            %>

            <div class="empty">

                <h2>
                    Không tìm thấy đơn hàng nào.
                </h2>

            </div>

            <%    } else {

                for (Order order
                        : orders) {

            %>

            <!-- =================================
                 ORDER CARD
            ================================== -->

            <section class="order-card" id="order-<%= escapeHtml(order.getId()) %>">

                <!-- HEADER -->

                <div class="order-header">

                    <div>

                        <div class="label">
                            Mã đơn hàng
                        </div>

                        <div class="value">

                            <%=escapeHtml(
                                    order.getId()
                            )%>

                        </div>

                    </div>

                    <div>

                        <div class="label">
                            Ngày đặt hàng
                        </div>

                        <div class="value">

                            <%=order.getOrderDate()
                                    != null
                                            ? dateFormat.format(
                                                    order.getOrderDate()
                                            )
                                            : ""%>

                        </div>

                    </div>

                    <div>

                        <div class="label">
                            Người nhận
                        </div>

                        <div class="value">

                            <%=escapeHtml(
                                    order
                                            .getRecipientName()
                            )%>

                        </div>

                    </div>

                    <div>

                        <div class="label">
                            Thanh toán
                        </div>

                        <div class="value">

                            <%=escapeHtml(paymentLabel(order.getPaymentMethod()))%>

                        </div>

                    </div>

                    <div>

                        <div class="label">
                            Trạng thái
                        </div>

                        <span
                            class="status <%=statusClass(
                                    order.getStatus()
                            )%>"
                            >

                            <%=escapeHtml(statusLabel(order.getStatus()))%>

                        </span>

                    </div>

                </div>

                <!-- =================================
                     BODY
                ================================== -->

                <div class="order-body">

                    <h3 class="section-title">
                        Sản phẩm trong đơn
                    </h3>

                    <div class="table-scroll"><table class="item-table">

                        <thead>

                            <tr>

                                <th>Sản phẩm</th>
                                <th>Mã sản phẩm</th>
                                <th>Giá</th>
                                <th>Số lượng</th>
                                <th>Thành tiền</th>

                            </tr>

                        </thead>

                        <tbody>

                            <%

                                List<OrderItem> items
                                        = order.getItems();

                                if (items != null) {

                                    for (OrderItem item
                                            : items) {

                            %>

                            <tr>

                                <td>

                                    <strong>

                                        <%=escapeHtml(
                                                item
                                                        .getProductName()
                                        )%>

                                    </strong>

                                </td>

                                <td>

                                    <%=escapeHtml(
                                            item
                                                    .getProductId()
                                    )%>

                                </td>

                                <td class="money">

                                    <%=String.format(
                                            "%,d",
                                            item.getPrice()
                                    )%>

                                    VNĐ

                                </td>

                                <td>

                                    <%=item.getQuantity()%>

                                </td>

                                <td class="money">

                                    <strong>

                                        <%=String.format(
                                                "%,d",
                                                item
                                                        .getSubtotal()
                                        )%>

                                        VNĐ

                                    </strong>

                                </td>

                            </tr>

                            <%

                                    }

                                }

                            %>

                        </tbody>

                    </table></div>

                    <!-- =================================
                         SHIPPING + CUSTOMER
                    ================================== -->

                    <div class="info-grid">

                        <div class="info-box">

                            <h3>
                                Thông tin giao hàng
                            </h3>

                            <div class="info-row">

                                <div class="label">
                                    Người nhận
                                </div>

                                <div>

                                    <%=escapeHtml(
                                            order
                                                    .getRecipientName()
                                    )%>

                                </div>

                            </div>

                            <div class="info-row">

                                <div class="label">
                                    Số điện thoại
                                </div>

                                <div>

                                    <%=escapeHtml(
                                            order.getPhone()
                                    )%>

                                </div>

                            </div>

                            <div class="info-row">

                                <div class="label">
                                    Địa chỉ
                                </div>

                                <div>

                                    <%=escapeHtml(
                                            order
                                                    .getShippingAddress()
                                    )%>

                                </div>

                            </div>

                        </div>

                        <div class="info-box">

                            <h3>
                                Thông tin đơn hàng
                            </h3>

                            <div class="info-row">

                                <div class="label">
                                    Mã người dùng
                                </div>

                                <div>

                                    <%=escapeHtml(
                                            order
                                                    .getUserId()
                                    )%>

                                </div>

                            </div>

                            <div class="info-row">

                                <div class="label">
                                    Thanh toán
                                </div>

                                <div>

                                    <%=escapeHtml(paymentLabel(order.getPaymentMethod()))%>

                                </div>

                            </div>

                            <div class="info-row">

                                <div class="label">
                                    Trạng thái hiện tại
                                </div>

                                <div>

                                    <%=escapeHtml(
                                            order
                                                    .getStatus()
                                    )%>

                                </div>

                            </div>

                        </div>

                    </div>

                </div>

                <!-- =================================
                     FOOTER
                ================================== -->

                <div class="order-footer">

                    <!-- STATUS UPDATE -->

                    <div>

                        <% if (canUpdate(
                                    order.getStatus()
                            )) {%>

                        <form
                            class="status-form"

                            method="post"

                            action="<%=request.getContextPath()%>/orders"
                            >

                            <input
                                type="hidden"
                                name="orderId"
                                value="<%=escapeHtml(
                                        order.getId()
                                )%>"
                                >

                            <input
                                type="hidden"
                                name="action"
                                value="updateStatus"
                                >

                            <label>
                                Đổi trạng thái:
                            </label>

                            <select
                                name="status"
                                required
                                >

                                <option value="">
                                    -- Chọn --
                                </option>

                                <% if ("Pending".equals(
                                            order.getStatus()
                                    )) { %>

                                <option
                                    value="Processing"
                                    >
                                    Đang xử lý
                                </option>

                                <option
                                    value="Cancelled"
                                    >
                                    Đã hủy
                                </option>

                                <% } %>

                                <% if ("Processing".equals(
                                            order.getStatus()
                                    )) { %>

                                <option
                                    value="Shipped"
                                    >
                                    Đang giao
                                </option>

                                <option
                                    value="Cancelled"
                                    >
                                    Đã hủy
                                </option>

                                <% } %>

                                <% if ("Shipped".equals(
                                            order.getStatus()
                                    )) { %>

                                <option
                                    value="Delivered"
                                    >
                                    Đã giao
                                </option>

                                <% } %>

                            </select>

                            <button type="submit">

                                Cập nhật trạng thái

                            </button>

                        </form>

                        <% } else { %>

                        <span class="final">

                            Đơn hàng này đã hoàn tất
                            và không thể cập nhật.

                        </span>

                        <% }%>

                    </div>

                    <!-- TOTAL -->

                    <div>

                        Tổng cộng:

                        <span class="total">

                            <%=String.format(
                                    "%,d",
                                    order.getTotalAmount()
                            )%>

                            VNĐ

                        </span>

                    </div>

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
