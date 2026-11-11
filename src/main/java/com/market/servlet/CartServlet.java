package com.market.servlet;

import com.market.dao.ProductDAO;
import com.market.model.CartItem;
import com.market.model.Product;
import com.market.util.CartState;
import com.mongodb.MongoException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(CartServlet.class.getName());
    private ProductDAO productDAO;

    @Override
    public void init() {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        Map<String, CartItem> cart = CartState.copy(session);
        boolean restoredImage = false;
        for (CartItem item : cart.values()) {
            if (item.getImageUrl() == null || item.getImageUrl().isBlank()) {
                Product product = productDAO.getById(item.getProductId());
                if (product != null) {
                    item.setImageUrl(product.getImageUrl());
                    restoredImage = true;
                }
            }
        }
        if (restoredImage) session.setAttribute("cart", cart);
        request.setAttribute("cart", cart);
        request.setAttribute("cartTotal", cart.values().stream().mapToLong(CartItem::getSubtotal).sum());
        request.setAttribute("cartQuantity", cart.values().stream().mapToLong(CartItem::getQuantity).sum());
        request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
        String action = request.getParameter("action");
        try {
            HttpSession session = request.getSession();
            Map<String, CartItem> cart = CartState.copy(session);
            switch (action == null ? "" : action) {
                case "add", "update" -> saveItem(request, cart, "add".equals(action));
                case "remove" -> cart.remove(productId(request));
                case "clear" -> cart.clear();
                default -> throw new IllegalArgumentException("Thao tác giỏ hàng không hợp lệ.");
            }
            session.setAttribute("cart", cart);
            CartState.invalidateReview(session);
            long quantity = cart.values().stream().mapToLong(CartItem::getQuantity).sum();
            if (ajax) {
                writeJson(response, 200, new Document("success", true).append("quantity", quantity)
                        .append("message", "add".equals(action) ? "Đã thêm sản phẩm vào giỏ hàng." : "Đã cập nhật giỏ hàng."));
            } else {
                if ("add".equals(action)) session.setAttribute("cartItemAdded", Boolean.TRUE);
                response.sendRedirect("add".equals(action) ? safeReturnTo(request) : request.getContextPath() + "/cart");
            }
        } catch (IllegalArgumentException e) {
            fail(request, response, ajax, 400, e.getMessage());
        } catch (MongoException e) {
            LOG.log(Level.SEVERE, "Cart database operation failed: action=" + action, e);
            fail(request, response, ajax, 503, "Tạm thời không thể kết nối dữ liệu sản phẩm. Vui lòng thử lại sau.");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cart operation failed: action=" + action, e);
            fail(request, response, ajax, 500, "Không thể cập nhật giỏ hàng. Vui lòng thử lại.");
        }
    }

    private void saveItem(HttpServletRequest request, Map<String, CartItem> cart, boolean adding) {
        String id = productId(request);
        int quantity;
        try {
            quantity = Integer.parseInt(request.getParameter("quantity"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng không hợp lệ.");
        }
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        CartItem existing = cart.get(id);
        if (!adding && existing == null) throw new IllegalArgumentException("Sản phẩm không có trong giỏ hàng.");
        Product product = productDAO.getById(id);
        if (product == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
        if (!product.isActive()) throw new IllegalArgumentException("Sản phẩm này hiện không còn bán.");
        if (product.getStock() <= 0) throw new IllegalArgumentException("Sản phẩm đã hết hàng.");
        long newQuantity = (long) quantity + (adding && existing != null ? existing.getQuantity() : 0);
        if (newQuantity > product.getStock()) throw new IllegalArgumentException("Số lượng vượt quá số hàng còn trong kho.");
        if (product.getPrice() < 0 || product.getPrice() > Long.MAX_VALUE / newQuantity) {
            throw new IllegalArgumentException("Giá trị sản phẩm không hợp lệ. Vui lòng liên hệ cửa hàng.");
        }
        cart.put(id, new CartItem(id, product.getName(), product.getImageUrl(), product.getPrice(),
                (int) newQuantity, product.getStock()));
    }

    private String productId(HttpServletRequest request) {
        String value = request.getParameter("productId");
        if (value == null || !ObjectId.isValid(value)) throw new IllegalArgumentException("Sản phẩm không hợp lệ.");
        return new ObjectId(value).toHexString();
    }

    private String safeReturnTo(HttpServletRequest request) {
        String value = request.getParameter("returnTo");
        String context = request.getContextPath();
        return (context + "/home").equals(value) || (context + "/shop").equals(value)
                ? value : context + "/shop";
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, boolean ajax, int status, String message)
            throws ServletException, IOException {
        if (ajax) {
            writeJson(response, status, new Document("success", false).append("message", message));
        } else {
            response.setStatus(status);
            request.setAttribute("error", message);
            doGet(request, response);
        }
    }

    private void writeJson(HttpServletResponse response, int status, Document body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body.toJson());
    }
}
