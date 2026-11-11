<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,com.market.model.Product,com.market.model.Category,com.market.util.ViewUtil" %>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cửa hàng</title>
    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>
<body class="msm-shop">
    <jsp:include page="/WEB-INF/views/includes/header.jsp" />
    <main>
        <div class="shop-layout container">
        <aside class="shop-sidebar" aria-label="Bộ lọc cửa hàng">
                <form class="shop-search" role="search">
                    <input id="shop-search" type="search" placeholder="Tìm kiếm..." aria-label="Tìm sản phẩm" autocomplete="off">
                    <button type="submit" aria-label="Tìm kiếm"><i class="ri-search-line" aria-hidden="true"></i></button>
                </form>
                <div class="shop-filter-panel">
                    <label class="shop-filter-title" for="shop-sort">Sắp xếp theo giá</label>
                    <select id="shop-sort">
                        <option value="relevant">Phù hợp nhất</option>
                        <option value="price-asc">Giá: thấp đến cao</option>
                        <option value="price-desc">Giá: cao đến thấp</option>
                    </select>
                </div>
                <fieldset class="shop-filter-panel shop-category-filter" id="categories">
                    <legend class="shop-filter-title">Danh mục</legend>
                    <div class="shop-category-options">
                        <% if (categories != null) { for (Category category : categories) { %>
                        <label class="shop-category-option">
                            <input type="checkbox" name="category" value="<%= ViewUtil.escapeHtml(category.getId()) %>">
                            <span><%= ViewUtil.escapeHtml(category.getName()) %></span>
                        </label>
                        <% } } %>
                        <% if (categories == null || categories.isEmpty()) { %>
                        <p class="shop-category-empty">Chưa có danh mục nào.</p>
                        <% } %>
                    </div>
                </fieldset>
            </aside>
        <section class="products shop-results" id="products" aria-label="Sản phẩm">
            <div class="heading">
                <h2>Sản phẩm tươi ngon</h2>
            </div>
            <div class="product-container">
                <% if (products != null) { for (Product product : products) {
                    String imageUrl = product.getImageUrl();
                    if (imageUrl != null) imageUrl = imageUrl.trim();
                    if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                        imageUrl = request.getContextPath() + "/" + imageUrl.replaceFirst("^/+", "");
                    }
                %>
                <article class="product-box" data-category="<%= ViewUtil.escapeHtml(product.getCategoryId()) %>" data-price="<%= product.getPrice() %>">
                    <% if (product.getStock() <= 0) { %>
                    <span class="badge badge-out">Hết hàng</span>
                    <% } else { %>
                    <span class="badge badge-hot">Còn hàng</span>
                    <% } %>
                    <% if (imageUrl == null || imageUrl.isEmpty()) { %>
                    <div class="product-img product-placeholder" aria-label="Chưa có ảnh sản phẩm"><i class="ri-image-line"></i></div>
                    <% } else { %>
                    <img src="<%= ViewUtil.escapeHtml(imageUrl) %>" alt="<%= ViewUtil.escapeHtml(product.getName()) %>" class="product-img" loading="lazy" decoding="async">
                    <% } %>
                    <h3><%= ViewUtil.escapeHtml(product.getName()) %></h3>
                    <%-- Template rating matches Home; Product has no review data yet. --%>
                    <div class="rating" aria-label="Đánh giá mẫu: 4,5 trên 5">
                        <i class="ri-star-fill"></i>
                        <i class="ri-star-fill"></i>
                        <i class="ri-star-fill"></i>
                        <i class="ri-star-fill"></i>
                        <i class="ri-star-half-line"></i>
                        <span>(4.5)</span>
                    </div>
                    <div class="product-footer">
                        <span class="price"><%= String.format("%,d", product.getPrice()) %> ₫</span>
                        <form class="shop-cart-form" method="post" action="<%= request.getContextPath() %>/cart">
                            <input type="hidden" name="productId" value="<%= ViewUtil.escapeHtml(product.getId()) %>">
                            <input type="hidden" name="quantity" value="1">
                            <button class="add-to-cart" type="submit" name="action" value="add" aria-label="Thêm <%= ViewUtil.escapeHtml(product.getName()) %> vào giỏ hàng" <%= product.getStock() <= 0 ? "disabled" : "" %>><i class="ri-shopping-bag-line"></i></button>
                        </form>
                    </div>
                </article>
                <% } } %>
            </div>
            <p class="shop-empty" role="status" <%= products != null && !products.isEmpty() ? "hidden" : "" %>>Chưa có sản phẩm.</p>
        </section>
        </div>
    </main>
    <jsp:include page="/WEB-INF/views/includes/footer.jsp" />
    <jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
</body>
</html>
