package vn.edu.ead.shop.web;

/** Nhận HTTP cho đơn hàng. Triển khai OrdersApi sinh từ hợp đồng OpenAPI để compiler kiểm tra chữ ký. */
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ead.shop.generated.api.OrdersApi;
import vn.edu.ead.shop.generated.model.*;
import vn.edu.ead.shop.service.ShopService;
// Trả dữ liệu JSON; mọi đường dẫn kế thừa từ interface được thêm tiền tố /api/v1.
@RestController @RequestMapping("/api/v1")
public class OrdersController implements OrdersApi {
 // Dependency được giữ cố định và nhận qua constructor thay vì tự new repository/service.
 private final ShopService service;
 private final vn.edu.ead.shop.service.AuthService auth;
 private final jakarta.servlet.http.HttpServletRequest request;
 // Constructor injection: Spring truyền ShopService vào khi tạo controller.
 public OrdersController(ShopService service,vn.edu.ead.shop.service.AuthService auth,jakarta.servlet.http.HttpServletRequest request) { this.service=service;this.auth=auth;this.request=request; }
 // GET danh sách: nhận page/size/sort, gọi service rồi trả 200 cùng dữ liệu phân trang.
 @Override public ResponseEntity<OrderPage> listOrders(Integer page,Integer size,String sort) { return ResponseEntity.ok(service.listOrders(page,size,sort)); }
 // GET chi tiết theo ID: service tìm dữ liệu; thành công trả 200, không có trả 404 qua handler.
 @Override public ResponseEntity<Order> getOrder(Long id) { return ResponseEntity.ok(service.getOrder(id)); }
 // POST đặt hàng: kiểm tra người sở hữu, tạo đơn/trừ tồn cùng transaction, trả 201 + Location.
 @Override public ResponseEntity<Order> createOrder(OrderInput input) { auth.requireOwner(auth.authenticate(request),input.getCustomerId()); var result=service.createOrder(input);return ResponseEntity.created(URI.create("/api/v1/orders/"+result.getId())).body(result); }
 // PATCH /orders/{id}: chỉ cập nhật trạng thái, service kiểm tra quy tắc và trả 200.
 @Override public ResponseEntity<Order> updateOrderStatus(Long id,OrderStatusInput input) { if(!"ADMIN".equals(auth.authenticate(request).role)&&!"CANCELLED".equals(input.getStatus().getValue()))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Only admin may mark an order paid/shipped"); return ResponseEntity.ok(service.updateOrderStatus(id,input)); }
}
