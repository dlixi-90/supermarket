<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.util.Map" %>

<%@ page import="com.market.model.CartItem" %>
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

    private String paymentLabel(String method) {
        if ("COD".equals(method)) return "Thanh toán khi nhận hàng (COD)";
        if ("QR".equals(method)) return "Thanh toán bằng mã QR";
        return method == null ? "" : method;
    }

%>

<%

    Map<String, CartItem> cart
            = (Map<String, CartItem>) request.getAttribute(
                    "cart"
            );

    Long cartTotal
            = (Long) request.getAttribute(
                    "cartTotal"
            );

    Number cartQuantity
            = (Number) request.getAttribute(
                    "cartQuantity"
            );

    String error
            = (String) request.getAttribute(
                    "error"
            );

    String recipientName
            = (String) request.getAttribute(
                    "recipientName"
            );

    String phone
            = (String) request.getAttribute(
                    "phone"
            );

    String shippingAddress
            = (String) request.getAttribute(
                    "shippingAddress"
            );

    String paymentMethod
            = (String) request.getAttribute(
                    "paymentMethod"
            );

    Boolean checkoutReady
            = (Boolean) request.getAttribute(
                    "checkoutReady"
            );

    boolean ready
            = Boolean.TRUE.equals(
                    checkoutReady
            );

%>

<!DOCTYPE html>

<html lang="vi">

    <head>

        <meta charset="UTF-8">

        <title>Thanh toán</title>

    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>

    <body class="msm-page msm-checkout">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content">

        <main class="container">

            <h1>Thanh toán</h1>

            <% if (error != null) {%>

            <div class="error">

                <%= escapeHtml(error)%>

                <br><br>

                <a
                    href="<%=request.getContextPath()%>/cart"
                    >
                    Quay lại giỏ hàng
                </a>

            </div>

            <% } %>

            <% if (ready) { %>

            <div class="success">

                Thông tin thanh toán hợp lệ.
                Vui lòng kiểm tra đơn hàng trước khi đặt.

            </div>

            <% } %>

            <div class="checkout-layout">

                <!-- =================================
                     SHIPPING
                ================================== -->

                <section class="box">

                    <% if (!ready) {%>

                    <h2>
                        Thông tin giao hàng
                    </h2>

                    <form

                        method="post"

                        action="<%=request.getContextPath()%>/checkout"

                        >

                        <label>
                            Tên người nhận
                        </label>

                        <input

                            type="text"

                            name="recipientName"

                            maxlength="100"

                            required

                            value="<%=escapeHtml(
                                recipientName
                        )%>"

                            >

                        <label>
                            Số điện thoại
                        </label>

                        <input

                            type="tel"

                            name="phone"

                            maxlength="20"

                            required

                            value="<%=escapeHtml(
                                phone
                        )%>"

                            >

                        <label>
                            Địa chỉ giao hàng
                        </label>

                        <textarea

                            name="shippingAddress"

                            maxlength="500"

                            required

                            ><%=escapeHtml(
                            shippingAddress
                    )%></textarea>

                        <label>
                            Thanh toán
                        </label>

                        <select
                            name="paymentMethod"
                            required
                            >

                            <option
                                value="COD"

                                <%=paymentMethod == null
                                        || "COD".equals(
                                                paymentMethod
                                    )
                                    ? "selected"
                                    : ""%>
                                >
                                Thanh toán khi nhận hàng (COD)
                            </option>

                            <option
                                value="QR"

                                <%="QR".equals(
                                    paymentMethod
                            )
                                    ? "selected"
                                    : ""%>
                                >
                                Thanh toán bằng mã QR
                            </option>

                        </select>

                        <button
                            class="primary"
                            type="submit"
                            >
                            Xem lại đơn hàng
                        </button>

                        <a
                            class="button secondary"
                            href="<%=request.getContextPath()%>/cart"
                            >
                            Quay lại giỏ hàng
                        </a>

                    </form>

                    <% } else {%>

                    <!-- =========================
                         REVIEW SHIPPING DATA
                    ========================== -->

                    <h2>
                        Thông tin giao hàng
                    </h2>

                    <div class="review">

                        <div class="review-row">

                            <div class="review-label">
                                Người nhận
                            </div>

                            <div>

                                <%=escapeHtml(
                                    recipientName
                            )%>

                            </div>

                        </div>

                        <div class="review-row">

                            <div class="review-label">
                                Số điện thoại
                            </div>

                            <div>

                                <%=escapeHtml(
                                    phone
                            )%>

                            </div>

                        </div>

                        <div class="review-row">

                            <div class="review-label">
                                Địa chỉ giao hàng
                            </div>

                            <div>

                                <%=escapeHtml(
                                    shippingAddress
                            )%>

                            </div>

                        </div>

                        <div class="review-row">

                            <div class="review-label">
                                Thanh toán
                            </div>

                            <div>

                                <%=escapeHtml(paymentLabel(paymentMethod))%>

                            </div>

                        </div>

                    </div>

                    <form
                        class="checkout-actions"
                        method="post"
                        action="<%=request.getContextPath()%>/place-order"
                        >
                        <input type="hidden" name="checkoutToken" value="<%=escapeHtml((String) request.getAttribute("checkoutToken"))%>">
                        <a
                            class="button secondary"
                            href="<%=request.getContextPath()%>/checkout"
                            >
                            Chỉnh sửa thông tin
                        </a>
                        <button
                            class="primary"
                            type="submit"
                            >
                            Đặt hàng
                        </button>
                    </form>

                    <% } %>

                </section>

                <!-- =================================
                     ORDER SUMMARY
                ================================== -->

                <aside class="box">

                    <h2>
                        Tóm tắt đơn hàng
                    </h2>

                    <%

                        if (cart != null) {

                            for (CartItem item
                                    : cart.values()) {

                    %>

                    <div class="cart-item">

                        <div>

                            <div class="product-name">

                                <%=escapeHtml(
                                    item
                                            .getProductName()
                            )%>

                            </div>

                            <div class="small">

                                <%= ViewUtil.formatCurrency(item.getPrice()) %>

                                ×

                                <%=item.getQuantity()%>

                            </div>

                        </div>

                        <div class="price">

                            <%= ViewUtil.formatCurrency(item.getSubtotal()) %>

                        </div>

                    </div>

                    <%

                            }

                        }

                    %>

                    <div class="summary-line">

                        <span>
                            Sản phẩm
                        </span>

                        <span>

                            <%=cartQuantity == null
                            ? 0
                            : cartQuantity%>

                        </span>

                    </div>

                    <div
                        class="summary-line total"
                        >

                        <span>
                            Tổng cộng
                        </span>

                        <span>

                            <%= ViewUtil.formatCurrency(cartTotal == null ? 0L : cartTotal) %>

                        </span>

                    </div>

                </aside>

            </div>

        </main>

    </div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
<jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>

</html>
