package com.market.servlet;

import com.market.dao.ProductDAO;

import com.market.model.CartItem;
import com.market.model.CheckoutData;
import com.market.model.Product;
import com.market.util.CartState;
import java.util.UUID;

import java.io.IOException;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private ProductDAO productDAO;

    @Override
    public void init() {

        productDAO
                = new ProductDAO();
    }

    // =====================================
    // SHOW CHECKOUT PAGE
    // =====================================
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        CartState.invalidateReview(session);

        String checkoutError
                = (String) session.getAttribute(
                        "checkoutError"
                );

        if (checkoutError != null) {

            request.setAttribute(
                    "error",
                    checkoutError
            );

            session.removeAttribute(
                    "checkoutError"
            );
        }

        Map<String, CartItem> cart
                = getCart(session);

        // =================================
        // EMPTY CART
        // =================================
        if (cart.isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/cart"
            );

            return;
        }

        // =================================
        // VALIDATE CART AGAIN
        // =================================
        String cartError
                = refreshAndValidateCart(
                        cart
                );

        session.setAttribute(
                "cart",
                cart
        );

        if (cartError != null) {

            request.setAttribute(
                    "error",
                    cartError
            );
        }

        // =================================
        // DEFAULT RECIPIENT NAME
        // =================================
        CheckoutData savedData
                = (CheckoutData) session.getAttribute(
                        "checkoutData"
                );

        if (savedData != null) {

            request.setAttribute(
                    "recipientName",
                    savedData
                            .getRecipientName()
            );

            request.setAttribute(
                    "phone",
                    savedData.getPhone()
            );

            request.setAttribute(
                    "shippingAddress",
                    savedData
                            .getShippingAddress()
            );

            request.setAttribute(
                    "paymentMethod",
                    savedData
                            .getPaymentMethod()
            );

        } else {

            String userName
                    = (String) session.getAttribute(
                            "userName"
                    );

            request.setAttribute(
                    "recipientName",
                    userName
            );
        }

        showCheckout(
                request,
                response,
                cart
        );
    }

    // =====================================
    // VALIDATE CHECKOUT FORM
    // =====================================
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        HttpSession session
                = request.getSession(false);

        CartState.invalidateReview(session);

        Map<String, CartItem> cart
                = getCart(session);

        // =================================
        // EMPTY CART
        // =================================
        if (cart.isEmpty()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/cart"
            );

            return;
        }

        String recipientName
                = normalize(
                        request.getParameter(
                                "recipientName"
                        )
                );

        String phone
                = normalize(
                        request.getParameter(
                                "phone"
                        )
                );

        String shippingAddress
                = normalize(
                        request.getParameter(
                                "shippingAddress"
                        )
                );

        String paymentMethod
                = normalize(
                        request.getParameter(
                                "paymentMethod"
                        )
                );

        // =================================
        // KEEP FORM DATA
        // =================================
        request.setAttribute(
                "recipientName",
                recipientName
        );

        request.setAttribute(
                "phone",
                phone
        );

        request.setAttribute(
                "shippingAddress",
                shippingAddress
        );

        request.setAttribute(
                "paymentMethod",
                paymentMethod
        );

        // =================================
        // VALIDATE FORM
        // =================================
        if (recipientName.isEmpty()) {

            showError(
                    request,
                    response,
                    cart,
                    "Vui lòng nhập tên người nhận."
            );

            return;
        }

        if (recipientName.length() > 100) {

            showError(
                    request,
                    response,
                    cart,
                    "Tên người nhận quá dài."
            );

            return;
        }

        if (phone.isEmpty()) {

            showError(
                    request,
                    response,
                    cart,
                    "Vui lòng nhập số điện thoại."
            );

            return;
        }

        if (!isValidPhone(phone)) {

            showError(
                    request,
                    response,
                    cart,
                    "Số điện thoại không hợp lệ."
            );

            return;
        }

        if (shippingAddress.isEmpty()) {

            showError(
                    request,
                    response,
                    cart,
                    "Vui lòng nhập địa chỉ giao hàng."
            );

            return;
        }

        if (shippingAddress.length() > 500) {

            showError(
                    request,
                    response,
                    cart,
                    "Địa chỉ giao hàng quá dài."
            );

            return;
        }

        paymentMethod
                = normalizePaymentMethod(
                        paymentMethod
                );

        if (paymentMethod == null) {

            showError(
                    request,
                    response,
                    cart,
                    "Phương thức thanh toán không hợp lệ."
            );

            return;
        }

        request.setAttribute(
                "paymentMethod",
                paymentMethod
        );

        // =================================
        // RELOAD PRODUCTS FROM DATABASE
        //
        // Không tin price / stock
        // trong Session.
        // =================================
        String cartError
                = refreshAndValidateCart(
                        cart
                );

        session.setAttribute(
                "cart",
                cart
        );

        if (cartError != null) {

            showError(
                    request,
                    response,
                    cart,
                    cartError
            );

            return;
        }

        // =================================
        // SAVE CHECKOUT DATA IN SESSION
        // =================================
        CheckoutData checkoutData
                = new CheckoutData(
                        recipientName,
                        phone,
                        shippingAddress,
                        paymentMethod
                );

        session.setAttribute(
                "checkoutData",
                checkoutData
        );

        // =================================
        // READY FOR ORDER
        // =================================
        request.setAttribute(
                "checkoutReady",
                true
        );

        String checkoutToken = UUID.randomUUID().toString();
        session.setAttribute("checkoutToken", checkoutToken);
        session.setAttribute("checkoutCartSignature", CartState.signature(cart));
        request.setAttribute("checkoutToken", checkoutToken);

        showCheckout(
                request,
                response,
                cart
        );
    }

    // =====================================
    // REFRESH + VALIDATE CART
    // =====================================
    private String refreshAndValidateCart(
            Map<String, CartItem> cart) {

        for (CartItem item
                : cart.values()) {

            Product product
                    = productDAO.getById(
                            item.getProductId()
                    );

            // PRODUCT DELETED
            if (product == null) {

                return "Sản phẩm \""
                        + item.getProductName()
                        + "\" không còn tồn tại.";
            }

            // PRODUCT INACTIVE
            if (!product.isActive()) {

                return "Sản phẩm \""
                        + product.getName()
                        + "\" hiện không còn được bán.";
            }

            // REFRESH DATA
            item.setProductName(
                    product.getName()
            );

            item.setImageUrl(
                    product.getImageUrl()
            );

            item.setPrice(
                    product.getPrice()
            );

            item.setStock(
                    product.getStock()
            );

            // OUT OF STOCK
            if (product.getStock() <= 0) {

                return "Sản phẩm \""
                        + product.getName()
                        + "\" đã hết hàng.";
            }

            // NOT ENOUGH STOCK
            if (item.getQuantity()
                    > product.getStock()) {

                return "Sản phẩm \""
                        + product.getName()
                        + "\" hiện chỉ còn "
                        + product.getStock()
                        + " sản phẩm trong kho.";
            }
        }

        return null;
    }

    // =====================================
    // SHOW CHECKOUT
    // =====================================
    private void showCheckout(
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, CartItem> cart)
            throws ServletException, IOException {

        long total
                = calculateTotal(
                        cart
                );

        long quantity
                = calculateQuantity(
                        cart
                );

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
                quantity
        );

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/checkout.jsp"
                )
                .forward(
                        request,
                        response
                );
    }

    // =====================================
    // ERROR
    // =====================================
    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, CartItem> cart,
            String message)
            throws ServletException, IOException {

        request.setAttribute(
                "error",
                message
        );

        showCheckout(
                request,
                response,
                cart
        );
    }

    // =====================================
    // GET CART
    // =====================================
    private Map<String, CartItem> getCart(
            HttpSession session) {
        return CartState.copy(session);
    }

    // =====================================
    // TOTAL
    // =====================================
    private long calculateTotal(
            Map<String, CartItem> cart) {

        long total = 0;

        for (CartItem item
                : cart.values()) {

            total
                    += item.getSubtotal();
        }

        return total;
    }

    // =====================================
    // QUANTITY
    // =====================================
    private long calculateQuantity(
            Map<String, CartItem> cart) {

        long total = 0;

        for (CartItem item
                : cart.values()) {

            total
                    += item.getQuantity();
        }

        return total;
    }

    // =====================================
    // NORMALIZE
    // =====================================
    private String normalize(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    // =====================================
    // PHONE
    // =====================================
    private boolean isValidPhone(
            String phone) {


        /*
         * Cho phép:
         *
         * 0901234567
         * +84901234567
         * 090 123 4567
         * (090) 123-4567
         */
        if (phone.length() < 8
                || phone.length() > 20) {

            return false;
        }

        return phone.matches("^\\+?[0-9() .-]+$")
                && phone.replaceAll("[^0-9]", "").matches("[0-9]{8,15}");
    }

    // =====================================
    // PAYMENT METHOD
    // =====================================
    private String normalizePaymentMethod(
            String method) {

        if ("COD".equalsIgnoreCase(
                method)) {

            return "COD";
        }


        /*
         * Chuẩn bị sẵn để sau
         * có thể triển khai QR.
         */
        if ("QR".equalsIgnoreCase(
                method)) {

            return "QR";
        }

        return null;
    }
}
