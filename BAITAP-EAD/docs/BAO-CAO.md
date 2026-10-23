# Báo cáo nâng cấp fullstack — NOVA Shop

Ngày thực hiện: **16/10/2026**. Nâng cấp từ Shop API ban đầu lên đầy đủ storefront/admin theo đề fullstack mới, dùng lại MySQL và giữ dữ liệu cũ.

## 1. Đối chiếu rubric

| Tiêu chí | Phần đã thực hiện | Bằng chứng |
|---|---|---|
| BA & Flow (1.5) | 14 user stories, acceptance criteria, business rules, sơ đồ checkout/hủy/trạng thái, ERD | BA-USER-STORIES.md |
| API-First (2.5) | OpenAPI 3.0.3, 26 operation; mock ghép nối UI; Postman/Newman schema test | shop-api.yaml; contract-newman.json; mock-ui-success.png |
| Backend & JPA (2.5) | Codegen interface và record DTO bất biến; readOnly ở service; transaction checkout/hủy; pessimistic locks; Datafaker CommandLineRunner | pom.xml; codegen-templates; source service/config; JUnit |
| Frontend (2.0) | Storefront, tìm/lọc/chi tiết, giỏ động, hoàn tất đơn; account/history; admin catalog/stock/orders; hai Base URL | static/index.html, shop.css, shop.js; ảnh UI |
| Testing & hồ sơ (1.5) | 5 unit + 8 integration; Newman mock/backend; E2E thật với đối chiếu SQL | evidence và E2E-RESULTS.md |

## 2. Thiết kế và quyết định nghiệp vụ

Luồng trạng thái mới là **PENDING -> PAID -> SHIPPED**, hủy từ PENDING/PAID. Mã cũ CONFIRMED và COMPLETED được ánh xạ tương ứng PAID và SHIPPED lúc khởi động. Không xóa đơn/sản phẩm/khách cũ. Phone và danh mục/ảnh còn thiếu của dữ liệu cũ được bổ sung.

Tài khoản mới luôn CUSTOMER; ADMIN chỉ tạo từ seeder. Xác thực Bearer session 8 giờ; mật khẩu BCrypt, token ngẫu nhiên 32 byte và database chỉ lưu hash SHA-256. AccessInterceptor kiểm tra quyền ở server, giới hạn hồ sơ/đơn theo chủ sở hữu. Không dùng cookie đăng nhập tự gửi. UI không hiển thị token trong nhật ký.

Giá/tồn được lấy lại ở server khi checkout. TreeMap sắp khóa sản phẩm tăng dần; thiếu tồn trả ERR_STOCK_EMPTY và rollback toàn bộ. Hủy khóa đơn trước, kiểm tra trạng thái, khóa sản phẩm và hoàn tồn trong cùng transaction; yêu cầu lặp trả trạng thái hiện tại. BigDecimal và bản chụp giá ở OrderLine bảo toàn lịch sử.

## 3. API-First và DTO bất biến

Hợp đồng YAML là nguồn gốc của interface, schema, request/response example và Postman schema assertions. Quá trình nâng cấp mở rộng hợp đồng và sinh collection trước khi viết implementation mới. Không tuyên bố quy trình của dự án cũ được thực hiện lại từ đầu.

Maven dùng OpenAPI Generator 7.14.0 với template model.mustache riêng sinh **Java record**. Fluent method trả bản sao, danh sách List.copyOf, không có setters. Bean Validation tái sử dụng partial chuẩn của generator từ ràng buộc OpenAPI. Có unit test xác minh `isRecord`, object gốc không đổi và collection không sửa được.

## 4. Seeding và UI

CommandLineRunner dùng Datafaker với seed ổn định để tạo 4 danh mục, 16 sản phẩm và khách mẫu, kèm tài khoản admin/demo. Kiểm tra dữ liệu tồn tại trước khi tạo nên restart không nhân đôi catalog hoặc đặt lại mật khẩu. Khi nâng cấp database hiện có, tổng sản phẩm còn bao gồm sản phẩm cũ và dữ liệu test.

SPA dùng HTML5/CSS/JS, responsive, SVG nội bộ tự dựng; không cần dịch vụ ảnh hoặc frontend build. Giỏ localStorage, token sessionStorage. Giao diện gồm đăng ký/đăng nhập, profile/history, bộ lọc kết hợp, chi tiết/giỏ/checkout, admin CRUD và sửa tồn. Mock Base URL và backend Base URL dùng cùng code FE.

## 5. Kiểm thử thực tế

Môi trường: JDK 24.0.1 biên dịch **release 21**, Maven 3.9.16, Spring Boot 3.5.3. Backend thật dùng MySQL 8.0.46 cổng 3307, còn JUnit integration dùng H2 profile demo.

- **Unit:** 5 test, gồm thành tiền/tồn, thiếu hàng không save, trạng thái kết thúc, range giá và record bất biến.
- **Integration:** 8 test, gồm validation/CRUD, rollback nhiều dòng, hủy lặp, Bearer logout/quyền sở hữu, PAID/SHIPPED, lọc, concurrent checkout. Hai giao dịch tranh cùng sản phẩm chỉ một được mua, stock không âm.
- **Mock contract:** 26 request, 58 assertion, kiểm status/Location/no body và JSON Schema từ hợp đồng.
- **Backend contract:** 28 request, 64 assertion đạt cho đủ 26 operation; có thêm create tạm để DELETE 204 không vi phạm lịch sử. Báo cáo chi tiết nằm trong `contract-backend-newman.json`.
- **E2E:** đã thao tác FE mock tới mã đơn; FE MySQL checkout đơn #4 total 700.000, quantity 2 và stock 15 -> 13; khách hủy; admin đưa đơn #5 tới SHIPPED và sửa stock thành 16. SQL và ảnh trong hồ sơ.

Kết quả cuối và số assertion backend được lưu trong các báo cáo Newman/JUnit, không suy diễn từ việc compile. Các ảnh là screenshot thực tế, không phải mockup thiết kế.

## 6. Các lỗi được bắt theo hướng Fail-Fast

| Phát hiện | Cách sửa |
|---|---|
| Phone mẫu bị parser mock coi là số và mất 0 đầu | Đặt dấu nháy rõ ràng cho chuỗi phone; Newman schema test phát hiện trước |
| Response gửi null cho description tùy chọn, trong khi schema không nullable | Cấu hình Jackson bỏ trường null; kiểm tra lại với backend collection |
| DTO codegen mặc định có setters | Template record bất biến và unit test tính bất biến |
| Checkout cạnh tranh tồn | Pessimistic lock, thứ tự khóa ổn định và integration test hai luồng |
| Dữ liệu cũ khác enum mới | Migration ánh xạ trạng thái trong seeder, có backup database trước nâng cấp |

## 7. Hồ sơ và giới hạn

Tài liệu BA: [BA-USER-STORIES.md](BA-USER-STORIES.md). E2E: [E2E-RESULTS.md](E2E-RESULTS.md). Hướng dẫn chạy, tài khoản mẫu và Swagger authorization: README.

Chưa tích hợp cổng thanh toán/giao vận/reset mật khẩu/upload ảnh. PAID là thao tác xác nhận của admin trong bài học. E2E được thực thi qua trình duyệt trong phiên, có hướng dẫn tái hiện; không đóng gói runner trình duyệt standalone. Các tài khoản demo chỉ dùng cục bộ. Bản sao database không nằm trong ZIP bài nộp.
