package com.market.util;

import com.market.model.CartItem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;

public final class CartState {
    private CartState() { }

    /** Work on a copy so rendering another page cannot iterate a changing map. */
    public static Map<String, CartItem> copy(HttpSession session) {
        Map<String, CartItem> copy = new LinkedHashMap<>();
        Object value = session == null ? null : session.getAttribute("cart");
        if (value instanceof Map<?, ?>) {
            for (Object entry : ((Map<?, ?>) value).values()) {
                if (entry instanceof CartItem item) {
                    copy.put(item.getProductId(), new CartItem(item.getProductId(), item.getProductName(),
                            item.getImageUrl(), item.getPrice(), item.getQuantity(), item.getStock()));
                }
            }
        }
        return copy;
    }

    public static String signature(Map<String, CartItem> cart) {
        return cart.values().stream()
                .map(item -> item.getProductId() + ":" + item.getQuantity() + ":" + item.getPrice())
                .sorted().collect(Collectors.joining("|"));
    }

    public static void invalidateReview(HttpSession session) {
        if (session == null) return;
        session.removeAttribute("checkoutToken");
        session.removeAttribute("checkoutCartSignature");
    }
}
