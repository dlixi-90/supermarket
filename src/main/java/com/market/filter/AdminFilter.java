package com.market.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import javax.servlet.annotation.WebFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(
        urlPatterns = {
            "/dashboard",
            "/users",
            "/categories",
            "/products",
            "/orders"
        }
)
public class AdminFilter implements Filter {

    @Override
    public void init(
            FilterConfig filterConfig)
            throws ServletException {
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest
                = (HttpServletRequest) request;

        HttpServletResponse httpResponse
                = (HttpServletResponse) response;

        // =================================
        // NO CACHE
        // =================================
        httpResponse.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        httpResponse.setHeader(
                "Pragma",
                "no-cache"
        );

        httpResponse.setDateHeader(
                "Expires",
                0
        );

        HttpSession session
                = httpRequest
                        .getSession(false);

        // =================================
        // NOT LOGGED IN
        // =================================
        if (session == null
                || session.getAttribute(
                        "userId"
                ) == null) {

            httpResponse.sendRedirect(
                    httpRequest.getContextPath()
                    + "/login"
            );

            return;
        }

        // =================================
        // ROLE
        // =================================
        String role
                = (String) session.getAttribute(
                        "userRole"
                );

        // =================================
        // NOT ADMIN
        // =================================
        if (!"admin".equals(role)) {

            httpResponse.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied"
            );

            return;
        }

        // =================================
        // ADMIN
        // =================================
        chain.doFilter(
                request,
                response
        );
    }

    @Override
    public void destroy() {
    }
}
