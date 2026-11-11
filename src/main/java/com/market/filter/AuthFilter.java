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
            "/cart",
            "/checkout",
            "/place-order",
            "/my-orders"
        }
)
public class AuthFilter implements Filter {

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
        // NO CACHE PRIVATE PAGES
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

        // =================================
        // SESSION
        // =================================
        HttpSession session
                = httpRequest
                        .getSession(false);

        boolean loggedIn
                = session != null
                && session.getAttribute(
                        "userId"
                ) != null;

        if (!loggedIn) {

            if ("/cart".equals(httpRequest.getServletPath())
                    && "POST".equalsIgnoreCase(httpRequest.getMethod())
                    && "add".equals(httpRequest.getParameter("action"))) {
                HttpSession pendingSession = httpRequest.getSession(true);
                pendingSession.setAttribute(
                        "pendingCartProductId",
                        httpRequest.getParameter("productId")
                );
                pendingSession.setAttribute(
                        "pendingCartQuantity",
                        httpRequest.getParameter("quantity")
                );

                if ("XMLHttpRequest".equalsIgnoreCase(
                        httpRequest.getHeader("X-Requested-With"))) {
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.setContentType("application/json; charset=UTF-8");
                    httpResponse.setCharacterEncoding("UTF-8");
                    httpResponse.getWriter().write(
                            "{\"success\":false,\"requiresLogin\":true,\"loginUrl\":\""
                                    + httpRequest.getContextPath() + "/login\"}"
                    );
                    return;
                }
            }

            httpResponse.sendRedirect(
                    httpRequest.getContextPath()
                    + "/login"
            );

            return;
        }

        chain.doFilter(
                request,
                response
        );
    }

    @Override
    public void destroy() {
    }
}
