package vn.edu.ead.shop.web;

/** Nhận HTTP cho khách hàng, gọi service và trả HTTP status/body. Mapping và validation kế thừa CustomersApi sinh từ YAML. */
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ead.shop.generated.api.CustomersApi;
import vn.edu.ead.shop.generated.model.*;
import vn.edu.ead.shop.service.ShopService;
// Trả dữ liệu JSON; mọi đường dẫn kế thừa từ interface được thêm tiền tố /api/v1.
@RestController @RequestMapping("/api/v1")
public class CustomersController implements CustomersApi {
 // Dependency được giữ cố định và nhận qua constructor thay vì tự new repository/service.
 private final ShopService service;
 // Constructor injection: Spring truyền ShopService vào khi tạo controller.
 public CustomersController(ShopService service) { this.service=service; }
 // GET danh sách: nhận page/size/sort, gọi service rồi trả 200 cùng dữ liệu phân trang.
 @Override public ResponseEntity<CustomerPage> listCustomers(Integer page,Integer size,String sort) { return ResponseEntity.ok(service.listCustomers(page,size,sort)); }
 // GET chi tiết theo ID: service tìm dữ liệu; thành công trả 200, không có trả 404 qua handler.
 @Override public ResponseEntity<Customer> getCustomer(Long id) { return ResponseEntity.ok(service.getCustomer(id)); }
 // POST tạo khách hàng: trả 201 cùng Location.
 @Override public ResponseEntity<Customer> createCustomer(CustomerInput input) { var result=service.createCustomer(input);return ResponseEntity.created(URI.create("/api/v1/customers/"+result.getId())).body(result); }
 // PUT: thay thế thông tin bằng request hợp lệ và trả 200 cùng DTO sau cập nhật.
 @Override public ResponseEntity<Customer> updateCustomer(Long id,CustomerInput input) { return ResponseEntity.ok(service.updateCustomer(id,input)); }
 // DELETE: gọi service xóa rồi trả 204 No Content, không gửi body.
 @Override public ResponseEntity<Void> deleteCustomer(Long id) { service.deleteCustomer(id);return ResponseEntity.noContent().build(); }
 // GET /customers/{id}/orders: trả HTTP 200 cùng trang đơn hàng của khách.
 @Override public ResponseEntity<OrderPage> listCustomerOrders(Long id,Integer page,Integer size,String sort) { return ResponseEntity.ok(service.listCustomerOrders(id,page,size,sort)); }
}
