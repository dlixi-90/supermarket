package com.market.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Serializes cart and checkout requests for one customer, including duplicate order submissions. */
@WebFilter(urlPatterns = {"/", "/home", "/shop", "/cart", "/checkout", "/place-order", "/login", "/logout", "/register"})
public class ShoppingSessionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ((HttpServletResponse) response).setHeader("Cache-Control", "no-store");
        HttpSession session = ((HttpServletRequest) request).getSession();
        synchronized (session) {
            chain.doFilter(request, response);
        }
    }
}
