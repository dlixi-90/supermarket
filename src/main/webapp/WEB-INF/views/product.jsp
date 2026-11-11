<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,java.time.format.DateTimeFormatter,com.market.model.Product,com.market.model.Category,com.market.util.ViewUtil" %>
<%!
    private String imageSource(String contextPath, String value) {
        if (value == null || value.isBlank()) return "";
        value = value.trim();
        return value.startsWith("https://") || value.startsWith("http://") ? value : contextPath + "/" + value.replaceFirst("^/+", "");
    }
%>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    Product editProduct = (Product) request.getAttribute("editProduct");
    boolean editing = editProduct != null;
    boolean submitted = Boolean.TRUE.equals(request.getAttribute("formSubmitted"));
    String name = submitted ? (String) request.getAttribute("formName") : editing ? editProduct.getName() : "";
    String price = submitted ? (String) request.getAttribute("formPrice") : editing ? String.valueOf(editProduct.getPrice()) : "";
    String stock = submitted ? (String) request.getAttribute("formStock") : editing ? String.valueOf(editProduct.getStock()) : "";
    String categoryId = submitted ? (String) request.getAttribute("formCategoryId") : editing ? editProduct.getCategoryId() : "";
    boolean active = submitted ? Boolean.TRUE.equals(request.getAttribute("formActive")) : !editing || editProduct.isActive();
    String manufactureDate = submitted ? (String) request.getAttribute("formManufactureDate") : editing && editProduct.getManufactureDate() != null ? editProduct.getManufactureDate().toString() : "";
    String expiryDate = submitted ? (String) request.getAttribute("formExpiryDate") : editing && editProduct.getExpiryDate() != null ? editProduct.getExpiryDate().toString() : "";
    String currentImage = editing ? imageSource(request.getContextPath(), editProduct.getImageUrl()) : "";
    String error = (String) request.getAttribute("error");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý sản phẩm | Growcer</title>
    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>
<body class="msm-page msm-product">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content admin-layout">
    <jsp:include page="/WEB-INF/views/includes/admin-nav.jsp" />
    <main class="container">
        <h1>Quản lý sản phẩm</h1>
        <div class="management-layout product-management-layout">
            <section class="management-list" aria-labelledby="product-list">
                <div class="section-header category-list-heading">
                    <div><h2 id="product-list">Danh sách sản phẩm</h2><span class="small"><%= products == null ? 0 : products.size() %> sản phẩm</span></div>
                    <button class="save-button category-add-button" type="button" id="add-product-button"><i class="ri-add-line" aria-hidden="true"></i>Thêm sản phẩm</button>
                </div>
                <nav class="list-filters" aria-label="Lọc sản phẩm trong kho">
                    <a href="<%= request.getContextPath() %>/products#product-list" aria-current="<%= !Boolean.TRUE.equals(request.getAttribute("lowStockOnly")) ? "page" : "false" %>">Tất cả sản phẩm</a>
                    <a href="<%= request.getContextPath() %>/products?stock=low#product-list" aria-current="<%= Boolean.TRUE.equals(request.getAttribute("lowStockOnly")) ? "page" : "false" %>">Sắp hết hàng</a>
                </nav>
                <div class="inventory-grid">
                    <% if (products != null && !products.isEmpty()) { for (Product product : products) {
                        String image = imageSource(request.getContextPath(), product.getImageUrl());
                    %>
                    <article class="inventory-card" aria-labelledby="product-title-<%= ViewUtil.escapeHtml(product.getId()) %>">
                        <div class="inventory-card-image">
                            <% if (!image.isEmpty()) { %>
                            <img src="<%= ViewUtil.escapeHtml(image) %>" alt="<%= ViewUtil.escapeHtml(product.getName()) %>" loading="lazy">
                            <% } else { %>
                            <div class="inventory-image-placeholder"><i class="ri-image-line" aria-hidden="true"></i><span>Chưa có ảnh</span></div>
                            <% } %>
                        </div>
                        <div class="inventory-card-body">
                            <p class="inventory-category"><%= ViewUtil.escapeHtml(product.getCategoryName()) %></p>
                            <h3 id="product-title-<%= ViewUtil.escapeHtml(product.getId()) %>"><%= ViewUtil.escapeHtml(product.getName()) %></h3>
                            <div class="inventory-price-row">
                                <strong class="inventory-price"><%= String.format("%,d", product.getPrice()) %> &#8363;</strong>
                                <span class="inventory-stock<%= product.getStock() <= 5 ? " inventory-stock-low" : "" %>">Tồn kho: <%= product.getStock() %></span>
                            </div>
                            <dl class="inventory-dates">
                                <div><dt>Ngày sản xuất</dt><dd><%= product.getManufactureDate() == null ? "Chưa đặt" : dateFormat.format(product.getManufactureDate()) %></dd></div>
                                <div><dt>Hạn sử dụng</dt><dd><%= product.getExpiryDate() == null ? "Chưa đặt" : dateFormat.format(product.getExpiryDate()) %></dd></div>
                            </dl>
                            <form class="product-active-form" method="post" action="<%= request.getContextPath() %>/products">
                                <input type="hidden" name="action" value="set-active">
                                <input type="hidden" name="id" value="<%= ViewUtil.escapeHtml(product.getId()) %>">
                                <% if (Boolean.TRUE.equals(request.getAttribute("lowStockOnly"))) { %><input type="hidden" name="stockFilter" value="low"><% } %>
                                <label class="product-switch" for="product-active-<%= ViewUtil.escapeHtml(product.getId()) %>">
                                    <input id="product-active-<%= ViewUtil.escapeHtml(product.getId()) %>" type="checkbox" role="switch" name="active" aria-label="Hiển thị trong cửa hàng: <%= ViewUtil.escapeHtml(product.getName()) %>" <%= product.isActive() ? "checked" : "" %>>
                                    <span class="product-switch-track" aria-hidden="true"></span>
                                <span>Hiển thị trong cửa hàng</span>
                                </label>
                            </form>
                            <div class="inventory-card-actions">
                                <button class="button secondary product-edit-button" type="button"
                                    data-id="<%= ViewUtil.escapeHtml(product.getId()) %>"
                                    data-name="<%= ViewUtil.escapeHtml(product.getName()) %>"
                                    data-price="<%= product.getPrice() %>"
                                    data-stock="<%= product.getStock() %>"
                                    data-category-id="<%= ViewUtil.escapeHtml(product.getCategoryId()) %>"
                                    data-manufacture-date="<%= product.getManufactureDate() == null ? "" : product.getManufactureDate() %>"
                                    data-expiry-date="<%= product.getExpiryDate() == null ? "" : product.getExpiryDate() %>"
                                    data-image="<%= ViewUtil.escapeHtml(image) %>"
                                    data-active="<%= product.isActive() %>"><i class="ri-edit-line" aria-hidden="true"></i>Sửa</button>
                                <form method="post" action="<%= request.getContextPath() %>/products" onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm này không?');">
                                    <input type="hidden" name="id" value="<%= ViewUtil.escapeHtml(product.getId()) %>">
                                    <button class="delete-button" type="submit" name="action" value="delete"><i class="ri-delete-bin-line" aria-hidden="true"></i>Xóa</button>
                                </form>
                            </div>
                        </div>
                    </article>
                    <% } } else { %><p class="inventory-empty">Chưa có sản phẩm nào.</p><% } %>
                </div>
            </section>
        </div>
        <dialog class="category-dialog product-dialog" id="product-dialog" aria-labelledby="product-form-title" data-open-on-load="<%= error != null || editing ? "true" : "false" %>">
            <div class="category-dialog-heading">
                <h2 id="product-form-title"><%= editing ? "Cập nhật sản phẩm" : "Thêm sản phẩm" %></h2>
                <button class="category-dialog-close" type="button" aria-label="Đóng cửa sổ">×</button>
            </div>
            <aside class="form-box management-form">
                <% if (error != null) { %><div class="error" role="alert"><%= ViewUtil.escapeHtml(error) %><p class="small">Nếu đã chọn ảnh, vui lòng chọn lại trước khi gửi biểu mẫu.</p></div><% } %>
                <form id="product-form" method="post" action="<%= request.getContextPath() %>/products" enctype="multipart/form-data">
                    <input id="product-edit-id" type="hidden" name="id" value="<%= editing ? ViewUtil.escapeHtml(editProduct.getId()) : "" %>">
                    <label for="product-name">Tên sản phẩm</label>
                    <input id="product-name" type="text" name="name" required value="<%= ViewUtil.escapeHtml(name) %>">
                    <div class="management-field-row">
                        <div><label for="product-price">Giá (VNĐ)</label><input id="product-price" type="number" name="price" min="0" step="1" required value="<%= ViewUtil.escapeHtml(price) %>"></div>
                        <div><label for="product-stock">Tồn kho</label><input id="product-stock" type="number" name="stock" min="0" step="1" required value="<%= ViewUtil.escapeHtml(stock) %>"></div>
                    </div>
                    <label for="product-manufacture-date">Ngày sản xuất</label>
                    <input id="product-manufacture-date" type="date" name="manufactureDate" min="0001-01-01" max="9999-12-31" value="<%= ViewUtil.escapeHtml(manufactureDate) %>">
                    <label for="product-expiry-date">Hạn sử dụng</label>
                    <input id="product-expiry-date" type="date" name="expiryDate" min="0001-01-01" max="9999-12-31" value="<%= ViewUtil.escapeHtml(expiryDate) %>">
                    <label for="product-category">Danh mục</label>
                    <select id="product-category" name="categoryId" required>
                        <option value="">Chọn danh mục</option>
                        <% if (categories != null) { for (Category category : categories) { %>
                        <option value="<%= ViewUtil.escapeHtml(category.getId()) %>" <%= category.getId().equals(categoryId) ? "selected" : "" %>><%= ViewUtil.escapeHtml(category.getName()) %></option>
                        <% } } %>
                    </select>
                    <label for="product-image">Ảnh sản phẩm</label>
                    <label class="upload-preview" for="product-image">
                        <img id="product-image-preview" alt="Ảnh xem trước sản phẩm" data-current-src="<%= ViewUtil.escapeHtml(currentImage) %>" <%= currentImage.isEmpty() ? "hidden" : "src=\"" + ViewUtil.escapeHtml(currentImage) + "\"" %>>
                        <span id="product-image-placeholder" <%= currentImage.isEmpty() ? "" : "hidden" %>><i class="ri-image-add-line" aria-hidden="true"></i>Chọn ảnh</span>
                    </label>
                    <input id="product-image" type="file" name="image" accept="image/jpeg,image/png" aria-describedby="product-image-hint product-image-error">
                    <p id="product-image-hint" class="hint">Ảnh JPG hoặc PNG · Tối đa 5 MB. Để trống nếu muốn giữ ảnh hiện tại.</p>
                    <p id="product-image-error" class="upload-error" role="status"></p>
                    <label class="product-switch" for="product-active">
                        <input id="product-active" type="checkbox" role="switch" name="active" <%= active ? "checked" : "" %>>
                        <span class="product-switch-track" aria-hidden="true"></span>
                        <span>Hiển thị trong cửa hàng</span>
                    </label>
                    <div class="management-form-actions">
                        <button class="primary" id="product-submit" type="submit" name="action" value="<%= editing ? "update" : "insert" %>"><%= editing ? "Cập nhật sản phẩm" : "Thêm sản phẩm" %></button>
                        <button class="button secondary category-dialog-cancel" type="button">Hủy</button>
                    </div>
                </form>
            </aside>
        </dialog>
    </main>
</div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
<jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>
</html>
