<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Map,com.market.model.CartItem" %>
<%
    long headerCartQuantity = 0;
    Object headerCart = session.getAttribute("cart");
    if (headerCart instanceof Map<?, ?>) {
        for (Object value : ((Map<?, ?>) headerCart).values()) {
            if (value instanceof CartItem) {
                headerCartQuantity += ((CartItem) value).getQuantity();
            }
        }
    }
    boolean headerCartAdded = Boolean.TRUE.equals(session.getAttribute("cartItemAdded"));
    session.removeAttribute("cartItemAdded");
    String headerCartLabel = "Giỏ hàng, " + headerCartQuantity + " sản phẩm";
%>
<header>
      <!-- Nav -->
      <div class="nav container">
        <!-- Logo -->
        <a href="<%= request.getContextPath() %>/home" class="logo">
          <img src="<%= request.getContextPath() %>/images/logo.png" alt="logo" />
        </a>
        <!-- Nav Links -->
        <nav class="nav-links" aria-label="Điều hướng chính">
          <a href="<%= request.getContextPath() %>/home" class="nav-link">Trang chủ</a>
          <a href="<%= request.getContextPath() %>/shop" class="nav-link">Cửa hàng</a>
        </nav>
        <!-- Nav Icons -->
        <div class="nav-icons">
          <a href="<%= request.getContextPath() %>/cart"
             class="cart-link<%= headerCartAdded && headerCartQuantity > 0 ? " cart-link-added" : "" %>"
             aria-label="<%= headerCartLabel %>" title="<%= headerCartLabel %>">
            <i class="ri-shopping-bag-4-line" aria-hidden="true"></i>
            <span class="cart-count" aria-hidden="true" <%= headerCartQuantity == 0 ? "hidden" : "" %>><%= headerCartQuantity > 99 ? "99+" : Long.toString(headerCartQuantity) %></span>
          </a>
          <details class="account-menu"><summary aria-label="Tài khoản"><i class="ri-user-line"></i></summary><div class="account-panel">
<% if (session.getAttribute("userId") == null) { %>
<a href="<%= request.getContextPath() %>/login">Đăng nhập</a>
<% } else { %>
<span><%= com.market.util.ViewUtil.escapeHtml((String) session.getAttribute("userName")) %></span>
<a href="<%= request.getContextPath() %>/my-orders">Đơn hàng của tôi</a>
<% if ("admin".equals(session.getAttribute("userRole"))) { %><a href="<%= request.getContextPath() %>/dashboard">Dashboard</a><% } %>
<a href="<%= request.getContextPath() %>/logout">Đăng xuất</a><% } %></div></details>
          <!-- Menu Icon -->
          <div class="menu-icon" role="button" tabindex="0" aria-label="Mở menu" aria-expanded="false">
            <div class="line1"></div>
            <div class="line2"></div>
            <div class="line3"></div>
          </div>
        </div>
      </div>
    </header>
