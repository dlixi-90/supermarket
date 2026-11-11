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

%>

<%

    Map<String, CartItem> cart =
            (Map<String, CartItem>)
            request.getAttribute(
                    "cart"
            );

    Long cartTotal =
            (Long)
            request.getAttribute(
                    "cartTotal"
            );

    Number cartQuantity =
            (Number)
            request.getAttribute(
                    "cartQuantity"
            );

    String error =
            (String)
            request.getAttribute(
                    "error"
            );

%>

<!DOCTYPE html>

<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Giỏ hàng</title>

<jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>

<body class="msm-page msm-cart">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content">

<div class="container">

    <h1>
        Giỏ hàng
    </h1>

    <% if (error != null) { %>

        <div class="error">

            <%= escapeHtml(error) %>

        </div>

    <% } %>

    <%

        if (cart == null
                || cart.isEmpty()) {

    %>

        <div class="empty">

            Giỏ hàng của bạn đang trống.

        </div>

    <%

        } else {

    %>

    <div class="table-scroll"><table>

        <thead>

        <tr>

            <th>Ảnh</th>
            <th>Sản phẩm</th>
            <th>Giá</th>
            <th>Số lượng</th>
            <th>Thành tiền</th>
            <th>Thao tác</th>

        </tr>

        </thead>

        <tbody>

        <%

            for (CartItem item :
                    cart.values()) {

        %>

        <tr>

            <!-- IMAGE -->

            <td>

                <%

                    String finalImageUrl = ViewUtil.resolveImageUrl(
                            item.getImageUrl(),
                            request.getContextPath()
                    );

                    if (!finalImageUrl.isEmpty()) {

                %>

                    <img
                        class="product-image"
                        src="<%=
                            escapeHtml(
                                finalImageUrl
                            )
                        %>"
                        alt="<%=
                            escapeHtml(
                                item.getProductName()
                            )
                        %>"
                    >

                <% } else { %>

                    <div class="no-image">
                        Chưa có ảnh
                    </div>

                <% } %>

            </td>

            <!-- NAME -->

            <td>

                <strong>

                    <%=
                        escapeHtml(
                            item.getProductName()
                        )
                    %>

                </strong>

            </td>

            <!-- PRICE -->

            <td>

                <%=
                    String.format(
                            "%,d",
                            item.getPrice()
                    )
                %>

                VNĐ

            </td>

            <!-- QUANTITY -->

            <td>

                <form
                    class="cart-quantity-form"
                    method="post"
                    action="<%=
                        request.getContextPath()
                    %>/cart"
                >

                    <input type="hidden" name="action" value="update">

                    <input
                        type="hidden"
                        name="productId"
                        value="<%=
                            escapeHtml(
                                item.getProductId()
                            )
                        %>"
                    >

                    <input
                        class="quantity-input"
                        aria-label="Số lượng của <%= escapeHtml(item.getProductName()) %>"
                        type="number"
                        name="quantity"
                        min="1"
                        max="<%=
                            item.getStock()
                        %>"
                        value="<%=
                            item.getQuantity()
                        %>"
                        required
                    >

                </form>

            </td>

            <!-- SUBTOTAL -->

            <td>

                <%=
                    String.format(
                            "%,d",
                            item.getSubtotal()
                    )
                %>

                VNĐ

            </td>

            <!-- REMOVE -->

            <td>

                <form
                    method="post"
                    action="<%=
                        request.getContextPath()
                    %>/cart"
                >

                    <input
                        type="hidden"
                        name="productId"
                        value="<%=
                            escapeHtml(
                                item.getProductId()
                            )
                        %>"
                    >

                    <button
                        class="danger"
                        type="submit"
                        name="action"
                        value="remove"
                        aria-label="Xóa <%= escapeHtml(item.getProductName()) %> khỏi giỏ hàng"
                        title="Remove khỏi giỏ hàng"
                    >
                        <i class="ri-delete-bin-line" aria-hidden="true"></i>
                    </button>

                </form>

            </td>

        </tr>

        <%

            }

        %>

        </tbody>

    </table></div>

    <!-- SUMMARY -->

    <div class="summary">

        <div>

            Số sản phẩm:

            <strong>
                <%=
                    cartQuantity == null
                    ? 0
                    : cartQuantity
                %>
            </strong>

        </div>

        <div>

            Tổng cộng:

            <strong>

                <%=
                    String.format(
                            "%,d",
                            cartTotal == null
                            ? 0L
                            : cartTotal
                    )
                %>

                VNĐ

            </strong>

        </div>

        <div class="summary-buttons">

            <!-- CLEAR -->

            <form
                class="inline"
                method="post"
                action="<%=
                    request.getContextPath()
                %>/cart"
                onsubmit="
                    return confirm(
                        'Bạn có chắc muốn xóa toàn bộ sản phẩm khỏi giỏ hàng không?'
                    );
                "
            >

                <button
                    class="secondary"
                    type="submit"
                    name="action"
                    value="clear"
                >
                    Xóa giỏ hàng
                </button>

            </form>

            <!-- CHECKOUT -->

            <a
                class="button primary"
                href="<%=
                    request.getContextPath()
                %>/checkout"
            >
                Thanh toán
            </a>

        </div>

    </div>

    <%

        }

    %>

</div>

</div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
<jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>

</html>
