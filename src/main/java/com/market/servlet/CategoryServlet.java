package com.market.servlet;

import com.market.dao.CategoryDAO;
import com.market.model.Category;
import com.market.util.ProductImageStorage;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;

@WebServlet("/categories")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5L * 1024 * 1024, maxRequestSize = 6L * 1024 * 1024)
public class CategoryServlet extends HttpServlet {
    private CategoryDAO categoryDAO;

    @Override public void init() { categoryDAO = new CategoryDAO(); }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("edit".equals(request.getParameter("action"))) {
            String id = request.getParameter("id");
            if (id != null && ObjectId.isValid(id)) request.setAttribute("editCategory", categoryDAO.getById(id));
        }
        showPage(request, response);
    }

    private void showPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("categories", categoryDAO.getAll());
        request.getRequestDispatcher("/WEB-INF/views/category.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        boolean multipart = request.getContentType() != null && request.getContentType().toLowerCase(Locale.ROOT).startsWith("multipart/form-data");
        if (multipart) {
            try { request.getParts(); }
            catch (IllegalStateException e) {
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
        try {
            String action = request.getParameter("action");
            if ("insert".equals(action) || "update".equals(action)) {
                boolean editing = "update".equals(action);
                Category existing = null;
                if (editing) {
                    String id = request.getParameter("id");
                    if (id == null || !ObjectId.isValid(id)) throw new IllegalArgumentException("Mã danh mục không hợp lệ.");
                    existing = categoryDAO.getById(id);
                    if (existing == null) throw new IllegalArgumentException("Không tìm thấy danh mục.");
                    request.setAttribute("editCategory", existing);
                }
                String name = request.getParameter("name");
                request.setAttribute("formSubmitted", true);
                request.setAttribute("formName", name);
                if (name == null || name.isBlank()) throw new IllegalArgumentException("Vui lòng nhập tên danh mục.");
                String image = ProductImageStorage.save(multipart ? request.getPart("image") : null);
                Category category = new Category(name.trim(), existing == null ? "" : existing.getDescription());
                category.setImageUrl(image != null ? image : existing == null ? "" : existing.getImageUrl());
                if (editing) {
                    category.setId(existing.getId());
                    if (!categoryDAO.update(category)) throw new IllegalArgumentException("Danh mục không còn tồn tại. Vui lòng tải lại trang và thử lại.");
                } else categoryDAO.insert(category);
            } else if ("delete".equals(action)) {
                String id = request.getParameter("id");
                if (id == null || !ObjectId.isValid(id)) throw new IllegalArgumentException("Mã danh mục không hợp lệ.");
                categoryDAO.delete(id);
            } else throw new IllegalArgumentException("Thao tác danh mục không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/categories");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", e.getMessage());
            showPage(request, response);
        }
    }
}
