package com.market.filter;

import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessFilterTest {
    @Test void cartIsPublicButCheckoutRequiresLogin() throws Exception {
        String[] routes = AuthFilter.class.getAnnotation(WebFilter.class).urlPatterns();
        assertFalse(Arrays.asList(routes).contains("/cart"));
        assertTrue(Arrays.asList(routes).contains("/checkout"));
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var session = mock(HttpSession.class);
        var chain = mock(FilterChain.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/checkout");
        new AuthFilter().doFilter(request, response, chain);
        verify(response).sendRedirect("/login");
        verify(session).setAttribute("redirectAfterLogin", "/checkout");
        verifyNoInteractions(chain);
    }

    @Test void customerCannotOpenAdministration() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var session = mock(HttpSession.class);
        var chain = mock(FilterChain.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn("customer-id");
        when(session.getAttribute("userRole")).thenReturn("customer");
        new AdminFilter().doFilter(request, response, chain);
        verify(response).sendError(403, "Access denied");
        verifyNoInteractions(chain);
    }
}
