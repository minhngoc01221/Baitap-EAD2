package vn.edu.ead.shop.service;

/** Tầng nghiệp vụ. Luồng request: Controller -> Service -> Repository -> Database -> DTO phản hồi. */

import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import vn.edu.ead.shop.domain.*;
import vn.edu.ead.shop.repository.*;
import vn.edu.ead.shop.generated.model.*;

// Đăng ký lớp nghiệp vụ thành bean để Spring tiêm vào controller.
@Service
// Mặc định các hàm chạy trong giao dịch chỉ đọc; hỗ trợ Hibernate tối ưu flush/dirty checking. Hàm ghi có @Transactional riêng.
@Transactional(readOnly=true)
public class ShopService {
 // Dependency được giữ cố định và nhận qua constructor thay vì tự new repository/service.
 private final CustomerRepository customers;
 // Dependency được giữ cố định và nhận qua constructor thay vì tự new repository/service.
 private final ProductRepository products;
 // Dependency được giữ cố định và nhận qua constructor thay vì tự new repository/service.
 private final OrderRepository orders;
 private final CategoryRepository categories;
 // Constructor injection: Spring cung cấp ba repository đã tạo sẵn.
 public ShopService(CustomerRepository c, ProductRepository p, OrderRepository o,CategoryRepository cat) { customers=c; products=p; orders=o; categories=cat; }
 // Bỏ cảnh báo ép kiểu generic khi lấy entity thật từ Hibernate proxy.
 @SuppressWarnings("unchecked")
 // Hàm dùng chung: nếu Optional rỗng thì báo 404; unproxy lấy entity thật để truy cập các field an toàn.
 private <T> T required(Optional<T> value,String name) { return (T)org.hibernate.Hibernate.unproxy(value.orElseThrow(()->new ResponseStatusException(NOT_FOUND,name+" not found"))); }
 // Chuyển page/size/sort từ query string sang đối tượng phân trang của Spring Data.
 private Pageable paging(Integer page,Integer size,String sort,Set<String> allowed) {
  // Kiểm tra trang bắt đầu từ 0 và mỗi trang có 1-100 phần tử.
  if(page==null || page<0 || size==null || size<1 || size>100) throw new IllegalArgumentException("page >= 0; size between 1 and 100");
  // Tách "price,desc" thành tên thuộc tính và hướng sắp xếp; mặc định "id,asc".
  String[] parts=(sort==null?"id,asc":sort).split(",",-1);
  // Chỉ cho phép các trường sort trong danh sách allowed và hai hướng asc/desc.
  if(parts.length!=2 || !allowed.contains(parts[0]) || !(parts[1].equals("asc")||parts[1].equals("desc"))) throw new IllegalArgumentException("Invalid sort; use allowed field,asc or field,desc: "+allowed);
  // Tạo yêu cầu phân trang; repository sẽ áp dụng giới hạn và ORDER BY khi truy vấn.
  return PageRequest.of(page,size,Sort.by(Sort.Direction.fromString(parts[1]),parts[0]));
 }
 // Chuyển entity database sang DTO hợp đồng; không đưa entity JPA trực tiếp vào JSON.
 private Customer customerDto(CustomerEntity e) { return new Customer().id(e.id).name(e.name).email(e.email).address(e.address).phone(e.phone); }
 // Chọn các trường sản phẩm được công khai trong API.
 private Product productDto(ProductEntity e) { return new Product().id(e.id).name(e.name).description(e.description).price(e.price).stock(e.stock).categoryId(e.category==null?null:e.category.getId()).categoryName(e.category==null?"Chưa phân loại":((CategoryEntity)org.hibernate.Hibernate.unproxy(e.category)).name).imageUrl(e.imageUrl==null?"/images/keyboard.svg":e.imageUrl); }
 // Dựng DTO đơn trong transaction để có thể đọc danh sách items LAZY.
 private Order orderDto(OrderEntity e) {
  // Sao chép thông tin đầu đơn; đổi trạng thái String sang enum của DTO được sinh từ YAML.
  return new Order().id(e.id).customerId(e.customer.getId()).status(Order.StatusEnum.fromValue(e.status)).createdAt(e.createdAt).total(e.total)
   // stream/map chuyển từng OrderLineEntity thành OrderLine DTO rồi gom lại bằng toList.
   .items(e.items.stream().map(l->new OrderLine().productId(l.product.getId()).productName(l.productName).quantity(l.quantity).unitPrice(l.unitPrice).subtotal(l.subtotal)).toList());
 }
 // GET danh sách khách; chỉ cho sort theo id, name, email.
 public CustomerPage listCustomers(Integer page,Integer size,String sort) {
  // Lấy một trang dữ liệu; p chứa cả nội dung và metadata phân trang.
  var p=customers.findAll(paging(page,size,sort,Set.of("id","name","email")));
  // Đổi entity sang DTO và trả metadata thống nhất với hợp đồng OpenAPI.
  return new CustomerPage().content(p.map(this::customerDto).getContent()).page(p.getNumber()).size(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages());
 }
 // Tra khách theo ID; required sẽ báo 404 nếu không tồn tại.
 public Customer getCustomer(Long id) { return customerDto(required(customers.findById(id),"Customer")); }
 // Dùng chung kiểm tra và gán thông tin khách cho cả tạo mới và cập nhật.
 private void fillCustomer(CustomerEntity e,CustomerInput input) {
  // Chuẩn hóa email: bỏ khoảng trắng hai đầu, chuyển chữ thường không phụ thuộc ngôn ngữ máy.
  String email=input.getEmail().trim().toLowerCase(Locale.ROOT);
  // Email phải duy nhất; khách mới chưa có ID nên dùng -1 để không loại trừ bản ghi thật.
  if(customers.existsByEmailIgnoreCaseAndIdNot(email,e.id==null?-1L:e.id)) throw new BusinessException("Email already exists");
  // Không chấp nhận tên/địa chỉ chỉ gồm dấu cách; lỗi này trả HTTP 400.
  if(input.getName().isBlank()||input.getAddress().isBlank()) throw new IllegalArgumentException("Name and address must not be blank");
  // Gán dữ liệu đã chuẩn hóa vào entity được JPA quản lý.
  e.name=input.getName().trim(); e.email=email; e.address=input.getAddress().trim();e.phone=input.getPhone();
 }
 // Giao dịch ghi: tạo entity, kiểm tra dữ liệu, save để INSERT rồi trả DTO có ID.
 @Transactional public Customer createCustomer(CustomerInput input) { var e=new CustomerEntity();fillCustomer(e,input);return customerDto(customers.save(e)); }
 // Giao dịch ghi: lấy entity đang được quản lý rồi sửa field; Hibernate dirty checking tự UPDATE khi commit.
 @Transactional public Customer updateCustomer(Long id,CustomerInput input) { var e=required(customers.findById(id),"Customer");fillCustomer(e,input);return customerDto(e); }
 // Chỉ xóa khách chưa có đơn; có lịch sử thì ném BusinessException để trả 422.
 @Transactional public void deleteCustomer(Long id) { var e=required(customers.findById(id),"Customer"); if(orders.existsByCustomerId(id)) throw new BusinessException("Customer has historical orders"); customers.delete(e); }
 // Danh sách sản phẩm có phân trang và sort theo id, name, price hoặc stock.
 public ProductPage listProducts(Integer page,Integer size,String sort,String q,Long categoryId,BigDecimal minPrice,BigDecimal maxPrice) {
  // Bộ lọc kết hợp AND; Criteria API bind giá trị thay vì nối chuỗi SQL.
  if(minPrice!=null&&maxPrice!=null&&minPrice.compareTo(maxPrice)>0) throw new IllegalArgumentException("minPrice must not exceed maxPrice");
  // Repository truy vấn sản phẩm theo yêu cầu phân trang đã được kiểm tra.
  org.springframework.data.jpa.domain.Specification<ProductEntity> filter=(r,query,cb)->{
   var predicates=new ArrayList<jakarta.persistence.criteria.Predicate>();
   if(q!=null&&!q.isBlank()) predicates.add(cb.like(cb.lower(r.get("name")),"%"+q.toLowerCase(Locale.ROOT)+"%"));
   if(categoryId!=null) predicates.add(cb.equal(r.get("category").get("id"),categoryId));
   if(minPrice!=null) predicates.add(cb.greaterThanOrEqualTo(r.get("price"),minPrice));
   if(maxPrice!=null) predicates.add(cb.lessThanOrEqualTo(r.get("price"),maxPrice));
   return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
  };
  var p=products.findAll(filter,paging(page,size,sort,Set.of("id","name","price","stock")));
  // Ánh xạ nội dung và tổng số phần tử/trang sang response DTO.
  return new ProductPage().content(p.map(this::productDto).getContent()).page(p.getNumber()).size(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages());
 }
 // Lấy chi tiết sản phẩm hoặc báo 404.
 public Product getProduct(Long id) { return productDto(required(products.findById(id),"Product")); }
 // Dùng chung kiểm tra và gán dữ liệu sản phẩm cho POST/PUT.
 private void fillProduct(ProductEntity e,ProductInput input) {
  // Chặn tên sản phẩm chỉ có khoảng trắng.
  if(input.getName().isBlank()) throw new IllegalArgumentException("Name must not be blank");
  // Bỏ số 0 thập phân dư rồi kiểm tra giá không quá hai chữ số sau dấu phẩy.
  if(input.getPrice().stripTrailingZeros().scale()>2) throw new IllegalArgumentException("Price supports at most two decimal places");
  // Gán tên, mô tả, giá và tồn kho từ request vào entity.
  e.name=input.getName().trim();e.description=input.getDescription();e.price=input.getPrice();e.stock=input.getStock();
  e.category=required(categories.findById(input.getCategoryId()),"Category");e.imageUrl=input.getImageUrl();
 }
 // Tạo sản phẩm trong transaction ghi; save thực hiện INSERT.
 @Transactional public Product createProduct(ProductInput input) { var e=new ProductEntity(); fillProduct(e,input);return productDto(products.save(e)); }
 // Khóa sản phẩm trước khi thay giá/tồn để không tranh chấp với đặt hàng.
 @Transactional public Product updateProduct(Long id,ProductInput input) { var e=required(products.findLockedById(id),"Product"); fillProduct(e,input);return productDto(e); }
 // Khóa và kiểm tra lịch sử trước DELETE; sản phẩm đã có trong đơn không được xóa.
 @Transactional public void deleteProduct(Long id) { var e=required(products.findLockedById(id),"Product"); if(orders.usesProduct(id)) throw new BusinessException("Product has historical orders"); products.delete(e); }
 // Hàm dùng chung để đổi Page<OrderEntity> sang response phân trang OrderPage.
 private OrderPage orderPage(Page<OrderEntity> p) { return new OrderPage().content(p.map(this::orderDto).getContent()).page(p.getNumber()).size(p.getSize()).totalElements(p.getTotalElements()).totalPages(p.getTotalPages()); }
 // Liệt kê đơn; chỉ cho sort theo các field hợp lệ của OrderEntity.
 public OrderPage listOrders(Integer page,Integer size,String sort) { return orderPage(orders.findAll(paging(page,size,sort,Set.of("id","createdAt","total","status")))); }
 // Kiểm tra khách tồn tại trước khi lấy đơn: khách không có đơn trả trang rỗng, khách không tồn tại trả 404.
 public OrderPage listCustomerOrders(Long id,Integer page,Integer size,String sort) { required(customers.findById(id),"Customer");return orderPage(orders.findByCustomerId(id,paging(page,size,sort,Set.of("id","createdAt","total","status")))); }
 // Lấy đơn theo ID và chuyển sang DTO trong giao dịch chỉ đọc.
 public Order getOrder(Long id) { return orderDto(required(orders.findById(id),"Order")); }
 // Một transaction bao gồm kiểm tra tồn, trừ tồn, tạo đơn và lưu chi tiết; lỗi ở bất kỳ bước nào sẽ rollback toàn bộ.
 @Transactional public Order createOrder(OrderInput input) {
  // Tạo đơn PENDING, xác nhận khách tồn tại và bắt đầu tổng tiền từ 0.
  var e=new OrderEntity();e.customer=required(customers.findById(input.getCustomerId()),"Customer");e.total=BigDecimal.ZERO;
  // TreeMap tự sắp ID tăng dần để mọi giao dịch khóa sản phẩm cùng thứ tự, giảm nguy cơ deadlock.
  Map<Long,Integer> quantities=new TreeMap<>();
  // Duyệt các dòng request để kiểm tra trước khi trừ tồn.
  for(var line:input.getItems()) {
   // Không cho mảng items chứa phần tử null.
   if(line==null) throw new IllegalArgumentException("Order items must not contain null");
   // Mỗi productId chỉ có một dòng; nếu đã tồn tại trong map thì báo lỗi nghiệp vụ.
   if(quantities.putIfAbsent(line.getProductId(),line.getQuantity())!=null) throw new BusinessException("Duplicate product in order");
  }
  // Duyệt các sản phẩm theo thứ tự ID đã sắp xếp.
  for(var entry:quantities.entrySet()) {
   // Khóa bản ghi sản phẩm cho tới commit/rollback và lấy số lượng khách muốn mua.
   var product=required(products.findLockedById(entry.getKey()),"Product"); int qty=entry.getValue();
   // Thiếu tồn thì ném ngoại lệ; các sản phẩm bị trừ trước đó cũng được rollback.
   if(product.stock<qty) throw new BusinessException("ERR_STOCK_EMPTY","Sản phẩm không đủ tồn kho: "+product.id);
   // Trừ tồn trên entity đang được quản lý; Hibernate tự ghi thay đổi khi transaction commit.
   product.stock-=qty;
   // Lưu bản chụp tên/giá mua; subtotal = đơn giá × số lượng bằng BigDecimal.
   var line=new OrderLineEntity(); line.order=e;line.product=product;line.productName=product.name;line.quantity=qty;line.unitPrice=product.price;line.subtotal=product.price.multiply(BigDecimal.valueOf(qty));
   // Thêm dòng vào đơn và cộng tiền dòng vào tổng tiền.
   e.items.add(line);e.total=e.total.add(line.subtotal);
  }
  // Lưu đơn; cascade ALL tự lưu các dòng chi tiết liên kết với đơn.
  return orderDto(orders.save(e));
 }
 // Khóa đơn và cập nhật trạng thái/hoàn tồn trong cùng một giao dịch.
 @Transactional public Order updateOrderStatus(Long id,OrderStatusInput input) {
  // Khóa đơn trước khi kiểm tra trạng thái, tránh hai yêu cầu cùng hủy và hoàn tồn hai lần.
  var e=required(orders.findLockedById(id),"Order");String next=input.getStatus().getValue();
  // Trạng thái đã đúng thì trả ngay: gửi lặp yêu cầu hủy không hoàn tồn thêm.
  if(e.status.equals(next)) return orderDto(e);
  // Kiểm tra trạng thái đích được phép từ trạng thái hiện tại.
  boolean valid=switch(e.status) {
   // Đơn mới chỉ được đánh dấu đã thanh toán hoặc hủy.
   case "PENDING" -> next.equals("PAID")||next.equals("CANCELLED");
   // Đơn đã thanh toán chỉ được giao hàng hoặc hủy.
   case "PAID" -> next.equals("SHIPPED")||next.equals("CANCELLED");
   // Đơn đã giao không được hủy để hoàn tồn sai nghiệp vụ.
   case "SHIPPED" -> false;
   // SHIPPED và CANCELLED là trạng thái kết thúc, không được chuyển tiếp.
   default -> false;
  };
  // Chuyển trạng thái trái quy tắc trả 422, transaction bị rollback.
  if(!valid) throw new BusinessException("Invalid transition: "+e.status+" -> "+next);
  // Chỉ nhánh hủy đơn mới cộng lại tồn kho.
  if(next.equals("CANCELLED")) {
   // Khóa từng sản phẩm theo ID tăng dần, kiểm tra tràn số int rồi cộng lại quantity đã mua.
   e.items.stream().sorted(Comparator.comparing(l->l.product.getId())).forEach(l->{var p=required(products.findLockedById(l.product.getId()),"Product"); if(p.stock>Integer.MAX_VALUE-l.quantity) throw new BusinessException("Stock overflow");p.stock+=l.quantity;});
  }
  // Gán trạng thái mới; dirty checking tự UPDATE khi kết thúc transaction, sau đó trả DTO.
  e.status=next; return orderDto(e);
 }

 // Cập nhật riêng tồn kho để admin không phải gửi lại toàn bộ thông tin sản phẩm.
 @Transactional public Product updateProductStock(Long id,StockInput input) {
  var e=required(products.findLockedById(id),"Product");e.stock=input.getStock();return productDto(e);
 }
 private Category categoryDto(CategoryEntity e) { return new Category().id(e.id).name(e.name).description(e.description); }
 public java.util.List<Category> listCategories() { return categories.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(this::categoryDto).toList(); }
 public Category getCategory(Long id) { return categoryDto(required(categories.findById(id),"Category")); }
 private void fillCategory(CategoryEntity e,CategoryInput input) {
  if(input.getName().isBlank()) throw new IllegalArgumentException("Category name must not be blank");
  e.name=input.getName().trim();e.description=input.getDescription();
 }
 @Transactional public Category createCategory(CategoryInput input) { var e=new CategoryEntity();fillCategory(e,input);return categoryDto(categories.save(e)); }
 @Transactional public Category updateCategory(Long id,CategoryInput input) { var e=required(categories.findById(id),"Category");fillCategory(e,input);return categoryDto(e); }
 @Transactional public void deleteCategory(Long id) {
  var e=required(categories.findById(id),"Category");if(products.existsByCategoryId(id))throw new BusinessException("Category contains products");categories.delete(e);
 }
}
