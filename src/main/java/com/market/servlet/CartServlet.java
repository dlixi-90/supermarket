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


        HttpSession session =
                request.getSession(false);


        Map<String, CartItem> cart =
                getCart(session);


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


        try {

            if ("add".equals(action)) {

                addToCart(request);
                request.getSession().setAttribute("cartItemAdded", Boolean.TRUE);

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

            request.setAttribute(
                    "error",
                    e.getMessage()
            );


            doGet(
                    request,
                    response
            );


        } catch (Exception e) {

            throw new ServletException(
                    "Thao tác giỏ hàng thất bại",
                    e
            );
        }
    }


    // =====================================
    // ADD TO CART
    // =====================================
    private void addToCart(
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


        HttpSession session =
                request.getSession();


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
