package com.market.servlet;

import com.market.dao.CategoryDAO;
import com.market.dao.ProductDAO;
import com.market.model.Category;
import com.market.model.Product;
import com.market.util.ProductImageStorage;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.bson.types.ObjectId;

@WebServlet("/products")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5L * 1024 * 1024, maxRequestSize = 6L * 1024 * 1024)
public class ProductServlet extends HttpServlet {
    private static final int PAGE_SIZE = 12;
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;

    @Override
    public void init() {
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("edit".equals(request.getParameter("action"))) {
            request.setAttribute("editProduct", productDAO.getById(request.getParameter("id")));
        }
        request.setAttribute("lowStockOnly", "low".equals(request.getParameter("stock")));
        showPage(request, response);
    }

    private void showPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Category> categories = categoryDAO.getAll();
            Map<String, String> categoryNames = new HashMap<>();
            for (Category category : categories) categoryNames.put(category.getId(), category.getName());
            List<Product> products = productDAO.getAll();
            String searchQuery = searchQuery(request);
            String normalizedQuery = normalizeSearch(searchQuery);
            if (!normalizedQuery.isEmpty()) {
                products = products.stream()
                        .filter(product -> normalizeSearch(product.getName()).contains(normalizedQuery)).toList();
            }
            if (Boolean.TRUE.equals(request.getAttribute("lowStockOnly"))) {
                products = products.stream().filter(product -> product.isActive() && product.getStock() <= 5).toList();
            }
            int totalProducts = products.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalProducts / PAGE_SIZE));
            int currentPage = Math.min(parsePage(request.getParameter("page")), totalPages);
            int fromIndex = (currentPage - 1) * PAGE_SIZE;
            int toIndex = Math.min(fromIndex + PAGE_SIZE, totalProducts);
            products = products.subList(fromIndex, toIndex);
            for (Product product : products) product.setCategoryName(categoryNames.getOrDefault(product.getCategoryId(), "Không rõ danh mục"));
            request.setAttribute("categories", categories);
            request.setAttribute("products", products);
            request.setAttribute("searchQuery", searchQuery);
            request.setAttribute("totalProducts", totalProducts);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("firstProduct", totalProducts == 0 ? 0 : fromIndex + 1);
            request.setAttribute("lastProduct", toIndex);
            request.getRequestDispatcher("/WEB-INF/views/product.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load products", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        boolean multipart = request.getContentType() != null && request.getContentType().toLowerCase(java.util.Locale.ROOT).startsWith("multipart/form-data");
        if (multipart) {
            try {
                request.getParts();
            } catch (IllegalStateException e) {
                response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                request.setAttribute("error", "Ảnh tải lên quá lớn. Vui lòng chọn ảnh JPG hoặc PNG có dung lượng tối đa 5 MB.");
                showPage(request, response);
                return;
            } catch (ServletException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                request.setAttribute("error", "Không thể đọc ảnh đã tải lên. Vui lòng chọn lại ảnh.");
                showPage(request, response);
                return;
            }
        }
        request.setAttribute("lowStockOnly", "low".equals(request.getParameter("stockFilter")));
        try {
            String action = request.getParameter("action");
            if ("insert".equals(action) || "update".equals(action)) {
                saveProduct(request, "update".equals(action), multipart);
            } else if ("set-active".equals(action)) {
                String id = request.getParameter("id");
                if (id == null || !ObjectId.isValid(id)) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
                String activeValue = request.getParameter("active");
                if (activeValue != null && !"on".equals(activeValue)) throw new IllegalArgumentException("Trạng thái sản phẩm không hợp lệ.");
                if (!productDAO.setActive(id, "on".equals(activeValue))) {
                    throw new IllegalArgumentException("Sản phẩm không còn tồn tại. Vui lòng tải lại trang và thử lại.");
                }
            } else if ("delete".equals(action)) {
                String id = request.getParameter("id");
                if (id == null || !ObjectId.isValid(id)) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
                productDAO.delete(id);
            } else {
                throw new IllegalArgumentException("Thao tác sản phẩm không hợp lệ.");
            }
            response.sendRedirect(request.getContextPath() + "/products?page=" + parsePage(request.getParameter("page"))
                    + (Boolean.TRUE.equals(request.getAttribute("lowStockOnly")) ? "&stock=low" : "")
                    + (searchQuery(request).isEmpty() ? "" : "&q=" + URLEncoder.encode(searchQuery(request), StandardCharsets.UTF_8))
                    + "#product-list");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", e.getMessage());
            showPage(request, response);
        } catch (Exception e) {
            throw new ServletException("Thao tác sản phẩm thất bại", e);
        }
    }

    private String searchQuery(HttpServletRequest request) {
        String value = request.getParameter("q");
        return value == null ? "" : value.strip();
    }

    private String normalizeSearch(String value) {
        return value == null ? "" : Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('đ', 'd').strip().replaceAll("\\s+", " ");
    }

    private int parsePage(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void saveProduct(HttpServletRequest request, boolean editing, boolean multipart) throws IOException, ServletException {
        Product existing = null;
        String id = request.getParameter("id");
        if (editing) {
            if (id == null || !ObjectId.isValid(id)) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
            existing = productDAO.getById(id);
            if (existing == null) throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
            request.setAttribute("editProduct", existing);
        }
        String name = request.getParameter("name");
        String priceText = request.getParameter("price");
        String stockText = request.getParameter("stock");
        String categoryId = request.getParameter("categoryId");
        String manufactureDateText = request.getParameter("manufactureDate");
        String expiryDateText = request.getParameter("expiryDate");
        boolean active = "on".equals(request.getParameter("active"));
        request.setAttribute("formSubmitted", true);
        request.setAttribute("formName", name);
        request.setAttribute("formPrice", priceText);
        request.setAttribute("formStock", stockText);
        request.setAttribute("formCategoryId", categoryId);
        request.setAttribute("formActive", active);
        request.setAttribute("formManufactureDate", manufactureDateText);
        request.setAttribute("formExpiryDate", expiryDateText);

        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm.");
        long price = parsePrice(priceText);
        int stock = parseStock(stockText);
        LocalDate manufactureDate = parseProductDate(manufactureDateText, "Manufacture date");
        LocalDate expiryDate = parseProductDate(expiryDateText, "Expiry date");
        if (manufactureDate != null && expiryDate != null && expiryDate.isBefore(manufactureDate)) {
            throw new IllegalArgumentException("Hạn sử dụng không thể trước ngày sản xuất.");
        }
        if (categoryId == null || !ObjectId.isValid(categoryId) || categoryDAO.getById(categoryId) == null) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục hợp lệ.");
        }

        Part imagePart = multipart ? request.getPart("image") : null;
        String uploadedImage = ProductImageStorage.save(imagePart);
        String imageUrl = uploadedImage != null ? uploadedImage : existing == null ? "" : existing.getImageUrl();
        // Description is no longer editable; keep existing data when updating.
        String description = existing == null ? "" : existing.getDescription();
        Product product = new Product(id, name.trim(), description, price, stock, categoryId, imageUrl, active);
        product.setManufactureDate(manufactureDate);
        product.setExpiryDate(expiryDate);
        if (editing) {
            if (!productDAO.update(product)) {
                throw new IllegalArgumentException("Sản phẩm không còn tồn tại. Vui lòng tải lại trang và thử lại.");
            }
        } else {
            productDAO.insert(product);
        }
    }

    private LocalDate parseProductDate(String value, String label) {
        if (value == null || value.isBlank()) return null;
        try {
            if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw new IllegalArgumentException(label + " không đúng định dạng ngày.");
            LocalDate date = LocalDate.parse(value);
            if (date.getYear() < 1) throw new IllegalArgumentException(label + " không hợp lệ.");
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(label + " không hợp lệ.");
        }
    }

    private long parsePrice(String value) {
        try {
            long price = Long.parseLong(value);
            if (price < 0) throw new IllegalArgumentException("Giá sản phẩm không thể là số âm.");
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá sản phẩm phải là một số hợp lệ.");
        }
    }

    private int parseStock(String value) {
        try {
            int stock = Integer.parseInt(value);
            if (stock < 0) throw new IllegalArgumentException("Số lượng tồn kho không thể là số âm.");
            return stock;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số lượng tồn kho phải là một số hợp lệ.");
        }
    }
}
