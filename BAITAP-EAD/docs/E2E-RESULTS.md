# Kết quả E2E qua trình duyệt — 16/10/2026

Các bước sau đã được thao tác thực tế trên giao diện, không thay bằng gọi HTTP trực tiếp. Truy vấn SQL được dùng để đối chiếu kết quả cuối với MySQL 8.0.46, cổng 3307.

## 1. Mock trước backend

1. Mở prototype `http://127.0.0.1:5500`, giữ Base URL `http://localhost:4010`.
2. Đăng nhập bằng dữ liệu mẫu, chọn Nova Mechanical K75, thêm giỏ và đặt hàng.
3. FE hiện **Mã đơn #1 / 1.500.000đ / PENDING** theo ví dụ cố định.

Kết quả **PASS**. Mock không tính lại số tiền hoặc lưu giỏ/đơn. Bằng chứng: [mock-ui-success.png](evidence/mock-ui-success.png).

## 2. FE thật -> checkout -> MySQL -> FE thành công

1. Mở `http://localhost:8080`, đăng nhập `demo@shop.local`.
2. Mở chi tiết Nova Mechanical K75: giá 350.000, stock 15.
3. Thêm giỏ, tăng quantity lên 2, quan sát tạm tính 700.000.
4. Bấm Đặt hàng. FE hiện **mã đơn #4, tổng 700.000, PENDING** và giỏ về 0.
5. SQL trực tiếp xác nhận customer_id 4, product_id 4, quantity 2, total 700000, stock 13.

Kết quả **PASS**. Bằng chứng: [e2e-checkout-success.png](evidence/e2e-checkout-success.png), [e2e-mysql-checkout.txt](evidence/e2e-mysql-checkout.txt).

## 3. Lịch sử, hủy và hoàn tồn

1. Mở Đơn của tôi, thấy đơn #4 và các dòng sản phẩm.
2. Bấm Hủy đơn: UI cập nhật CANCELLED và bỏ nút hủy.
3. Đặt thêm một sản phẩm cùng loại tạo đơn #5: tổng 350.000; admin thấy stock 14, khớp việc đã hoàn 2 sản phẩm của đơn #4 rồi trừ 1 của đơn #5.

Kết quả **PASS**. Bằng chứng: [e2e-cancelled.png](evidence/e2e-cancelled.png). Unit/integration test bổ sung kiểm tra hủy lặp không hoàn tồn hai lần.

## 4. Admin dashboard

1. Đăng xuất khách, đăng nhập `admin@shop.local`, mở Quản trị.
2. Tải danh sách đơn/sản phẩm/danh mục từ backend.
3. Chuyển đơn #5 PENDING -> PAID -> SHIPPED bằng nút trên đúng dòng.
4. Sau SHIPPED, dòng không còn nút hủy/chuyển tiếp.
5. Thay tồn Nova Mechanical K75 từ 14 thành 16, bấm Lưu tồn; UI báo thành công và SQL xác nhận 16.

Kết quả **PASS**. Bằng chứng: [admin-dashboard.png](evidence/admin-dashboard.png), [e2e-mysql-final.txt](evidence/e2e-mysql-final.txt).

## 5. Cách chạy lại

Đã kiểm tra thêm tìm `Wave`, danh mục `Âm thanh`, giá 500.000–700.000: FE trả đúng một sản phẩm `Wave Pro Headphones` giá 590.000. Bằng chứng [storefront-filtered.png](evidence/storefront-filtered.png). Swagger UI cũng đã thực thi GET products và nhận HTTP 200: [swagger-products-200.png](evidence/swagger-products-200.png).

Đã xóa bộ lọc và chuyển storefront sang trang 2/3, xác nhận nhóm sản phẩm tiếp theo hiển thị và nút trang trước được bật. Sau thử nghiệm đã đăng xuất tài khoản admin và trả storefront về trang đầu để bàn giao.

Dùng tài khoản mẫu trên MySQL hoặc profile demo. ID sẽ thay đổi theo database, hãy đối chiếu mã đơn hiện trên FE thay vì cố định #4/#5. Thực hiện các bước trên theo thứ tự. Ngoài E2E UI, hai Postman collections và 13 JUnit tests có thể chạy tự động theo README.

E2E được thực thi bằng công cụ điều khiển trình duyệt trong phiên làm việc. Chưa có runner trình duyệt standalone đóng gói; các bước tái hiện và ảnh/SQL là hồ sơ E2E của bài. Không có giao dịch thanh toán thật.
