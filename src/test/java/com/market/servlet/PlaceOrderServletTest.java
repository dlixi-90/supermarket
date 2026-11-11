package com.market.servlet;

import com.market.dao.OrderDAO;
import com.market.filter.ShoppingSessionFilter;
import com.market.model.CartItem;
import com.market.model.CheckoutData;
import java.util.Map;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlaceOrderServletTest {
    private static final String ID = "abcdef0123456789abcdef01";
    private final PlaceOrderServlet servlet = new PlaceOrderServlet();
    private final OrderDAO dao = mock(OrderDAO.class);

    @BeforeEach void setup() throws Exception {
        TestHttp.inject(servlet, "orderDAO", dao);
        when(dao.placeOrder(anyString(), any(), anyMap())).thenReturn("order-id");
    }

    private TestHttp reviewedCart() throws Exception {
        var http = new TestHttp(Map.of("checkoutToken", "review-token"));
        http.attributes.put("userId", ID);
        http.attributes.put("cart", Map.of(ID, new CartItem(ID, "Rau", "", 25000, 1, 10)));
        http.attributes.put("checkoutData", new CheckoutData("Khách", "0901234567", "123 Đường A", "COD"));
        http.attributes.put("checkoutToken", "review-token");
        http.attributes.put("checkoutCartSignature", ID + ":1:25000");
        return http;
    }

    @Test void confirmedOrderClearsCartAndConsumesToken() throws Exception {
        var http = reviewedCart();
        servlet.doPost(http.request, http.response);
        verify(dao).placeOrder(eq(ID), any(), anyMap());
        assertNull(http.attributes.get("cart"));
        assertNull(http.attributes.get("checkoutToken"));
        verify(http.response).sendRedirect("/my-orders?placed=1");
    }

    @Test void changedCartCannotUsePreviousConfirmation() throws Exception {
        var http = reviewedCart();
        http.attributes.put("cart", Map.of(ID, new CartItem(ID, "Rau", "", 25000, 2, 10)));
        servlet.doPost(http.request, http.response);
        verifyNoInteractions(dao);
        verify(http.response).sendRedirect("/checkout");
    }

    @Test void directPostWithoutReviewIsRejected() throws Exception {
        var http = reviewedCart();
        http.attributes.remove("checkoutToken");
        servlet.doPost(http.request, http.response);
        verifyNoInteractions(dao);
    }

    @Test void twoSimultaneousSubmissionsOnlyCreateOneOrder() throws Exception {
        var first = reviewedCart();
        var second = new TestHttp(Map.of("checkoutToken", "review-token"), first.session);
        var filter = new ShoppingSessionFilter();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Void> one = () -> { filter.doFilter(first.request, first.response,
                    (req, res) -> servlet.doPost(first.request, first.response)); return null; };
            Callable<Void> two = () -> { filter.doFilter(second.request, second.response,
                    (req, res) -> servlet.doPost(second.request, second.response)); return null; };
            for (Future<Void> task : pool.invokeAll(java.util.List.of(one, two))) task.get(10, TimeUnit.SECONDS);
            verify(dao, times(1)).placeOrder(eq(ID), any(), anyMap());
        } finally { pool.shutdownNow(); }
    }
}
