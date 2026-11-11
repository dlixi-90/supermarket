<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,com.market.model.User,com.market.util.ViewUtil" %>
<%
    List<User> users = (List<User>) request.getAttribute("users");
    User editUser = (User) request.getAttribute("editUser");
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý người dùng</title>
    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>
<body class="msm-page msm-user">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content admin-layout">
    <jsp:include page="/WEB-INF/views/includes/admin-nav.jsp" />
    <main class="container">
        <h1>Quản lý người dùng</h1>
        <% if (editUser != null) { %>
        <section class="form-box" aria-labelledby="edit-user-title">
            <h2 id="edit-user-title">Cập nhật người dùng</h2>
            <form method="post" action="<%= request.getContextPath() %>/users">
                <input type="hidden" name="id" value="<%= ViewUtil.escapeHtml(editUser.getId()) %>">
                <label for="user-name">Họ và tên</label>
                <input id="user-name" type="text" name="name" required value="<%= ViewUtil.escapeHtml(editUser.getName()) %>">
                <label for="user-email">Email</label>
                <input id="user-email" type="email" name="email" required value="<%= ViewUtil.escapeHtml(editUser.getEmail()) %>">
                <label for="user-role">Vai trò</label>
                <select id="user-role" name="role">
                    <option value="customer" <%= "customer".equals(editUser.getRole()) ? "selected" : "" %>>Khách hàng</option>
                    <option value="admin" <%= "admin".equals(editUser.getRole()) ? "selected" : "" %>>Quản trị viên</option>
                </select>
                <p class="hint">Mật khẩu không được thay đổi tại đây.</p>
                <button class="save-button" type="submit" name="action" value="update">Cập nhật người dùng</button>
                <a class="button cancel-button" href="<%= request.getContextPath() %>/users">Hủy</a>
            </form>
        </section>
        <% } %>
        <div class="section-header"><h2>Danh sách người dùng</h2><span class="small"><%= users == null ? 0 : users.size() %> tài khoản</span></div>
        <div class="table-scroll"><table>
            <thead><tr><th scope="col">Họ tên</th><th scope="col">Email</th><th scope="col">Vai trò</th><th scope="col">Thao tác</th></tr></thead>
            <tbody>
                <% if (users != null && !users.isEmpty()) { for (User user : users) { %>
                <tr>
                    <td><%= ViewUtil.escapeHtml(user.getName()) %></td>
                    <td><%= ViewUtil.escapeHtml(user.getEmail()) %></td>
                    <td><%= "admin".equals(user.getRole()) ? "Quản trị viên" : "customer".equals(user.getRole()) ? "Khách hàng" : ViewUtil.escapeHtml(user.getRole()) %></td>
                    <td class="actions">
                        <a class="button cancel-button" href="<%= request.getContextPath() %>/users?action=edit&amp;id=<%= ViewUtil.escapeHtml(user.getId()) %>">Sửa</a>
                        <form method="post" action="<%= request.getContextPath() %>/users" onsubmit="return confirm('Bạn có chắc muốn xóa người dùng này không?');">
                            <input type="hidden" name="id" value="<%= ViewUtil.escapeHtml(user.getId()) %>">
                            <button class="delete-button" type="submit" name="action" value="delete">Xóa</button>
                        </form>
                    </td>
                </tr>
                <% } } else { %>
                <tr><td colspan="4">Chưa có người dùng nào.</td></tr>
                <% } %>
            </tbody>
        </table></div>
    </main>
</div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
<jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>
</html>
