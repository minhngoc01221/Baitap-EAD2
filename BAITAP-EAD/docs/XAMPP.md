# Shop kết nối XAMPP và lưu database

## Cấu hình đang chạy

- XAMPP: `D:\Xam`; `htdocs` là thư mục PHP, không đặt source Spring Boot vào đó.
- MariaDB: **10.4.32**, host `127.0.0.1`, port **3308**.
- Database: `shop_api`, user `root`, mật khẩu rỗng (tài khoản thử nghiệm cục bộ).
- Dữ liệu lưu trên đĩa: `D:\Xam\mysql\shop-data`.
- Spring Boot: profile `xampp`, driver MariaDB Connector/J, `ddl-auto=update`.
- phpMyAdmin: <http://127.0.0.1/phpmyadmin/index.php?server=2&db=shop_api>.

Backend không lưu database trong htdocs. Hibernate ghi trực tiếp vào MariaDB; dữ liệu còn sau khi tắt/chạy lại cả backend và database. `database/shop_api.sql` là bản xuất để sao lưu/nhập lại, không phải file được cập nhật sau mỗi request.

## Vì sao có instance Shop riêng?

Cổng 3306 thuộc dịch vụ MySQL80. MariaDB của XAMPP dùng vùng `mysql/data` cũ không khởi động được, log báo LSN InnoDB không nhất quán và crash ở bước nạp quyền truy cập. Trong đó còn nhiều database bài tập khác.

Đã giữ cấu hình/datadir gốc, không xóa hoặc ghi đè những database đó. Tạo vùng dữ liệu sạch `mysql/shop-data` bằng bộ MariaDB của chính XAMPP, chạy riêng ở cổng 3308, và thêm server **Shop - XAMPP MariaDB (3308)** trong phpMyAdmin. Không sử dụng nút MySQL mặc định để mở instance Shop vì nút đó vẫn trỏ vùng dữ liệu cũ.

## Chạy lại sau khi tắt máy

1. Trong thư mục dự án chạy:
   ```powershell
   ./start-xampp-shop.ps1
   ```
   Script mở MariaDB Shop ở chế độ nền, không mở cửa sổ console. Log trong `tmp/xampp-shop.*.log`.
2. Start **Apache** trong XAMPP Control Panel nếu muốn dùng phpMyAdmin.
3. Trong IntelliJ chọn **ShopApplication-XAMPP**, bấm Run. Nếu ứng dụng đang chạy ở cổng 8080, dừng phiên cũ trước.
4. Mở <http://localhost:8080>. Để xem dữ liệu, vào phpMyAdmin bằng URL trên hoặc chọn server **Shop - XAMPP MariaDB (3308)** rồi database `shop_api`.

Các file cấu hình XAMPP/phpMyAdmin gốc được sao lưu cạnh file gốc với hậu tố `.before-shop-3308.bak`. MySQL80 cổng 3306 và database cũ cổng 3307 không bị dừng hay đổi cấu hình.

## Chuyển sang XAMPP trên máy khác

Profile hỗ trợ biến môi trường `XAMPP_DB_PORT`, `XAMPP_DB_USER`, `XAMPP_DB_PASSWORD`. Nếu XAMPP máy khác dùng cổng 3306, đặt `XAMPP_DB_PORT=3306` trong IntelliJ. Dữ liệu không tự đi theo source; có thể nhập `database/shop_api.sql` bằng phpMyAdmin.

**File SQL có DROP TABLE: chỉ import vào database mới/rỗng hoặc sau khi đã sao lưu database đích.** File giữ dữ liệu/hồ sơ/mật khẩu băm, không chuyển phiên đăng nhập đang hoạt động. Sau import cần đăng nhập lại.

## Kiểm tra đã thực hiện

- Đã kết nối bằng driver MariaDB và nhập dữ liệu Shop vào XAMPP.
- Đã tạo khách, sản phẩm và đơn qua API.
- Đã restart riêng MariaDB Shop và backend, rồi đọc lại đơn và kiểm tra tồn kho.
- Bằng chứng: `docs/evidence/xampp-import.txt`, `docs/evidence/xampp-persistence.json` và log backend XAMPP.
- 13 test backend vẫn đạt. Apache/phpMyAdmin trả HTTP 200; PHP mysqli kết nối được MariaDB 10.4.32.
