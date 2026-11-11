<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,java.util.Locale,java.text.SimpleDateFormat,com.market.model.Order,com.market.util.ViewUtil" %>
<%!
    private long metric(javax.servlet.http.HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
    private String number(long value) { return String.format("%,d", value); }
    private String statusLabel(String status) {
        if ("Pending".equals(status)) return "Chờ xác nhận";
        if ("Processing".equals(status)) return "Đang xử lý";
        if ("Shipped".equals(status)) return "Đang giao";
        if ("Delivered".equals(status)) return "Đã giao";
        if ("Cancelled".equals(status)) return "Đã hủy";
        return "Không xác định";
    }
%>
<%
    long customers = metric(request, "totalCustomers");
    long products = metric(request, "totalProducts");
    long activeProducts = metric(request, "activeProducts");
    long totalOrders = metric(request, "totalOrders");
    long revenue = metric(request, "deliveredRevenue");
    long lowStock = metric(request, "lowStockProducts");
    String[] statuses = {"Pending", "Processing", "Shipped", "Delivered", "Cancelled"};
    long[] counts = {
        metric(request, "pendingOrders"), metric(request, "processingOrders"),
        metric(request, "shippedOrders"), metric(request, "deliveredOrders"),
        metric(request, "cancelledOrders")
    };
    List<Order> recentOrders = (List<Order>) request.getAttribute("recentOrders");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    String contextPath = request.getContextPath();
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>
<body class="msm-page msm-dashboard">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content admin-layout">
    <jsp:include page="/WEB-INF/views/includes/admin-nav.jsp" />
    <main class="container dashboard-overview">
        <div class="dashboard-heading">
            <div>
                <h1>Dashboard</h1>
            </div>
        </div>

        <section class="stats-grid dashboard-metrics" aria-label="Thống kê cửa hàng">
            <a class="card" href="<%= contextPath %>/orders?status=Delivered">
                <div class="metric-heading"><span class="card-label">Doanh thu đơn đã giao</span><i class="ri-wallet-3-line" aria-hidden="true"></i></div>
                <div class="card-number"><%= number(revenue) %> <small>₫</small></div>
                <p class="card-note">Từ <%= number(counts[3]) %> đơn đã giao</p>
            </a>
            <a class="card" href="<%= contextPath %>/orders">
                <div class="metric-heading"><span class="card-label">Tổng số đơn hàng</span><i class="ri-shopping-bag-line" aria-hidden="true"></i></div>
                <div class="card-number"><%= number(totalOrders) %></div>
                <p class="card-note"><%= number(counts[0] + counts[1] + counts[2]) %> đơn đang chờ hoàn tất</p>
            </a>
            <a class="card" href="<%= contextPath %>/products">
                <div class="metric-heading"><span class="card-label">Sản phẩm</span><i class="ri-box-3-line" aria-hidden="true"></i></div>
                <div class="card-number"><%= number(products) %></div>
                <p class="card-note"><%= number(activeProducts) %> đang được bán</p>
            </a>
            <a class="card" href="<%= contextPath %>/users">
                <div class="metric-heading"><span class="card-label">Khách hàng</span><i class="ri-group-line" aria-hidden="true"></i></div>
                <div class="card-number"><%= number(customers) %></div>
                <p class="card-note">Tài khoản khách hàng đã đăng ký</p>
            </a>
        </section>

        <section class="dashboard-attention" aria-labelledby="attention-title">
            <div class="attention-grid">
                <a class="attention-item" href="<%= contextPath %>/orders?status=Pending">
                    <span class="attention-icon"><i class="ri-time-line" aria-hidden="true"></i></span>
                    <div><strong><%= number(counts[0]) %> đơn chờ xác nhận</strong><span>Kiểm tra và xác nhận đơn mới</span></div>
                    <i class="ri-arrow-right-line" aria-hidden="true"></i>
                </a>
                <a class="attention-item" href="<%= contextPath %>/orders?status=Processing">
                    <span class="attention-icon"><i class="ri-truck-line" aria-hidden="true"></i></span>
                    <div><strong><%= number(counts[1]) %> đơn đang xử lý</strong><span>Chuẩn bị đơn hàng để giao</span></div>
                    <i class="ri-arrow-right-line" aria-hidden="true"></i>
                </a>
                <a class="attention-item" href="<%= contextPath %>/products?stock=low#product-list">
                    <span class="attention-icon"><i class="ri-stock-line" aria-hidden="true"></i></span>
                    <div><strong><%= number(lowStock) %> sản phẩm sắp hết hàng</strong><span>Sản phẩm đang bán còn không quá 5 món</span></div>
                    <i class="ri-arrow-right-line" aria-hidden="true"></i>
                </a>
            </div>
        </section>

        <div class="dashboard-details">
            <section class="dashboard-panel" aria-labelledby="recent-title">
                <div class="section-header">
                    <div><h2 id="recent-title">Đơn hàng gần đây</h2><p class="dashboard-description">5 đơn hàng mới nhất</p></div>
                    <a class="dashboard-link" href="<%= contextPath %>/orders">Xem tất cả <i class="ri-arrow-right-line" aria-hidden="true"></i></a>
                </div>
                <% if (recentOrders == null || recentOrders.isEmpty()) { %>
                <div class="empty">Chưa có đơn hàng. Đơn hàng mới sẽ xuất hiện tại đây.</div>
                <% } else { %>
                <div class="table-scroll"><table>
                    <thead><tr><th scope="col">Đơn hàng / Ngày</th><th scope="col">Khách hàng</th><th scope="col">Trạng thái</th><th scope="col">Tổng cộng</th></tr></thead>
                    <tbody>
                    <% for (Order order : recentOrders) {
                        String orderId = order.getId() == null ? "" : order.getId();
                        String shortId = orderId.length() > 8 ? orderId.substring(orderId.length() - 8) : orderId;
                        String status = order.getStatus() == null ? "Unknown" : order.getStatus();
                    %>
                    <tr>
                        <td><a class="dashboard-order-link" href="<%= contextPath %>/orders#order-<%= ViewUtil.escapeHtml(orderId) %>" title="<%= ViewUtil.escapeHtml(orderId) %>">#<%= ViewUtil.escapeHtml(shortId) %></a><span class="small dashboard-date"><%= order.getOrderDate() == null ? "" : dateFormat.format(order.getOrderDate()) %></span></td>
                        <td><%= ViewUtil.escapeHtml(order.getRecipientName()) %></td>
                        <td><span class="status <%= ViewUtil.escapeHtml(status.toLowerCase(Locale.ROOT)) %>"><%= ViewUtil.escapeHtml(statusLabel(status)) %></span></td>
                        <td class="money"><%= number(order.getTotalAmount()) %> ₫</td>
                    </tr>
                    <% } %>
                    </tbody>
                </table></div>
                <% } %>
            </section>
            <section class="dashboard-panel" aria-labelledby="status-title">
                <div class="section-header"><div><h2 id="status-title">Trạng thái đơn hàng</h2><p class="dashboard-description"><%= number(totalOrders) %> đơn hàng ở tất cả trạng thái</p></div></div>
                <div class="dashboard-status-list">
                    <% for (int i = 0; i < statuses.length; i++) {
                        double percentage = totalOrders > 0 ? Math.min(100.0, 100.0 * counts[i] / totalOrders) : 0.0;
                    %>
                    <a class="dashboard-status-row" href="<%= contextPath %>/orders?status=<%= statuses[i] %>">
                        <div><span><span class="dashboard-dot <%= statuses[i].toLowerCase(Locale.ROOT) %>" aria-hidden="true"></span><%= statusLabel(statuses[i]) %></span><strong><%= number(counts[i]) %></strong></div>
                        <span class="dashboard-track" aria-hidden="true"><span style="width:<%= String.format(Locale.ROOT, "%.1f", percentage) %>%"></span></span>
                    </a>
                    <% } %>
                </div>
            </section>
        </div>
    </main>
</div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
<jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>
</html>
