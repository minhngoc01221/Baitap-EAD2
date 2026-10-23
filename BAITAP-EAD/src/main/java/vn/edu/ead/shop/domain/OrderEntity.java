package vn.edu.ead.shop.domain;

/** Entity đơn hàng: liên kết khách hàng với các dòng sản phẩm và lưu trạng thái, tổng tiền. */
import jakarta.persistence.*;
// Đánh dấu lớp là đối tượng được JPA quản lý và lưu xuống database.
@Entity
// Chọn tên bảng; uniqueConstraints (nếu có) ngăn trùng dữ liệu ở mức database.
@Table(name="shop_orders")
public class OrderEntity {
 // Khóa chính; IDENTITY để database tự tăng ID khi INSERT.
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 // Nhiều bản ghi cùng tham chiếu một đối tượng; LAZY tải quan hệ khi cần, JoinColumn là cột khóa ngoại.
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id") public CustomerEntity customer;
 // Trạng thái nghiệp vụ; đơn mới mặc định PENDING.
 @Column(nullable=false) public String status="PENDING";
 // Thời điểm tạo đơn theo UTC, giúp dữ liệu nhất quán giữa các múi giờ.
 @Column(nullable=false) public java.time.OffsetDateTime createdAt=java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
 // Tổng tiền của đơn; dùng kiểu số thập phân chính xác với hai chữ số lẻ.
 @Column(nullable=false,precision=19,scale=2) public java.math.BigDecimal total;
 // Một đơn có nhiều dòng: mappedBy trỏ tới thuộc tính order; cascade lưu/xóa dòng theo đơn; orphanRemoval xóa dòng bị bỏ khỏi danh sách.
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true) public java.util.List<OrderLineEntity> items=new java.util.ArrayList<>();
}
