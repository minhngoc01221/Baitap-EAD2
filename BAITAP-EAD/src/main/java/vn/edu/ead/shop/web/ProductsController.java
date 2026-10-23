package vn.edu.ead.shop.web;

/** Nhận HTTP cho sản phẩm. Chỉ điều phối request/response; nghiệp vụ nằm trong ShopService. */
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ead.shop.generated.api.ProductsApi;
import vn.edu.ead.shop.generated.model.*;
import vn.edu.ead.shop.service.ShopService;
// Trả dữ liệu JSON; mọi đường dẫn kế thừa từ interface được thêm tiền tố /api/v1.
@RestController @RequestMapping("/api/v1")
public class ProductsController implements ProductsApi {
 // Dependency được giữ cố định và nhận qua constructor thay vì tự new repository/service.
 private final ShopService service;
 // Constructor injection: Spring truyền ShopService vào khi tạo controller.
 public ProductsController(ShopService service) { this.service=service; }
 // GET danh sách: nhận page/size/sort, gọi service rồi trả 200 cùng dữ liệu phân trang.
 @Override public ResponseEntity<ProductPage> listProducts(Integer page,Integer size,String sort,String q,Long categoryId,java.math.BigDecimal minPrice,java.math.BigDecimal maxPrice) { return ResponseEntity.ok(service.listProducts(page,size,sort,q,categoryId,minPrice,maxPrice)); }
 // GET chi tiết theo ID: service tìm dữ liệu; thành công trả 200, không có trả 404 qua handler.
 @Override public ResponseEntity<Product> getProduct(Long id) { return ResponseEntity.ok(service.getProduct(id)); }
 // POST tạo sản phẩm: trả 201 và Location trỏ tới sản phẩm mới.
 @Override public ResponseEntity<Product> createProduct(ProductInput input) { var result=service.createProduct(input);return ResponseEntity.created(URI.create("/api/v1/products/"+result.getId())).body(result); }
 // PUT: thay thế thông tin bằng request hợp lệ và trả 200 cùng DTO sau cập nhật.
 @Override public ResponseEntity<Product> updateProduct(Long id,ProductInput input) { return ResponseEntity.ok(service.updateProduct(id,input)); }
 // DELETE: gọi service xóa rồi trả 204 No Content, không gửi body.
 @Override public ResponseEntity<Void> deleteProduct(Long id) { service.deleteProduct(id);return ResponseEntity.noContent().build(); }
 
 // PATCH tồn kho chỉ được gọi bởi ADMIN (interceptor kiểm tra quyền).
 @Override public ResponseEntity<Product> updateProductStock(Long id,StockInput input) { return ResponseEntity.ok(service.updateProductStock(id,input)); }
}
