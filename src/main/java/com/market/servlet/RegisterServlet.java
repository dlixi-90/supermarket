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


@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserDAO userDAO;


    @Override
    public void init() {

        userDAO =
                new UserDAO();
    }


    // =====================================
    // SHOW REGISTER PAGE
    // =====================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        request
                .getRequestDispatcher(
                        "/WEB-INF/views/register.jsp"
                )
                .forward(
                        request,
                        response
                );
    }


    // =====================================
    // REGISTER USER
    // =====================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        request.setCharacterEncoding(
                "UTF-8"
        );


        String name =
                request.getParameter(
                        "name"
                );

        String email =
                request.getParameter(
                        "email"
                );

        String password =
                request.getParameter(
                        "password"
                );

        String confirmPassword =
                request.getParameter(
                        "confirmPassword"
                );


        // =================================
        // NORMALIZE
        // =================================

        name =
                name == null
                        ? ""
                        : name.trim();


        email =
                email == null
                        ? ""
                        : email
                                .trim()
                                .toLowerCase();


        // Giữ lại data nếu form lỗi

        request.setAttribute(
                "name",
                name
        );

        request.setAttribute(
                "email",
                email
        );


        // =================================
        // VALIDATE NAME
        // =================================

        if (name.isEmpty()) {

            showError(
                    request,
                    response,
                    "Vui lòng nhập họ tên."
            );

            return;
        }


        // =================================
        // VALIDATE EMAIL
        // =================================

        if (email.isEmpty()) {

            showError(
                    request,
                    response,
                    "Vui lòng nhập email."
            );

            return;
        }


        // =================================
        // PASSWORD
        // =================================

        if (password == null
                || password.isEmpty()) {

            showError(
                    request,
                    response,
                    "Vui lòng nhập mật khẩu."
            );

            return;
        }


        if (password.length() < 6) {

            showError(
                    request,
                    response,
                    "Mật khẩu phải có ít nhất 6 ký tự."
            );

            return;
        }


        // =================================
        // CONFIRM PASSWORD
        // =================================

        if (!password.equals(
                confirmPassword)) {

            showError(
                    request,
                    response,
                    "Mật khẩu xác nhận không khớp."
            );

            return;
        }


        // =================================
        // EMAIL UNIQUE
        // =================================

        if (userDAO.emailExists(email)) {

            showError(
                    request,
                    response,
                    "Email này đã được đăng ký."
            );

            return;
        }


        // =================================
        // PASSWORD HASH
        // =================================

        String passwordHash =
                PasswordUtil
                        .hashPassword(
                                password
                        );


        // =================================
        // CUSTOMER ACCOUNT
        // =================================

        User user =
                new User(
                        name,
                        email,
                        passwordHash,
                        "customer"
                );


        userDAO.insert(user);


        // =================================
        // SUCCESS
        // =================================

        response.sendRedirect(
                request.getContextPath()
                        + "/login?registered=1"
        );
    }


    // =====================================
    // ERROR
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
                        "/WEB-INF/views/register.jsp"
                )
                .forward(
                        request,
                        response
                );
    }
}
