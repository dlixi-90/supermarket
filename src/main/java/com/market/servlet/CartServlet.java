package com.market.servlet;

import com.market.dao.ProductDAO;
import com.market.model.CartItem;
import com.market.model.Product;

import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.types.ObjectId;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private ProductDAO productDAO;


    @Override
    public void init() {

        productDAO =
                new ProductDAO();
    }


    // =====================================
    // SHOW CART
    // =====================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session =
                request.getSession(false);

        if (session != null
                && session.getAttribute("pendingCartProductId") != null
                && session.getAttribute("pendingCartQuantity") != null) {
            String pendingProductId = (String) session.getAttribute("pendingCartProductId");
            String pendingQuantity = (String) session.getAttribute("pendingCartQuantity");
            session.removeAttribute("pendingCartProductId");
            session.removeAttribute("pendingCartQuantity");

            try {
                addToCart(pendingProductId, pendingQuantity, session);
                session.setAttribute("cartItemAdded", Boolean.TRUE);
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            } catch (IllegalArgumentException e) {
                request.setAttribute("error", e.getMessage());
            }
        }


        Map<String, CartItem> cart =
                getCart(session);

        if (session != null) {
            for (CartItem item : cart.values()) {
                if (item.getImageUrl() == null
                        || item.getImageUrl().isBlank()) {
                    Product product = productDAO.getById(item.getProductId());
                    if (product != null) {
                        item.setImageUrl(product.getImageUrl());
                    }
                }
            }
            session.setAttribute("cart", cart);
        }


        // =================================
        // TOTAL
        // =================================

        long total =
                calculateTotal(cart);


        int totalQuantity =
                calculateTotalQuantity(cart);


        request.setAttribute(
                "cart",
                cart
        );

        request.setAttribute(
                "cartTotal",
                total
        );

        request.setAttribute(
                "cartQuantity",
                totalQuantity
        );


        request
                .getRequestDispatcher(
                        "/WEB-INF/views/cart.jsp"
                )
                .forward(
                        request,
                        response
                );
    }


    // =====================================
    // CART ACTIONS
    // =====================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        request.setCharacterEncoding(
                "UTF-8"
        );


        String action =
                request.getParameter(
                        "action"
                );

        boolean ajaxRequest = "XMLHttpRequest".equalsIgnoreCase(
                request.getHeader("X-Requested-With")
        );


        try {

            if ("add".equals(action)) {

                addToCart(request);

                if (ajaxRequest) {
                    HttpSession session = request.getSession(false);
                    long quantity = calculateTotalQuantity(getCart(session));
                    response.setContentType("application/json; charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                            "{\"success\":true,\"quantity\":" + quantity
                                    + ",\"message\":\"Đã thêm sản phẩm vào giỏ hàng.\"}"
                    );
                    return;
                }

                request.getSession().setAttribute("cartItemAdded", Boolean.TRUE);
                response.sendRedirect(getSafeReturnTo(request));
                return;

            } else if ("update".equals(action)) {

                updateCart(request);

            } else if ("remove".equals(action)) {

                removeFromCart(request);

            } else if ("clear".equals(action)) {

                clearCart(request);
            }


            response.sendRedirect(
                    request.getContextPath()
                            + "/cart"
            );


        } catch (IllegalArgumentException e) {

            if (ajaxRequest) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json; charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\""
                                + escapeJson(e.getMessage()) + "\"}"
                );
                return;
            }

            request.setAttribute(
                    "error",
                    e.getMessage()
            );


            doGet(
                    request,
                    response
            );


        } catch (Exception e) {

            if (ajaxRequest) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json; charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Không thể thêm sản phẩm vào giỏ hàng. Vui lòng thử lại.\"}"
                );
                return;
            }

            throw new ServletException(
                    "Thao tác giỏ hàng thất bại",
                    e
            );
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private String getSafeReturnTo(HttpServletRequest request) {
        String returnTo = request.getParameter("returnTo");
        String contextPath = request.getContextPath();
        String requiredPrefix = contextPath + "/";

        if (returnTo != null
                && returnTo.startsWith(requiredPrefix)
                && !returnTo.startsWith("//")
                && !returnTo.contains("\r")
                && !returnTo.contains("\n")) {
            return returnTo;
        }

        return contextPath + "/shop";
    }


    // =====================================
    // ADD TO CART
    // =====================================
    private void addToCart(
            HttpServletRequest request) {

        addToCart(
                request.getParameter("productId"),
                request.getParameter("quantity"),
                request.getSession()
        );
    }

    private void addToCart(
            String productId,
            String quantityString,
            HttpSession session) {

        if (productId == null
                || !ObjectId.isValid(
                        productId
                )) {

            throw new IllegalArgumentException(
                    "Sản phẩm không hợp lệ."
            );
        }


        int quantity =
                parseQuantity(
                        quantityString
                );


        // =================================
        // LOAD PRODUCT FROM DATABASE
        //
        // Không lấy price/name từ browser
        // =================================

        Product product =
                productDAO.getById(
                        productId
                );


        if (product == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy sản phẩm."
            );
        }


        if (!product.isActive()) {

            throw new IllegalArgumentException(
                    "Sản phẩm này hiện không còn bán."
            );
        }


        if (product.getStock() <= 0) {

            throw new IllegalArgumentException(
                    "Sản phẩm đã hết hàng."
            );
        }


        Map<String, CartItem> cart =
                getCart(session);


        CartItem existing =
                cart.get(
                        productId
                );


        // =================================
        // ALREADY IN CART
        // =================================

        if (existing != null) {

            int newQuantity =
                    existing.getQuantity()
                    + quantity;


            if (newQuantity
                    > product.getStock()) {

                throw new IllegalArgumentException(
                        "Số lượng vượt quá số hàng còn trong kho."
                );
            }


            existing.setQuantity(
                    newQuantity
            );


            /*
             * Refresh các dữ liệu có thể
             * đã thay đổi.
             */
            existing.setProductName(
                    product.getName()
            );

            existing.setPrice(
                    product.getPrice()
            );

            existing.setStock(
                    product.getStock()
            );

            existing.setImageUrl(
                    product.getImageUrl()
            );


        } else {

            // =================================
            // NEW ITEM
            // =================================

            if (quantity
                    > product.getStock()) {

                throw new IllegalArgumentException(
                        "Số lượng vượt quá số hàng còn trong kho."
                );
            }


            CartItem item =
                    new CartItem(
                            product.getId(),
                            product.getName(),
                            product.getImageUrl(),
                            product.getPrice(),
                            quantity,
                            product.getStock()
                    );


            cart.put(
                    productId,
                    item
            );
        }


        session.setAttribute(
                "cart",
                cart
        );
    }


    // =====================================
    // UPDATE QUANTITY
    // =====================================
    private void updateCart(
            HttpServletRequest request) {


        String productId =
                request.getParameter(
                        "productId"
                );


        String quantityString =
                request.getParameter(
                        "quantity"
                );


        if (productId == null
                || !ObjectId.isValid(
                        productId
                )) {

            throw new IllegalArgumentException(
                    "Sản phẩm không hợp lệ."
            );
        }


        int quantity =
                parseQuantity(
                        quantityString
                );


        HttpSession session =
                request.getSession();


        Map<String, CartItem> cart =
                getCart(session);


        CartItem item =
                cart.get(
                        productId
                );


        if (item == null) {

            throw new IllegalArgumentException(
                    "Sản phẩm không có trong giỏ hàng."
            );
        }


        // =================================
        // LOAD LATEST PRODUCT
        // =================================

        Product product =
                productDAO.getById(
                        productId
                );


        if (product == null) {

            cart.remove(productId);

            throw new IllegalArgumentException(
                    "Sản phẩm không còn tồn tại."
            );
        }


        if (!product.isActive()) {

            cart.remove(productId);

            throw new IllegalArgumentException(
                    "Sản phẩm này hiện không còn bán."
            );
        }


        if (quantity
                > product.getStock()) {

            throw new IllegalArgumentException(
                    "Số lượng vượt quá số hàng còn trong kho."
            );
        }


        // Refresh product information

        item.setQuantity(
                quantity
        );

        item.setProductName(
                product.getName()
        );

        item.setPrice(
                product.getPrice()
        );

        item.setStock(
                product.getStock()
        );

        item.setImageUrl(
                product.getImageUrl()
        );


        session.setAttribute(
                "cart",
                cart
        );
    }


    // =====================================
    // REMOVE
    // =====================================
    private void removeFromCart(
            HttpServletRequest request) {


        String productId =
                request.getParameter(
                        "productId"
                );


        HttpSession session =
                request.getSession();


        Map<String, CartItem> cart =
                getCart(session);


        if (productId != null) {

            cart.remove(
                    productId
            );
        }


        session.setAttribute(
                "cart",
                cart
        );
    }


    // =====================================
    // CLEAR CART
    // =====================================
    private void clearCart(
            HttpServletRequest request) {


        HttpSession session =
                request.getSession();


        session.removeAttribute(
                "cart"
        );
    }


    // =====================================
    // GET CART FROM SESSION
    // =====================================
    @SuppressWarnings("unchecked")
    private Map<String, CartItem> getCart(
            HttpSession session) {


        if (session == null) {

            return new LinkedHashMap<>();
        }


        Map<String, CartItem> cart =
                (Map<String, CartItem>)
                session.getAttribute(
                        "cart"
                );


        if (cart == null) {

            cart =
                    new LinkedHashMap<>();


            session.setAttribute(
                    "cart",
                    cart
            );
        }


        return cart;
    }


    // =====================================
    // PARSE QUANTITY
    // =====================================
    private int parseQuantity(
            String value) {


        try {

            int quantity =
                    Integer.parseInt(
                            value
                    );


            if (quantity <= 0) {

                throw new IllegalArgumentException(
                        "Số lượng phải lớn hơn 0."
                );
            }


            return quantity;


        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Số lượng không hợp lệ."
            );
        }
    }


    // =====================================
    // TOTAL PRICE
    // =====================================
    private long calculateTotal(
            Map<String, CartItem> cart) {


        long total = 0;


        for (CartItem item :
                cart.values()) {

            total +=
                    item.getSubtotal();
        }


        return total;
    }


    // =====================================
    // TOTAL NUMBER OF ITEMS
    // =====================================
    private int calculateTotalQuantity(
            Map<String, CartItem> cart) {


        int total = 0;


        for (CartItem item :
                cart.values()) {

            total +=
                    item.getQuantity();
        }


        return total;
    }
}
