package com.market.servlet;

import com.market.dao.ProductDAO;
import com.market.model.CartItem;
import com.market.model.Product;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CheckoutServletTest {
    private static final String ID = "abcdef0123456789abcdef01";
    private final CheckoutServlet servlet = new CheckoutServlet();
    private final ProductDAO dao = mock(ProductDAO.class);

    @BeforeEach void setup() throws Exception {
        TestHttp.inject(servlet, "productDAO", dao);
        when(dao.getById(ID)).thenReturn(new Product(ID, "Rau", "", 25000, 10, "", "", true));
    }

    private TestHttp checkout(String phone) throws Exception {
        var http = new TestHttp(Map.of("recipientName", "Khách hàng", "phone", phone,
                "shippingAddress", "123 Đường A", "paymentMethod", "COD"));
        http.attributes.put("cart", Map.of(ID, new CartItem(ID, "Rau", "", 25000, 1, 10)));
        return http;
    }

    @Test void validCheckoutCreatesConfirmationBoundToCart() throws Exception {
        var http = checkout("+84 901 234 567");
        servlet.doPost(http.request, http.response);
        assertNotNull(http.attributes.get("checkoutToken"));
        assertEquals(ID + ":1:25000", http.attributes.get("checkoutCartSignature"));
        verify(http.request).setAttribute("checkoutReady", true);
    }

    @ParameterizedTest @ValueSource(strings = {"--------", "++++++++", "123", "090+1234567", "1234567890123456"})
    void phoneMustContainRealDigits(String phone) throws Exception {
        var http = checkout(phone);
        http.attributes.put("checkoutToken", "old");
        servlet.doPost(http.request, http.response);
        assertNull(http.attributes.get("checkoutToken"));
        verify(http.request).setAttribute(eq("error"), anyString());
    }

    @Test void editingCheckoutRevokesPreviousConfirmation() throws Exception {
        var http = checkout("0901234567");
        http.attributes.put("checkoutToken", "old");
        servlet.doGet(http.request, http.response);
        assertNull(http.attributes.get("checkoutToken"));
    }

    @Test void outOfStockProductCannotBeConfirmed() throws Exception {
        var http = checkout("0901234567");
        when(dao.getById(ID)).thenReturn(new Product(ID, "Rau", "", 25000, 0, "", "", true));
        servlet.doPost(http.request, http.response);
        assertNull(http.attributes.get("checkoutToken"));
        verify(http.request).setAttribute(eq("error"), contains("hết hàng"));
    }
}
