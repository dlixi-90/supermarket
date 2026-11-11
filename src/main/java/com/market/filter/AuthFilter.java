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
            String redirectUrl = httpRequest.getRequestURI();
            if (httpRequest.getQueryString() != null) {
                redirectUrl += "?" + httpRequest.getQueryString();
            }
            if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
                httpRequest.getSession(true).setAttribute("redirectAfterLogin", redirectUrl);
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
