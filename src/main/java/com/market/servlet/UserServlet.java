package com.market.servlet;

import com.market.dao.UserDAO;
import com.market.model.User;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/users")
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() {

        userDAO =
                new UserDAO();
    }

    // =====================================
    // GET
    // LIST + EDIT
    // =====================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            String action =
                    request.getParameter(
                            "action"
                    );

            // EDIT
            if ("edit".equals(action)) {

                String id =
                        request.getParameter(
                                "id"
                        );

                User user =
                        userDAO.getById(id);

                if (user != null) {

                    request.setAttribute(
                            "editUser",
                            user
                    );
                }
            }

            // LIST
            List<User> users =
                    userDAO.getAll();

            request.setAttribute(
                    "users",
                    users
            );

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/user.jsp"
                    )
                    .forward(
                            request,
                            response
                    );

        } catch (Exception e) {

            throw new ServletException(
                    "Cannot load users",
                    e
            );
        }
    }

    // =====================================
    // POST
    // =====================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        String action =
                request.getParameter(
                        "action"
                );

        try {

            if ("update".equals(action)) {

                updateUser(request);

            } else if ("delete".equals(action)) {

                deleteUser(request);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thao tác người dùng không hợp lệ.");
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                            + "/users"
            );

        } catch (Exception e) {

            throw new ServletException(
                    "Thao tác người dùng thất bại",
                    e
            );
        }
    }

    // =====================================
    // UPDATE USER
    // =====================================
    private void updateUser(
            HttpServletRequest request) {

        String id =
                request.getParameter(
                        "id"
                );

        String name =
                request.getParameter(
                        "name"
                );

        String email =
                request.getParameter(
                        "email"
                );

        String role =
                request.getParameter(
                        "role"
                );

        if (name == null
                || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập họ tên."
            );
        }

        if (email == null
                || email.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập email."
            );
        }

        email =
                email.trim()
                        .toLowerCase();

        User existingUser =
                userDAO.getById(id);

        if (existingUser == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy người dùng."
            );
        }

        if (userDAO
                .emailExistsForOtherUser(
                        email,
                        id
                )) {

            throw new IllegalArgumentException(
                    "Email này đã được sử dụng."
            );
        }

        role =
                normalizeRole(role);

        User user =
                new User(
                        id,
                        name.trim(),
                        email,
                        existingUser
                                .getPasswordHash(),
                        role
                );

        userDAO.update(user);
    }

    // =====================================
    // DELETE
    // =====================================
    private void deleteUser(
            HttpServletRequest request) {

        String id =
                request.getParameter(
                        "id"
                );

        userDAO.delete(id);
    }

    // =====================================
    // ROLE
    // =====================================
    private String normalizeRole(
            String role) {

        if ("admin".equals(role)) {
            return "admin";
        }

        return "customer";
    }
}
