package com.market.servlet;

import com.market.dao.UserDAO;
import com.market.model.User;
import com.market.util.PasswordUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() {

        userDAO
                = new UserDAO();
    }

    // =====================================
    // SHOW LOGIN PAGE
    // =====================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        // =================================
        // ALREADY LOGGED IN
        //
        // Customer và Admin đều về Home
        // =================================
        if (session != null
                && session.getAttribute(
                        "userId"
                ) != null) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/home"
            );

            return;
        }

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/login.jsp"
                )
                .forward(
                        request,
                        response
                );
    }

    // =====================================
    // LOGIN
    // =====================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        String email
                = normalizeEmail(
                        request.getParameter(
                                "email"
                        )
                );

        String password
                = request.getParameter(
                        "password"
                );

        // Giữ lại email nếu login lỗi
        request.setAttribute(
                "email",
                email
        );

        // =================================
        // VALIDATION
        // =================================
        if (email.isEmpty()) {

            showError(
                    request,
                    response,
                    "Vui lòng nhập email."
            );

            return;
        }

        if (password == null
                || password.isEmpty()) {

            showError(
                    request,
                    response,
                    "Vui lòng nhập mật khẩu."
            );

            return;
        }

        try {

            // =================================
            // FIND USER
            // =================================
            User user
                    = userDAO.findByEmail(
                            email
                    );


            /*
             * Không nói rõ email không tồn tại
             * hay password sai.
             */
            if (user == null) {

                showError(
                        request,
                        response,
                        "Email hoặc mật khẩu không chính xác."
                );

                return;
            }

            // =================================
            // VERIFY PASSWORD
            // =================================
            boolean passwordCorrect
                    = PasswordUtil
                            .verifyPassword(
                                    password,
                                    user.getPasswordHash()
                            );

            if (!passwordCorrect) {

                showError(
                        request,
                        response,
                        "Email hoặc mật khẩu không chính xác."
                );

                return;
            }

            // =================================
            // PREVENT SESSION FIXATION
            // =================================
            HttpSession oldSession
                    = request.getSession(false);

            Object pendingCartProductId = oldSession == null
                    ? null
                    : oldSession.getAttribute("pendingCartProductId");
            Object pendingCartQuantity = oldSession == null
                    ? null
                    : oldSession.getAttribute("pendingCartQuantity");

            if (oldSession != null) {

                oldSession.invalidate();
            }

            HttpSession session
                    = request.getSession(true);

            // =================================
            // ONLY SAFE SESSION VALUES
            // =================================
            session.setAttribute(
                    "userId",
                    user.getId()
            );

            session.setAttribute(
                    "userName",
                    user.getName()
            );

            session.setAttribute(
                    "userEmail",
                    user.getEmail()
            );

            session.setAttribute(
                    "userRole",
                    user.getRole()
            );

            if (pendingCartProductId != null && pendingCartQuantity != null) {
                session.setAttribute("pendingCartProductId", pendingCartProductId);
                session.setAttribute("pendingCartQuantity", pendingCartQuantity);
            }

            // 30 phút
            session.setMaxInactiveInterval(
                    30 * 60
            );

            // =================================
            // QUAN TRỌNG:
            //
            // Admin và Customer đều về /home
            // =================================
            String destination = pendingCartProductId != null && pendingCartQuantity != null
                    ? "/cart"
                    : "/home";
            response.sendRedirect(request.getContextPath() + destination);

        } catch (Exception e) {

            throw new ServletException(
                    "Đăng nhập thất bại",
                    e
            );
        }
    }

    // =====================================
    // SHOW ERROR
    // =====================================
    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException {

        request.setAttribute(
                "error",
                message
        );

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/login.jsp"
                )
                .forward(
                        request,
                        response
                );
    }

    // =====================================
    // NORMALIZE EMAIL
    // =====================================
    private String normalizeEmail(
            String email) {

        if (email == null) {

            return "";
        }

        return email
                .trim()
                .toLowerCase();
    }
}
