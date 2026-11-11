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

    String error
            = (String) request.getAttribute("error");

    String email
            = (String) request.getAttribute("email");

    boolean registered
            = "1".equals(
                    request.getParameter(
                            "registered"
                    )
            );

    boolean loggedOut
            = "1".equals(
                    request.getParameter(
                            "logout"
                    )
            );

%>

<!DOCTYPE html>

<html lang="vi">

    <head>

        <meta charset="UTF-8">

        <title>Đăng nhập</title>

    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>

    <body class="msm-page msm-login">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content">

        <div class="page">

            <div class="login-box">

                <h1>Đăng nhập</h1>

                <!-- REGISTER SUCCESS -->

                <% if (registered) { %>

                <div class="success">

                    Đăng ký thành công.
                    Vui lòng đăng nhập.

                </div>

                <% } %>

                <% if (loggedOut) { %>

                <div class="success">
                    Bạn đã đăng xuất.
                </div>

                <% } %>

                <!-- LOGIN ERROR -->

                <% if (error != null) {%>

                <div class="error">

                    <%= escapeHtml(error)%>

                </div>

                <% }%>

                <!-- LOGIN FORM -->

                <form
                    method="post"
                    action="<%=request.getContextPath()%>/login"
                    >

                    <!-- EMAIL -->

                    <label>Email</label>

                    <input
                        type="email"
                        name="email"
                        required
                        autocomplete="email"
                        value="<%=escapeHtml(email)%>"
                        >

                    <!-- PASSWORD -->

                    <label>Mật khẩu</label>

                    <input
                        type="password"
                        name="password"
                        required
                        autocomplete="current-password"
                        >

                    <!-- BUTTON -->

                    <button type="submit">

                        Đăng nhập

                    </button>

                </form>

                <!-- REGISTER -->

                <div class="register-link">

                    Bạn chưa có tài khoản?

                    <a
                        href="<%=request.getContextPath()%>/register"
                        >
                        Đăng ký
                    </a>

                </div>

            </div>

        </div>

    </div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
<jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>

</html>
