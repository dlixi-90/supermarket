<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List,com.market.model.Category,com.market.util.ViewUtil" %>
<%!
    private String imageSource(String context, String image) {
        if (image == null || image.isBlank()) return "";
        image = image.trim();
        return image.startsWith("https://") || image.startsWith("http://") ? image : context + "/" + image.replaceFirst("^/+", "");
    }
%>
<%
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    Category editCategory = (Category) request.getAttribute("editCategory");
    boolean editing = editCategory != null;
    String name = Boolean.TRUE.equals(request.getAttribute("formSubmitted")) ? (String) request.getAttribute("formName") : editing ? editCategory.getName() : "";
    String currentImage = editing ? imageSource(request.getContextPath(), editCategory.getImageUrl()) : "";
    String error = (String) request.getAttribute("error");
%>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý danh mục</title>
    <jsp:include page="/WEB-INF/views/includes/styles.jsp" />
</head>
<body class="msm-page msm-category">
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<div class="msm-content admin-layout">
    <jsp:include page="/WEB-INF/views/includes/admin-nav.jsp" />
    <main class="container">
        <h1>Quản lý danh mục</h1>
        <div class="management-layout category-management-layout">
            <section class="management-list" aria-labelledby="category-list">
                <div class="section-header category-list-heading">
                    <div><h2 id="category-list">Danh sách danh mục</h2><span class="small"><%= categories == null ? 0 : categories.size() %> danh mục</span></div>
                    <button class="save-button category-add-button" type="button" id="add-category-button"><i class="ri-add-line" aria-hidden="true"></i>Thêm danh mục</button>
                </div>
                <div class="product-container category-card-grid">
                    <% if (categories != null && !categories.isEmpty()) { for (Category category : categories) {
                        String image = imageSource(request.getContextPath(), category.getImageUrl());
                    %>
                    <article class="product-box category-card">
                        <% if (!image.isEmpty()) { %><img class="product-img" src="<%= ViewUtil.escapeHtml(image) %>" alt="<%= ViewUtil.escapeHtml(category.getName()) %>" loading="lazy"><% } else { %><div class="product-img product-placeholder" aria-label="Danh mục chưa có ảnh"><i class="ri-image-line" aria-hidden="true"></i></div><% } %>
                        <h3 title="<%= ViewUtil.escapeHtml(category.getName()) %>"><%= ViewUtil.escapeHtml(category.getName()) %></h3>
                        <div class="category-card-actions">
                            <button class="button secondary category-edit-button" type="button" data-id="<%= ViewUtil.escapeHtml(category.getId()) %>" data-name="<%= ViewUtil.escapeHtml(category.getName()) %>" data-image="<%= ViewUtil.escapeHtml(image) %>">Sửa</button>
                            <form method="post" action="<%= request.getContextPath() %>/categories" onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này không?');"><input type="hidden" name="id" value="<%= ViewUtil.escapeHtml(category.getId()) %>"><button class="delete-button" type="submit" name="action" value="delete">Xóa</button></form>
                        </div>
                    </article>
                    <% } } %>
                </div>
                <% if (categories == null || categories.isEmpty()) { %><p class="empty">Chưa có danh mục nào.</p><% } %>
            </section>
        </div>
        <dialog class="category-dialog" id="category-dialog" aria-labelledby="category-form-title" data-open-on-load="<%= error != null || editing ? "true" : "false" %>">
            <div class="category-dialog-heading">
                <h2 id="category-form-title"><%= editing ? "Cập nhật danh mục" : "Thêm danh mục" %></h2>
                <button class="category-dialog-close" type="button" aria-label="Đóng cửa sổ">×</button>
            </div>
            <aside class="form-box management-form">
                <% if (error != null) { %><div class="error" role="alert"><%= ViewUtil.escapeHtml(error) %><p class="small">Nếu đã chọn ảnh, vui lòng chọn lại trước khi gửi biểu mẫu.</p></div><% } %>
                <form id="category-form" method="post" action="<%= request.getContextPath() %>/categories" enctype="multipart/form-data">
                    <input id="category-edit-id" type="hidden" name="id" value="<%= editing ? ViewUtil.escapeHtml(editCategory.getId()) : "" %>">
                    <label for="category-name">Tên danh mục</label>
                    <input id="category-name" type="text" name="name" required value="<%= ViewUtil.escapeHtml(name) %>">
                    <label for="category-image">Ảnh danh mục</label>
                    <label class="upload-preview" for="category-image">
                        <img id="category-image-preview" alt="Ảnh xem trước danh mục" data-current-src="<%= ViewUtil.escapeHtml(currentImage) %>" <%= currentImage.isEmpty() ? "hidden" : "src=\"" + ViewUtil.escapeHtml(currentImage) + "\"" %>>
                        <span id="category-image-placeholder" <%= currentImage.isEmpty() ? "" : "hidden" %>><i class="ri-image-add-line" aria-hidden="true"></i>Chọn ảnh</span>
                    </label>
                    <input id="category-image" type="file" name="image" accept="image/jpeg,image/png" aria-describedby="category-image-hint category-image-error">
                    <p id="category-image-hint" class="hint">Ảnh JPG hoặc PNG · Tối đa 5 MB. Để trống nếu muốn giữ ảnh hiện tại.</p>
                    <p id="category-image-error" class="upload-error" role="status"></p>
                    <div class="management-form-actions">
                        <button class="save-button" id="category-submit" type="submit" name="action" value="<%= editing ? "update" : "insert" %>"><%= editing ? "Cập nhật danh mục" : "Thêm danh mục" %></button>
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
