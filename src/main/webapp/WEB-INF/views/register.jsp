<%@ page contentType="text/html;charset=UTF-8" %>

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

    String error =
            (String)
            request.getAttribute(
                    "error"
            );

    String name =
            (String)
            request.getAttribute(
                    "name"
            );

    String email =
            (String)
            request.getAttribute(
                    "email"
            );

    boolean success =
            "1".equals(
                    request.getParameter(
                            "success"
                    )
            );

%>

<!DOCTYPE html>

<html lang="vi">

<head>

    <meta charset="UTF-8">

    <title>Đăng ký</title>

<jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>

<body class="msm-page msm-register">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content">

<div class="page">

    <div class="register-box">

        <h1>Đăng ký</h1>

        <% if (error != null) { %>

            <div class="error">
                <%= escapeHtml(error) %>
            </div>

        <% } %>

        <% if (success) { %>

            <div class="success">
                Đăng ký thành công.
                Bạn có thể đăng nhập ngay.
            </div>

        <% } %>

        <form
            method="post"
            action="<%=
                request.getContextPath()
            %>/register"
        >

            <!-- NAME -->

            <label>Họ và tên</label>

            <input
                type="text"
                name="name"
                required
                value="<%=
                    escapeHtml(name)
                %>"
            >

            <!-- EMAIL -->

            <label>Email</label>

            <input
                type="email"
                name="email"
                required
                value="<%=
                    escapeHtml(email)
                %>"
            >

            <!-- PASSWORD -->

            <label>Mật khẩu</label>

            <input
                type="password"
                name="password"
                required
                minlength="6"
            >

            <div class="hint">
                Tối thiểu 6 ký tự.
            </div>

            <!-- CONFIRM -->

            <label>Xác nhận mật khẩu</label>

            <input
                type="password"
                name="confirmPassword"
                required
                minlength="6"
            >

            <button type="submit">
                Đăng ký
            </button>

        </form>

        <div class="register-link">
            Đã có tài khoản?
            <a href="<%= request.getContextPath() %>/login">Đăng nhập</a>
        </div>

    </div>

</div>

</div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
<jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>

</html>
