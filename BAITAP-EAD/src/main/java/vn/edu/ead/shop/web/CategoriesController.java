package vn.edu.ead.shop.web;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ead.shop.generated.api.CategoriesApi;
import vn.edu.ead.shop.generated.model.*;
import vn.edu.ead.shop.service.ShopService;
/** Danh mục: đọc công khai, thay đổi chỉ dành cho ADMIN. */
@RestController @RequestMapping("/api/v1")
public class CategoriesController implements CategoriesApi {
 private final ShopService shop;
 public CategoriesController(ShopService s) {shop=s;}
 @Override public ResponseEntity<List<Category>> listCategories() {return ResponseEntity.ok(shop.listCategories());}
 @Override public ResponseEntity<Category> getCategory(Long id) {return ResponseEntity.ok(shop.getCategory(id));}
 @Override public ResponseEntity<Category> createCategory(CategoryInput input) {var c=shop.createCategory(input);return ResponseEntity.created(URI.create("/api/v1/categories/"+c.getId())).body(c);}
 @Override public ResponseEntity<Category> updateCategory(Long id,CategoryInput input) {return ResponseEntity.ok(shop.updateCategory(id,input));}
 @Override public ResponseEntity<Void> deleteCategory(Long id) {shop.deleteCategory(id);return ResponseEntity.noContent().build();}
}
