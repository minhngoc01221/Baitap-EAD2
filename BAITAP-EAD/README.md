# NOVA Shop — Fullstack Spring Boot & API-First

Ứng dụng bán hàng và quản trị viết bằng Java 21 / Spring Boot 3.5.3, Spring Data JPA, MySQL, OpenAPI 3.0.3 và HTML/CSS/JavaScript SPA. Có chú thích tiếng Việt trong source. Bản nâng cấp giữ dữ liệu của dự án cũ.

## Chạy trong IntelliJ

1. Open thư mục dự án, chọn JDK 21 (hoặc JDK 24 hiện có trên máy, Maven vẫn biên dịch release 21).
2. Reload Maven, chạy `generate-sources` nếu IDE chưa nhận `generated` package.
3. Chọn cấu hình **ShopApplication-XAMPP**, VM option `-Dspring.profiles.active=xampp`. Đây cũng là profile mặc định khi không chỉ định profile.
4. XAMPP Shop đã cấu hình dùng `127.0.0.1:3308`, database `shop_api`, user `root`, mật khẩu rỗng. Đổi bằng `XAMPP_DB_PORT`, `XAMPP_DB_USER`, `XAMPP_DB_PASSWORD` khi cần. Xem `docs/XAMPP.md` để nhập dữ liệu hiện có từ `database/shop_api.sql`.
5. Nếu backend đã chạy ở cổng 8080, dừng phiên cũ trước khi Run trong IntelliJ.

Mở cửa hàng tại <http://localhost:8080>, quản trị tại <http://localhost:8080/#admin>, Swagger tại <http://localhost:8080/swagger-ui.html>.

**Máy hiện tại:** XAMPP ở `D:\Xam`, MariaDB Shop chạy cổng **3308**, dữ liệu lưu trong `D:\Xam\mysql\shop-data`. Đã chuyển dữ liệu Shop sang đó. Vì vùng dữ liệu XAMPP cũ bị lỗi và còn chứa các database khác, instance Shop chạy riêng, không sửa/xóa database cũ. Sau khi khởi động lại máy, chạy `./start-xampp-shop.ps1`, rồi chạy **ShopApplication-XAMPP** trong IntelliJ. Xem dữ liệu tại `http://127.0.0.1/phpmyadmin/index.php?server=2&db=shop_api` (Apache XAMPP phải đang chạy). MySQL80 cổng 3306 và database cũ cổng 3307 được giữ nguyên.

| Vai trò | Email thử nghiệm | Mật khẩu |
|---|---|---|
| Khách | demo@shop.local | Customer123! |
| Admin | admin@shop.local | Admin123! |

Tài khoản và dữ liệu mẫu tự tạo bằng CommandLineRunner + Datafaker. Đăng ký tài khoản mới từ giao diện nếu muốn thử dữ liệu riêng. Mật khẩu lưu BCrypt, token phiên lưu hash SHA-256, token hết hạn sau 8 giờ.

## Chạy độc lập / máy khác

Không cần MySQL khi muốn xem nhanh bằng H2:

```powershell
./run.ps1 demo
# hoặc
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Chế độ demo dùng database trong RAM, dữ liệu mất khi dừng. Dữ liệu mẫu vẫn được nạp.

Chạy MySQL cổng 3306 bằng Docker:

```powershell
docker compose up -d mysql
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Hoặc kết nối MySQL bạn tự cài:

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/shop_api?createDatabaseIfNotExist=true&serverTimezone=UTC'
$env:DB_USER='shop'
$env:DB_PASSWORD='shop'
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Các thông tin đăng nhập mẫu chỉ phục vụ bài tập cục bộ. MySQL cổng 3307 trên máy thực hiện được lưu riêng trong `.tools/mysql-data`, không đóng gói vào bài nộp. Profile `xampp` trên máy này mặc định dùng cổng 3308; `mysql-local` dùng cổng 3307. SQL export được đóng gói trong `database/` để nhập bằng phpMyAdmin, không bao gồm các phiên đăng nhập đang hoạt động.

## Chạy mock trước backend

```powershell
npm ci
npm run mock
# terminal khác
npm run ui
```

Mở <http://127.0.0.1:5500>, API Base URL mặc định `http://localhost:4010`. Thử đăng nhập, xem chi tiết, thêm giỏ và đặt hàng. Prism trả ví dụ cố định từ YAML, không lưu thay đổi, không mô phỏng nghiệp vụ tồn kho/phân quyền. Có thể dùng `docker compose up mock` thay cho Prism cài qua npm.

Trên cùng giao diện, đổi Base URL sang `http://localhost:8080/api/v1`, bấm **Kết nối** để chuyển sang backend. Việc đổi server xóa phiên/giỏ hiện tại để không dùng nhầm ID của mock. UI phục vụ trực tiếp ở cổng 8080 tự chọn backend thật.

## Các chức năng

- Tài khoản: đăng ký, đăng nhập/đăng xuất, cập nhật tên/email/phone/địa chỉ và lịch sử đơn phân trang.
- Storefront: danh sách, lọc danh mục/giá, tìm tên, sort, phân trang, chi tiết sản phẩm có ảnh.
- Giỏ hàng: thêm/tăng/giảm/xóa, tạm tính, lưu localStorage; checkout sử dụng giá và tồn kho thật ở server.
- Admin: CRUD danh mục/sản phẩm, cập nhật riêng tồn kho, danh sách đơn phân trang và xử lý trạng thái.
- Trạng thái: `PENDING -> PAID -> SHIPPED`; được `CANCELLED` từ PENDING hoặc PAID, hoàn tồn đúng một lần. PAID là xác nhận trong bài thực hành, không tích hợp thanh toán thật.
- Phân quyền backend: khách chỉ đọc/sửa hồ sơ và đơn của mình; ADMIN mới được sửa catalog hoặc xác nhận PAID/SHIPPED.

## API và Swagger

Hợp đồng gốc: `src/main/resources/shop-api.yaml`, 26 operation. Tất cả đường dẫn backend có tiền tố `/api/v1`; mock dùng trực tiếp đường dẫn không có tiền tố.

| Endpoint | Phương thức | Quyền |
|---|---|---|
| `/registrations` | POST | Công khai |
| `/sessions` | POST / DELETE | Đăng nhập / đăng xuất |
| `/profiles/current` | GET / PUT | Người đang đăng nhập |
| `/categories` | GET / POST | Đọc công khai; ghi ADMIN |
| `/categories/{id}` | GET / PUT / DELETE | Đọc công khai; ghi ADMIN |
| `/products` | GET / POST | Đọc công khai; ghi ADMIN |
| `/products/{id}` | GET / PUT / DELETE | Đọc công khai; ghi ADMIN |
| `/products/{id}/stock` | PATCH | ADMIN |
| `/customers` | GET / POST | ADMIN |
| `/customers/{id}` | GET / PUT / DELETE | Chủ sở hữu hoặc ADMIN; xóa chỉ ADMIN |
| `/customers/{id}/orders` | GET | Chủ sở hữu hoặc ADMIN |
| `/orders` | GET / POST | GET ADMIN; POST chủ đơn hoặc ADMIN |
| `/orders/{id}` | GET / PATCH | Chủ sở hữu hoặc ADMIN; khách chỉ được hủy |

Trong Swagger, chọn server Spring Boot. Gọi POST `/sessions` với tài khoản mẫu, chép trường `token`, bấm **Authorize**, nhập token (không cần gõ thêm chữ Bearer). Sau đó thử các endpoint cần quyền. Endpoint đọc catalog có thể gọi ngay không đăng nhập.

Ví dụ lọc: `/products?q=Nova&categoryId=1&minPrice=100000&maxPrice=900000&page=0&size=8&sort=price,asc`.

HTTP 201 kèm Location khi tạo, 204 không có body khi xóa; lỗi `{code,message,details}` với 400/401/403/404/422/500. Thiếu tồn dùng mã `ERR_STOCK_EMPTY`.

## Sinh record DTO bất biến

```powershell
mvn generate-sources
```

Maven plugin sinh interfaces và Java record DTO vào `target/generated-sources/openapi`. Template `codegen-templates/model.mustache` giữ validation từ schema. Mỗi fluent method tạo record mới, không sửa record cũ; danh sách được sao chép bằng `List.copyOf`. Không sửa file trong target. Cả template và hợp đồng được đóng gói để máy khác tái sinh được code.

`ShopService`/`AuthService` mặc định `@Transactional(readOnly=true)`; các thao tác ghi ghi đè `@Transactional`. Checkout và hủy đơn khóa bản ghi theo thứ tự ID tăng dần, cập nhật đơn/tồn trong cùng transaction. `open-in-view=false`; DTO ánh xạ bên trong transaction. Read-only là gợi ý tối ưu Hibernate, không thay thế phân quyền.

## Kiểm thử và bài nộp

```powershell
mvn test
mvn package
npm run test:contract
npm run test:contract:backend
```

- **5 unit test**: thành tiền/tồn kho, hết hàng, chuyển trạng thái, lọc giá sai, record/list bất biến.
- **8 integration test**: CRUD/validation, rollback, hủy lặp, token/logout/quyền sở hữu, PAID/SHIPPED, lọc và checkout đồng thời.
- **Newman mock**: 26 request / 58 assertion theo schema OpenAPI.
- **Newman backend**: 28 request bao phủ 26 operation; tự lấy token/ID và tạo bản ghi rời để kiểm tra DELETE hợp lệ.
- **E2E đã thực thi bằng trình duyệt**: mock -> nhận mã đơn; FE thật -> checkout -> MySQL -> FE thành công; lịch sử/hủy; admin PAID/SHIPPED và sửa tồn. Xem `docs/E2E-RESULTS.md` và ảnh trong `docs/evidence`.

Collections nằm trong `tests/contracts`. Chạy mock và backend trước khi chạy Newman tương ứng. BDD Gherkin mô tả luồng tại `tests/contracts/checkout.feature`; phần Gherkin là đặc tả, không gắn runner riêng.

`scripts/smoke-api.py` là smoke test v2 đơn giản trên backend có xác thực. Chạy `node scripts/sanitize-evidence.cjs` sau Newman để che token trước khi đóng gói. Dùng `python scripts/package-submission.py` để tạo ZIP source, bỏ qua node_modules, .tools, database và target.

## Cách đọc code

Đọc `ShopApplication` -> `domain` -> `repository` -> `service/ShopService` -> `service/AuthService` -> `web/AccessInterceptor` -> các controller -> `static/shop.js` -> tests. Code bổ sung có comment tiếng Việt.

- `docs/BA-USER-STORIES.md`: BA, acceptance criteria, sơ đồ đặt/hủy/trạng thái.
- `docs/BAO-CAO.md`: đối chiếu rubric và kết quả thực nghiệm.
- `docs/E2E-RESULTS.md`: kịch bản tái hiện và bằng chứng chạy FE/BE/database.
- `docs/evidence`: báo cáo Newman, JUnit, log, SQL và screenshot thực tế.

Nguồn chính thức: [OpenAPI](https://spec.openapis.org/oas/v3.0.3.html), [OpenAPI Generator](https://openapi-generator.tech/docs/generators/spring/), [Datafaker](https://www.datafaker.net/documentation/usage/), [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html).
