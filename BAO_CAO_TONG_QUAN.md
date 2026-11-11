# Báo cáo tổng quan website Growcer

## 1. Mục đích

Growcer là website bán hàng tạp hóa trực tuyến. Khách truy cập có thể xem danh mục và sản phẩm; khách hàng đăng ký tài khoản, tạo đơn và theo dõi đơn hàng; quản trị viên quản lý dữ liệu và xử lý hoạt động bán hàng.

## 2. Kiến trúc hệ thống

- **Giao diện:** JSP, CSS và JavaScript; hiển thị trang bán hàng và khu vực quản trị.
- **Ứng dụng:** Java 17, Servlet API 4.0.1, tổ chức xử lý theo Servlet, DAO và model.
- **Cơ sở dữ liệu:** MongoDB, database `marketdb`.

Luồng xử lý chính: trình duyệt gửi yêu cầu đến Servlet → Servlet gọi DAO → DAO đọc/ghi MongoDB → Servlet chuyển dữ liệu sang JSP để hiển thị.

## 3. Tổng quan dữ liệu

Database `marketdb` có bốn collection nghiệp vụ chính:

| Collection | Nội dung chính |
|---|---|
| `users` | Tài khoản gồm tên, email, mật khẩu đã băm và vai trò (`customer` hoặc `admin`). Đăng ký công khai tạo tài khoản khách hàng. |
| `categories` | Danh mục gồm tên, mô tả và đường dẫn ảnh. |
| `products` | Sản phẩm gồm tên, mô tả, giá, tồn kho, mã danh mục, ảnh, trạng thái kinh doanh, ngày sản xuất, hạn sử dụng và thời điểm tạo/cập nhật. |
| `orders` | Đơn hàng gồm khách đặt, thông tin nhận hàng, ngày đặt, phương thức thanh toán, trạng thái, tổng tiền và danh sách mặt hàng. Mỗi mặt hàng lưu mã/tên sản phẩm, giá, số lượng và thành tiền. |

Giỏ hàng được lưu trong HTTP session trong quá trình mua sắm; không phải collection riêng trong MongoDB. Đơn hàng được lưu trong `orders`, cùng danh sách mặt hàng của đơn.

## 4. Vai trò và use case

### Khách chưa đăng nhập

- Xem trang chủ, danh mục và cửa hàng.
- Thêm sản phẩm vào giỏ hàng, xem và cập nhật giỏ hàng theo phiên duyệt web.
- Đăng ký hoặc đăng nhập.

### Khách hàng

- Thêm, cập nhật số lượng hoặc bỏ sản phẩm khỏi giỏ hàng.
- Nhập thông tin giao hàng, chọn phương thức thanh toán và đặt đơn.
- Xem danh sách và trạng thái các đơn hàng của mình.

### Quản trị viên

- Xem dashboard: doanh thu đơn đã giao, số đơn, sản phẩm, khách hàng, đơn theo trạng thái và cảnh báo tồn kho thấp.
- Thêm, sửa, xóa danh mục và sản phẩm; quản lý tồn kho, trạng thái bán và ảnh.
- Quản lý tài khoản người dùng và vai trò.
- Xem đơn hàng, lọc theo trạng thái và cập nhật tiến trình xử lý.

## 5. Tính năng chính

- Cửa hàng trực tuyến với danh mục và sản phẩm.
- Đăng ký, đăng nhập, đăng xuất và phân quyền theo vai trò.
- Giỏ hàng theo phiên đăng nhập.
- Checkout và đặt hàng; khi tạo đơn, hệ thống kiểm tra và cập nhật tồn kho.
- Theo dõi đơn hàng phía khách hàng và quản lý trạng thái phía quản trị.
- Dashboard tổng hợp hoạt động bán hàng.
- Tải ảnh JPG/PNG cho danh mục và sản phẩm; giới hạn dung lượng ảnh theo xử lý hiện tại là 5 MB.

## 6. Các đường dẫn chức năng tiêu biểu

`/home`, `/shop`, `/login`, `/register`, `/cart`, `/checkout`, `/place-order`, `/my-orders`, `/dashboard`, `/users`, `/categories`, `/products`, `/orders`.

Các trang `/checkout`, `/place-order` và `/my-orders` yêu cầu đăng nhập. Khách chưa đăng nhập có thể thêm và xem giỏ hàng (`/cart`), và sẽ được yêu cầu đăng nhập khi tiến hành thanh toán. Các trang `/dashboard`, `/users`, `/categories`, `/products` và `/orders` được giới hạn cho vai trò quản trị viên.

## 7. Tóm tắt

Growcer là ứng dụng web bán hàng theo mô hình MVC đơn giản: JSP đảm nhiệm giao diện, Servlet điều phối nghiệp vụ, DAO thao tác MongoDB. Các nghiệp vụ cốt lõi gồm quản lý sản phẩm/danh mục, giỏ hàng, đặt đơn, theo dõi đơn và quản trị hệ thống.
