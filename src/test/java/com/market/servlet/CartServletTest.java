package com.market.servlet;

import com.market.dao.ProductDAO;
import com.market.filter.ShoppingSessionFilter;
import com.market.model.CartItem;
import com.market.model.Product;
import com.mongodb.MongoTimeoutException;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.*;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CartServletTest {
    private static final String ID = "abcdef0123456789abcdef01";
    private final CartServlet servlet = new CartServlet();
    private final ProductDAO dao = mock(ProductDAO.class);
    private Product product;

    @BeforeEach void setup() throws Exception {
        TestHttp.inject(servlet, "productDAO", dao);
        product = new Product(ID, "Rau tươi", "", 25000, 100, "", "images/p1.png", true);
        when(dao.getById(ID)).thenReturn(product);
    }

    @Test void guestCanAddAndServerSetsProductData() throws Exception {
        var http = new TestHttp(Map.of("action", "add", "productId", ID, "quantity", "2", "price", "1"));
        servlet.doPost(http.request, http.response);
        Document result = Document.parse(http.body.toString());
        assertTrue(result.getBoolean("success"));
        assertEquals(2, ((Number) result.get("quantity")).intValue());
        CartItem item = cart(http).get(ID);
        assertEquals(25000, item.getPrice());
        assertEquals("Rau tươi", item.getProductName());
        assertEquals(50000, item.getSubtotal());
        verify(http.response).setHeader("Cache-Control", "no-store");
    }

    @Test void oldCartImagesAreRestoredWithoutMutatingThePreviousSnapshot() throws Exception {
        var http = new TestHttp(Map.of());
        var oldItem = new CartItem(ID, "Rau", "", 25000, 1, 100);
        http.attributes.put("cart", Map.of(ID, oldItem));
        servlet.doGet(http.request, http.response);
        assertEquals("images/p1.png", cart(http).get(ID).getImageUrl());
        assertEquals("", oldItem.getImageUrl());
        verify(http.request).setAttribute("cartQuantity", 1L);
        verify(http.dispatcher).forward(http.request, http.response);
    }

    @ParameterizedTest @ValueSource(strings = {"0", "-1", "abc", "2147483648", "101"})
    void invalidQuantityDoesNotChangeCart(String quantity) throws Exception {
        var http = new TestHttp(Map.of("action", "add", "productId", ID, "quantity", quantity));
        servlet.doPost(http.request, http.response);
        verify(http.response).setStatus(400);
        assertFalse(Document.parse(http.body.toString()).getBoolean("success"));
        assertNull(http.attributes.get("cart"));
    }

    @Test void repeatedAddCannotExceedStockOrOverflow() throws Exception {
        var http = new TestHttp(Map.of("action", "add", "productId", ID, "quantity", "2147483647"));
        product.setStock(Integer.MAX_VALUE);
        http.attributes.put("cart", Map.of(ID, new CartItem(ID, "Rau", "", 25000, 1, Integer.MAX_VALUE)));
        servlet.doPost(http.request, http.response);
        verify(http.response).setStatus(400);
        assertEquals(1, cart(http).get(ID).getQuantity());
    }

    @Test void objectIdCaseDoesNotCreateDuplicateLines() throws Exception {
        var http = new TestHttp(Map.of("action", "add", "productId", ID.toUpperCase(), "quantity", "1"));
        http.attributes.put("cart", Map.of(ID, new CartItem(ID, "Rau", "", 25000, 1, 100)));
        servlet.doPost(http.request, http.response);
        assertEquals(1, cart(http).size());
        assertEquals(2, cart(http).get(ID).getQuantity());
    }

    @ParameterizedTest @ValueSource(strings = {"missing", "inactive", "sold-out"})
    void unavailableProductsCannotBeAdded(String state) throws Exception {
        if (state.equals("missing")) when(dao.getById(ID)).thenReturn(null);
        if (state.equals("inactive")) product.setActive(false);
        if (state.equals("sold-out")) product.setStock(0);
        var http = new TestHttp(Map.of("action", "add", "productId", ID, "quantity", "1"));
        servlet.doPost(http.request, http.response);
        verify(http.response).setStatus(400);
        assertNull(http.attributes.get("cart"));
    }

    @ParameterizedTest @ValueSource(strings = {"", "invalid"})
    void unknownActionsAreRejected(String action) throws Exception {
        var http = new TestHttp(Map.of("action", action));
        servlet.doPost(http.request, http.response);
        verify(http.response).setStatus(400);
    }

    @Test void databaseOutageReturnsUsefulJsonWithoutChangingCart() throws Exception {
        when(dao.getById(ID)).thenThrow(new MongoTimeoutException("test outage"));
        var http = new TestHttp(Map.of("action", "add", "productId", ID, "quantity", "1"));
        servlet.doPost(http.request, http.response);
        verify(http.response).setStatus(503);
        assertFalse(http.body.toString().contains("test outage"));
        assertNull(http.attributes.get("cart"));
    }

    @ParameterizedTest @ValueSource(strings = {"/home", "/shop", "/WEB-INF/views/home.jsp", "//evil.example", "/\\evil.example"})
    void plainFormOnlyRedirectsToPublicCatalog(String destination) throws Exception {
        var http = new TestHttp(Map.of("action", "add", "productId", ID, "quantity", "1", "returnTo", destination));
        when(http.request.getHeader("X-Requested-With")).thenReturn(null);
        servlet.doPost(http.request, http.response);
        verify(http.response).sendRedirect(destination.equals("/home") ? "/home" : "/shop");
    }

    @Test void updateRemoveAndClearInvalidateOldCheckoutConfirmation() throws Exception {
        var initial = new TestHttp(Map.of());
        initial.attributes.put("cart", Map.of(ID, new CartItem(ID, "Rau", "", 25000, 2, 100)));
        for (String action : new String[]{"update", "remove", "clear"}) {
            initial.attributes.put("checkoutToken", "old-confirmation");
            var http = new TestHttp(Map.of("action", action, "productId", ID, "quantity", "3"), initial.session);
            servlet.doPost(http.request, http.response);
            assertNull(initial.attributes.get("checkoutToken"));
            if (action.equals("update")) assertEquals(3, cart(initial).get(ID).getQuantity());
            else assertTrue(cart(initial).isEmpty());
        }
    }

    @Test void parallelAddsDoNotLoseItems() throws Exception {
        var initial = new TestHttp(Map.of());
        var filter = new ShoppingSessionFilter();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            var tasks = new ArrayList<Future<?>>();
            for (int i = 0; i < 32; i++) {
                tasks.add(pool.submit(() -> {
                    try {
                        var http = new TestHttp(Map.of("action", "add", "productId", ID, "quantity", "1"), initial.session);
                        filter.doFilter(http.request, http.response, (req, res) -> servlet.doPost(http.request, http.response));
                        assertTrue(Document.parse(http.body.toString()).getBoolean("success"));
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));
            }
            for (Future<?> task : tasks) task.get(10, TimeUnit.SECONDS);
            assertEquals(32, cart(initial).get(ID).getQuantity());
        } finally { pool.shutdownNow(); }
    }

    @SuppressWarnings("unchecked") private Map<String, CartItem> cart(TestHttp http) {
        return (Map<String, CartItem>) http.attributes.get("cart");
    }
}
