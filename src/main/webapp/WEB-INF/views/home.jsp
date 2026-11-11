<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,java.util.Map,com.market.model.Category,com.market.model.Product,com.market.util.ViewUtil" %>
<%
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    List<Product> products = (List<Product>) request.getAttribute("products");
    Map<String, Integer> categoryCounts = (Map<String, Integer>) request.getAttribute("categoryCounts");
    Map<String, Integer> categoryColors = new java.util.HashMap<>();
    if (categories != null) {
        for (int i = 0; i < categories.size(); i++) {
            categoryColors.put(categories.get(i).getId(), i % 6 + 1);
        }
    }
    int homeCategoryLimit = categories == null ? 0 : categories.size();
    int homeFilterCategoryLimit = 8;
    int homeProductLimit = 10;
%>
<!doctype html>
<html lang="vi">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Growcer</title>
    <!-- Link to CSS -->

    <!-- Remix Icons -->

    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
  </head>
  <body data-page="home">
    <!-- Header -->
    <jsp:include page="/WEB-INF/views/includes/header.jsp" />
    <!-- Home -->
    <section class="home container" id="home">
      <!-- Home Image -->
      <img
        src="<%= request.getContextPath() %>/images/hero1.png"
        alt=""
        class="home-img"
        fetchpriority="high"
        decoding="async"
      />
      <div class="home-text">
        <h1>
          Đừng bỏ lỡ ưu đãi<br />
          thực phẩm hấp dẫn!
        </h1>
        <p>Tiết kiệm đến 50% cho đơn hàng đầu tiên</p>
        <a href="<%= request.getContextPath() %>/shop" class="btn">
          <i class="ri-shopping-bag-line"></i>
          Mua ngay
        </a>
      </div>
    </section>
    <!-- Categories -->
    <section class="categories container" id="categories">
      <!-- Heading -->
      <div class="heading home-category-heading">
        <h2>Mua sắm theo danh mục</h2>
        <a href="<%= request.getContextPath() %>/shop" class="btn btn-rounded"
          >Xem tất cả <i class="ri-arrow-right-line"></i
        ></a>
      </div>
      <!-- Categories Container -->
      <div class="home-category-slider">
        <button type="button" class="category-slide-button" data-category-slide="prev" aria-label="Xem danh mục trước"><i class="ri-arrow-left-s-line" aria-hidden="true"></i></button>
        <div class="home-category-viewport">
        <div class="categories-container home-category-track">
        <% int categoryIndex = 0; for (Category category : categories) {
            if (categoryIndex >= homeCategoryLimit) break;
            String imageUrl = category.getImageUrl();
            if (imageUrl != null) imageUrl = imageUrl.trim();
            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                imageUrl = request.getContextPath() + "/" + imageUrl.replaceFirst("^/+", "");
            }
            int productCount = categoryCounts.getOrDefault(category.getId(), 0);
        %>
        <a class="box box<%= categoryIndex++ % 6 + 1 %>" href="<%= request.getContextPath() %>/home?category=<%= ViewUtil.escapeHtml(category.getId()) %>#products">
          <% if (imageUrl == null || imageUrl.isEmpty()) { %>
          <div class="category-placeholder" role="img" aria-label="Danh mục chưa có ảnh"><i class="ri-image-line"></i></div>
          <% } else { %>
          <img src="<%= ViewUtil.escapeHtml(imageUrl) %>" alt="<%= ViewUtil.escapeHtml(category.getName()) %>" loading="lazy" decoding="async" />
          <% } %>
          <h2><%= ViewUtil.escapeHtml(category.getName()) %></h2>
          <span><%= productCount %> sản phẩm</span>
        </a>
        <% } %>
        </div>
        </div>
        <button type="button" class="category-slide-button" data-category-slide="next" aria-label="Xem danh mục tiếp theo"><i class="ri-arrow-right-s-line" aria-hidden="true"></i></button>
      </div>
      <% if (categories.isEmpty()) { %>
      <p class="shop-empty" role="status">Chưa có danh mục nào.</p>
      <% } %>
    </section>
    <!-- Banners -->
    <section class="banners container">
      <div class="banners-container">
        <div class="banner-box banner-1">
          <div class="banner-content">
            <h2>Tươi ngon mỗi ngày<br />cùng sản phẩm sạch<br />của chúng tôi</h2>
            <a href="<%= request.getContextPath() %>/shop" class="btn btn-red">
              <i class="ri-shopping-bag-line"></i>
            Mua ngay
            </a>
          </div>
          <img
            src="<%= request.getContextPath() %>/images/banner-1.png"
            alt=""
            class="banner-img"
            loading="lazy"
            decoding="async"
          />
        </div>
        <div class="banner-box banner-2">
          <div class="banner-content">
            <h2>Bữa sáng lành mạnh<br />và dễ dàng hơn</h2>
            <a href="<%= request.getContextPath() %>/shop" class="btn btn-red">
              <i class="ri-shopping-bag-line"></i>
            Mua ngay
            </a>
          </div>
          <img
            src="<%= request.getContextPath() %>/images/banner-2.png"
            alt=""
            class="banner-img"
            loading="lazy"
            decoding="async"
          />
        </div>
      </div>
    </section>
    <!-- Products -->
    <section class="products container" id="products" data-product-limit="<%= homeProductLimit %>">
      <div class="heading">
        <h2>Sản phẩm tươi ngon</h2>
        <a href="<%= request.getContextPath() %>/shop" class="btn btn-rounded"
          >Xem tất cả <i class="ri-arrow-right-line"></i
        ></a>
      </div>
        <div class="filter-buttons" aria-label="Lọc sản phẩm">
          <button type="button" class="filter-btn active" data-category="all" aria-pressed="true">Tất cả</button>
          <% int filterCategoryIndex = 0; for (Category category : categories) {
              if (filterCategoryIndex++ >= homeFilterCategoryLimit) break;
          %>
          <button type="button" class="filter-btn" data-category="<%= ViewUtil.escapeHtml(category.getId()) %>" aria-pressed="false"><%= ViewUtil.escapeHtml(category.getName()) %></button>
          <% } %>
        </div>
      <div class="product-container">
        <% int productIndex = 0; for (Product product : products) {
            String imageUrl = product.getImageUrl();
            if (imageUrl != null) imageUrl = imageUrl.trim();
            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                imageUrl = request.getContextPath() + "/" + imageUrl.replaceFirst("^/+", "");
            }
        %>
        <article class="product-box box<%= categoryColors.getOrDefault(product.getCategoryId(), 1) %>" data-category="<%= ViewUtil.escapeHtml(product.getCategoryId()) %>" <%= productIndex++ >= homeProductLimit ? "hidden" : "" %>>
          <% if (product.getStock() <= 0) { %>
          <span class="badge badge-out">Hết hàng</span>
          <% } else { %>
          <span class="badge badge-hot">Còn hàng</span>
          <% } %>
          <% if (imageUrl == null || imageUrl.isEmpty()) { %>
          <div class="product-img product-placeholder" role="img" aria-label="Chưa có ảnh sản phẩm"><i class="ri-image-line"></i></div>
          <% } else { %>
          <img src="<%= ViewUtil.escapeHtml(imageUrl) %>" alt="<%= ViewUtil.escapeHtml(product.getName()) %>" class="product-img" loading="lazy" decoding="async" />
          <% } %>
          <h3><%= ViewUtil.escapeHtml(product.getName()) %></h3>
          <div class="product-footer">
            <span class="price"><%= String.format("%,d", product.getPrice()) %> &#8363;</span>
            <form class="shop-cart-form" method="post" action="<%= request.getContextPath() %>/cart">
              <input type="hidden" name="productId" value="<%= ViewUtil.escapeHtml(product.getId()) %>" />
              <input type="hidden" name="quantity" value="1" />
              <button class="add-to-cart" type="submit" name="action" value="add" aria-label="Thêm <%= ViewUtil.escapeHtml(product.getName()) %> vào giỏ hàng" <%= product.getStock() <= 0 ? "disabled" : "" %>><i class="ri-shopping-bag-line"></i></button>
            </form>
          </div>
        </article>
        <% } %>
      </div>
      <p class="shop-empty" role="status" <%= products.isEmpty() ? "" : "hidden" %>>Không có sản phẩm trong danh mục này.</p>
    </section>
    <!-- Shipping Container -->
    <section class="shipping container">
      <div class="shipping-box">
        <div class="shipping-content">
          <h2>
            Chúng tôi giao hàng<br />vào ngày hôm sau, từ<br />10:00 đến 20:00
          </h2>
          <p>Áp dụng cho đơn hàng trên 500.000 ₫</p>
          <a href="<%= request.getContextPath() %>/shop" class="btn">
            <i class="ri-shopping-bag-line"></i>
          Đặt ngay
          </a>
        </div>
        <div class="shipping-image">
          <img
            src="<%= request.getContextPath() %>/images/scooter-3d.png"
            alt="scooter"
            loading="lazy"
            decoding="async"
          />
        </div>
      </div>
    </section>
    <!-- Footer -->
    <jsp:include page="/WEB-INF/views/includes/footer.jsp" />
    <!-- Link To JS -->
    <jsp:include page="/WEB-INF/views/includes/scripts.jsp" />
  </body>
</html>
