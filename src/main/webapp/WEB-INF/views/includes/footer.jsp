<%@ page contentType="text/html;charset=UTF-8" %>
<footer class="footer" id="about">
  <div class="footer-container container">
    <div class="footer-col">
      <div class="footer-logo">
        <img src="<%= request.getContextPath() %>/images/logo.png" alt="logo" />
      </div>
      <div class="footer-contact">
        <div class="contact-item">
          <i class="ri-map-pin-line"></i>
          <span>Việt Nam</span>
        </div>
        <div class="contact-item">
          <i class="ri-phone-line"></i>
          <span>(028) 123 123</span>
        </div>
        <div class="contact-item">
          <i class="ri-mail-line"></i>
          <span>hello@gmail.com</span>
        </div>
        <div class="contact-item">
          <i class="ri-time-line"></i>
          <span>10:00 - 18:00, Thứ Hai - Thứ Bảy</span>
        </div>
      </div>
    </div>
    <div class="footer-col">
      <h3>Tài khoản</h3>
      <a href="<%= request.getContextPath() %>/login">Đăng nhập</a>
      <a href="<%= request.getContextPath() %>/cart">Xem giỏ hàng</a>
      <a href="<%= request.getContextPath() %>/my-orders">Đơn hàng của tôi</a>
      <a href="<%= request.getContextPath() %>/shop">Sản phẩm mới</a>
      <a href="#">Liên hệ</a>
    </div>
    <div class="footer-col">
      <h3>Cửa hàng</h3>
      <a href="#">Về chúng tôi</a>
      <a href="#">Chính sách bảo mật</a>
      <a href="#">Điều khoản và điều kiện</a>
      <a href="#">Trợ giúp</a>
      <a href="#">Tuyển dụng</a>
    </div>
    <div class="footer-col">
      <h3>Thanh toán</h3>
      <div class="payment-methods">
        <div class="payment-card">
          <i class="ri-visa-line"></i>
        </div>
        <div class="payment-card">
          <i class="ri-mastercard-line"></i>
        </div>
        <div class="payment-card">
          <i class="ri-paypal-line"></i>
        </div>
      </div>
    </div>
  </div>
  <div class="copyright">
    <p>&copy; 2026 Growcer. Mọi quyền được bảo lưu.</p>
  </div>
</footer>
