package com.market.servlet;

import com.market.dao.UserDAO;
import com.market.model.CartItem;
import com.market.model.User;
import com.market.util.PasswordUtil;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginServletTest {
    @Test void loginRotatesSessionIdAndPreservesGuestCart() throws Exception {
        var servlet = new LoginServlet();
        var dao = mock(UserDAO.class);
        TestHttp.inject(servlet, "userDAO", dao);
        var user = new User("abcdef0123456789abcdef01", "Khách", "customer@example.test",
                PasswordUtil.hashPassword("test-password"), "customer");
        when(dao.findByEmail("customer@example.test")).thenReturn(user);
        var http = new TestHttp(Map.of("email", "customer@example.test", "password", "test-password"));
        Object cart = Map.of("abcdef0123456789abcdef02", new CartItem("abcdef0123456789abcdef02", "Rau", "", 25000, 1, 10));
        http.attributes.put("cart", cart);
        http.attributes.put("checkoutToken", "old-review");
        http.attributes.put("redirectAfterLogin", "/checkout");
        servlet.doPost(http.request, http.response);
        verify(http.request).changeSessionId();
        verify(http.session, never()).invalidate();
        assertSame(cart, http.attributes.get("cart"));
        assertNull(http.attributes.get("checkoutToken"));
        assertNull(http.attributes.get("redirectAfterLogin"));
        assertEquals(user.getId(), http.attributes.get("userId"));
        verify(http.response).sendRedirect("/checkout");
    }
}
