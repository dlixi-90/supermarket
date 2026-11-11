package com.market.servlet;

import com.market.dao.CategoryDAO;
import com.market.dao.ProductDAO;
import com.market.model.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServletPaginationTest {
    private final ProductServlet servlet = new ProductServlet();
    private final ProductDAO productDAO = mock(ProductDAO.class);
    private final CategoryDAO categoryDAO = mock(CategoryDAO.class);
    private final Map<String, Object> attributes = new HashMap<>();

    @BeforeEach void setup() throws Exception {
        TestHttp.inject(servlet, "productDAO", productDAO);
        TestHttp.inject(servlet, "categoryDAO", categoryDAO);
        when(categoryDAO.getAll()).thenReturn(List.of());
        when(productDAO.getAll()).thenReturn(products(25, 20, true));
    }

    private TestHttp request(Map<String, String> parameters) throws Exception {
        var http = new TestHttp(parameters);
        when(http.request.getAttribute(anyString())).thenAnswer(call -> attributes.get(call.getArgument(0)));
        doAnswer(call -> { attributes.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(http.request).setAttribute(anyString(), any());
        return http;
    }

    private List<Product> products(int count, int stock, boolean active) {
        var products = new ArrayList<Product>();
        for (int index = 0; index < count; index++) {
            var product = new Product();
            product.setId(String.format("%024x", index + 1));
            product.setStock(stock);
            product.setActive(active);
            products.add(product);
        }
        return products;
    }

    @Test void secondPageContainsTwelveProductsAndKeepsTotalCount() throws Exception {
        var http = request(Map.of("page", "2"));
        servlet.doGet(http.request, http.response);
        assertEquals(12, ((List<?>) attributes.get("products")).size());
        assertEquals(25, attributes.get("totalProducts"));
        assertEquals(3, attributes.get("totalPages"));
        assertEquals(2, attributes.get("currentPage"));
        assertEquals(13, attributes.get("firstProduct"));
        assertEquals(24, attributes.get("lastProduct"));
        assertEquals(String.format("%024x", 13), ((Product) ((List<?>) attributes.get("products")).get(0)).getId());
        verify(http.dispatcher).forward(http.request, http.response);
    }

    @ParameterizedTest @ValueSource(strings = {"0", "-1", "invalid", "2147483648"})
    void invalidPageFallsBackToFirstPage(String page) throws Exception {
        var http = request(Map.of("page", page));
        servlet.doGet(http.request, http.response);
        assertEquals(1, attributes.get("currentPage"));
        assertEquals(12, ((List<?>) attributes.get("products")).size());
    }

    @Test void pagePastEndClampsToLastPage() throws Exception {
        var http = request(Map.of("page", "999"));
        servlet.doGet(http.request, http.response);
        assertEquals(3, attributes.get("currentPage"));
        assertEquals(1, ((List<?>) attributes.get("products")).size());
        assertEquals(25, attributes.get("firstProduct"));
        assertEquals(25, attributes.get("lastProduct"));
    }

    @Test void emptyInventoryHasAValidEmptyFirstPage() throws Exception {
        when(productDAO.getAll()).thenReturn(List.of());
        var http = request(Map.of("page", "999"));
        servlet.doGet(http.request, http.response);
        assertEquals(1, attributes.get("currentPage"));
        assertEquals(1, attributes.get("totalPages"));
        assertEquals(0, attributes.get("totalProducts"));
        assertTrue(((List<?>) attributes.get("products")).isEmpty());
    }

    @Test void lowStockFilterIsAppliedBeforePagination() throws Exception {
        var products = products(13, 3, true);
        products.addAll(products(12, 20, true));
        products.addAll(products(4, 2, false));
        when(productDAO.getAll()).thenReturn(products);
        var http = request(Map.of("page", "2", "stock", "low"));
        servlet.doGet(http.request, http.response);
        assertEquals(13, attributes.get("totalProducts"));
        assertEquals(2, attributes.get("totalPages"));
        assertEquals(1, ((List<?>) attributes.get("products")).size());
    }

    @ParameterizedTest @ValueSource(strings = {"set-active", "delete"})
    void productActionsReturnToSamePageAndFilter(String action) throws Exception {
        String id = "abcdef0123456789abcdef01";
        when(productDAO.setActive(id, true)).thenReturn(true);
        var http = request(Map.of("action", action, "id", id, "active", "on", "page", "2", "stockFilter", "low"));
        servlet.doPost(http.request, http.response);
        verify(http.response).sendRedirect("/products?page=2&stock=low#product-list");
    }

    @Test void invalidSaveKeepsPaginationAndFilter() throws Exception {
        when(productDAO.getAll()).thenReturn(products(13, 3, true));
        var http = request(Map.of("action", "insert", "name", "", "page", "2", "stockFilter", "low"));
        servlet.doPost(http.request, http.response);
        verify(http.response).setStatus(400);
        assertEquals(2, attributes.get("currentPage"));
        assertEquals(13, attributes.get("totalProducts"));
        assertEquals(true, attributes.get("lowStockOnly"));
    }

    @ParameterizedTest @ValueSource(strings = {"sua tuoi", "SỮA TƯƠI", "  sữa   tươi  "})
    void nameSearchMatchesAcrossAllPagesWithoutRequiringAccentsOrCase(String query) throws Exception {
        var products = products(25, 20, true);
        for (int index = 0; index < products.size(); index++) {
            products.get(index).setName(index < 5 ? "Rau củ" : "Sữa tươi nguyên chất");
        }
        when(productDAO.getAll()).thenReturn(products);
        var http = request(Map.of("q", query, "page", "2"));
        servlet.doGet(http.request, http.response);
        assertEquals(20, attributes.get("totalProducts"));
        assertEquals(2, attributes.get("currentPage"));
        assertEquals(8, ((List<?>) attributes.get("products")).size());
        assertEquals(query.strip(), attributes.get("searchQuery"));
    }

    @Test void searchCombinesWithLowStockFilter() throws Exception {
        var products = products(13, 3, true);
        products.addAll(products(12, 20, true));
        products.addAll(products(4, 2, false));
        products.forEach(product -> product.setName("Đậu xanh"));
        var otherProducts = products(5, 2, true);
        otherProducts.forEach(product -> product.setName("Trứng gà"));
        products.addAll(otherProducts);
        when(productDAO.getAll()).thenReturn(products);
        var http = request(Map.of("q", "dau xanh", "stock", "low", "page", "2"));
        servlet.doGet(http.request, http.response);
        assertEquals(13, attributes.get("totalProducts"));
        assertEquals(1, ((List<?>) attributes.get("products")).size());
    }

    @Test void unmatchedSearchReturnsEmptyFirstPage() throws Exception {
        var http = request(Map.of("q", "không tồn tại", "page", "9"));
        servlet.doGet(http.request, http.response);
        assertEquals(0, attributes.get("totalProducts"));
        assertEquals(1, attributes.get("currentPage"));
        assertTrue(((List<?>) attributes.get("products")).isEmpty());
    }

    @Test void actionRedirectSafelyPreservesVietnameseQueryAndSpecialCharacters() throws Exception {
        var http = request(Map.of("action", "delete", "id", "abcdef0123456789abcdef01", "page", "2", "q", "Sữa & \"tươi\""));
        servlet.doPost(http.request, http.response);
        verify(http.response).sendRedirect("/products?page=2&q=S%E1%BB%AFa+%26+%22t%C6%B0%C6%A1i%22#product-list");
    }
}
