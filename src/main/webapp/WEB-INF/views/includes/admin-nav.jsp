<%@ page contentType="text/html;charset=UTF-8" %>
<nav class="admin-nav" aria-label="Quản trị">
    <p class="admin-nav-title">Quản lý</p>
    <a href="<%= request.getContextPath() %>/dashboard"><i class="ri-dashboard-line" aria-hidden="true"></i><span>Dashboard</span></a>
    <a href="<%= request.getContextPath() %>/users"><i class="ri-group-line" aria-hidden="true"></i><span>Người dùng</span></a>
    <a href="<%= request.getContextPath() %>/categories"><i class="ri-layout-grid-line" aria-hidden="true"></i><span>Danh mục</span></a>
    <a href="<%= request.getContextPath() %>/products"><i class="ri-box-3-line" aria-hidden="true"></i><span>Sản phẩm</span></a>
    <a href="<%= request.getContextPath() %>/orders"><i class="ri-shopping-bag-line" aria-hidden="true"></i><span>Đơn hàng</span></a>
</nav>
