# Rà soát và sửa lỗi website — 11/11/2026

## Nguyên nhân trực tiếp không thêm được giỏ hàng

Form thêm sản phẩm có phần tử `name="action"`. Trong DOM của trình duyệt, phần tử này che thuộc tính `HTMLFormElement.action`. Đoạn `fetch(form.action, ...)` vì vậy nhận phần tử HTML hoặc `RadioNodeList` thay vì URL `/cart`, dẫn đến gửi sai địa chỉ và hiện thông báo thất bại.

Đã thay bằng `fetch(form.getAttribute('action'), ...)`, giữ cookie phiên, gửi rõ thao tác `add` và xếp hàng các yêu cầu AJAX để số lượng trên biểu tượng giỏ không bị cập nhật ngược thứ tự. Đã tái hiện lỗi và xác nhận bản sửa bằng Chrome thật.

## Những vấn đề liên quan đã sửa

- **Tomcat/Render:** tắt cổng shutdown (`Server port="-1"`); HTTP bind `0.0.0.0` theo biến `PORT`, mặc định 10000. Bổ sung `/health` hỗ trợ GET/HEAD và cấu hình `healthCheckPath`. Cảnh báo `Invalid shutdown command [HEAD / HTTP/1.1]` trong ảnh là HTTP đi vào cổng shutdown; đây là vấn đề riêng với lỗi JavaScript của nút giỏ hàng.
- **Form thường:** trang JSP được forward có thể cung cấp URI nội bộ `/WEB-INF/views/...` làm địa chỉ quay lại. Đã dùng rõ `/home` hoặc `/shop`, thêm hidden action và giới hạn chuyển hướng về hai trang công khai này. Đã thử với JavaScript bị tắt.
- **Giỏ hàng:** tuần tự hóa thao tác theo phiên, dùng bản sao giỏ khi cập nhật, chuẩn hóa ObjectId, chặn cộng số lượng bị tràn số hoặc vượt tồn kho. Giữ việc khôi phục ảnh cho giỏ cũ. Thao tác không hợp lệ trả 400; lỗi MongoDB trả JSON 503 và có log máy chủ.
- **Đăng nhập:** đổi session ID để bảo vệ phiên và giữ giỏ khách, tránh hủy phiên rồi khôi phục giỏ cũ. Hạn chế địa chỉ chuyển hướng sau đăng nhập và xóa xác nhận thanh toán cũ.
- **Thanh toán:** xác nhận gắn với mã dùng một lần và nội dung giỏ gồm sản phẩm, giá, số lượng. Thay đổi giỏ hoặc sửa thông tin sẽ hủy xác nhận trước; hai yêu cầu đặt hàng đồng thời trong cùng phiên chỉ tạo một đơn. Số điện thoại phải chứa đủ chữ số thực tế.
- **Giao diện:** đồng bộ trạng thái menu với `aria-expanded`, bảo vệ truy cập phần tử DOM; thay phiên bản URL script để trình duyệt tải bản sửa.

## Kiểm chứng

- Maven: **37 kiểm thử Java đạt**, bao gồm khách chưa đăng nhập, tồn kho, dữ liệu đầu vào, 32 yêu cầu thêm đồng thời, giữ giỏ khi đăng nhập, xác nhận cũ và hai yêu cầu đặt hàng đồng thời. DAO đặt hàng được giả lập trong kiểm thử để không tạo đơn thực.
- Node: **3 kiểm thử JavaScript đạt**, gồm lỗi phần tử `action` che thuộc tính form, thứ tự yêu cầu và phục hồi sau lỗi phản hồi.
- Chrome thật: thêm AJAX, thêm nhanh hai sản phẩm, số lượng trên header, cập nhật và xóa giỏ, tìm kiếm, menu mobile bằng bàn phím, thêm bằng form khi tắt JavaScript; không có lỗi JavaScript.
- Tomcat 9 + MongoDB thật trên máy cục bộ: kiểm tra trang chủ, cửa hàng, giỏ, đăng nhập, đăng ký, checkout, đơn cá nhân; kiểm tra quyền truy cập và tải các trang quản trị. Phiên giả lập chỉ tồn tại trong thư mục runtime thử nghiệm, không nằm trong WAR bàn giao.
- MongoDB: ping và đọc được cả 180 sản phẩm đang bán; kiểm tra kiểu dữ liệu và truy xuất sản phẩm theo ID thành công. 182 tham chiếu ảnh trên trang cửa hàng đều trả HTTP 200.
- Shell: kiểm tra cổng mặc định, cổng tùy chỉnh, từ chối PORT không hợp lệ và phép sửa `server.xml`. Môi trường không có Docker engine nên chưa chạy container Docker thực tế.

Chạy lại các kiểm thử trong repository:

```powershell
mvn -B package
node src/test/js/cart.test.cjs
```

Gói WAR: `target/msm.war`. Mã nguồn và gói đã được sửa tại máy; chưa push hoặc triển khai lên Render. Với dịch vụ Render tạo thủ công, đặt Health Check Path thành `/health`; `render.yaml` áp dụng thiết lập này khi dùng Blueprint. Sau khi triển khai, kiểm tra log HTTP connector dùng đúng PORT và thử thêm giỏ cả khi chưa đăng nhập.

Tài liệu đối chiếu: [Tomcat Server/shutdown port](https://tomcat.apache.org/tomcat-9.0-doc/config/server.html), [Render web service ports](https://render.com/docs/web-services), [Render health checks](https://render.com/docs/health-checks).
