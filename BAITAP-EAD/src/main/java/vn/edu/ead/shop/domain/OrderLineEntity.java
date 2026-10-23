package vn.edu.ead.shop.domain;

/** Một dòng chi tiết đơn: lưu sản phẩm, số lượng và bản chụp tên/giá tại thời điểm mua. */
import jakarta.persistence.*;
// Đánh dấu lớp là đối tượng được JPA quản lý và lưu xuống database.
@Entity
// Chọn tên bảng; uniqueConstraints (nếu có) ngăn trùng dữ liệu ở mức database.
@Table(name="order_lines")
public class OrderLineEntity {
 // Khóa chính; IDENTITY để database tự tăng ID khi INSERT.
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 // Nhiều bản ghi cùng tham chiếu một đối tượng; LAZY tải quan hệ khi cần, JoinColumn là cột khóa ngoại.
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id") public OrderEntity order;
 // Nhiều bản ghi cùng tham chiếu một đối tượng; LAZY tải quan hệ khi cần, JoinColumn là cột khóa ngoại.
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id") public ProductEntity product;
 // Tên sản phẩm tại thời điểm đặt; đổi tên sản phẩm sau này không làm đổi lịch sử.
 @Column(nullable=false) public String productName;
 // Số lượng mua của dòng đơn này.
 @Column(nullable=false) public int quantity;
 // Đơn giá tại thời điểm mua, không lấy lại giá mới khi đọc đơn cũ.
 @Column(nullable=false,precision=15,scale=2) public java.math.BigDecimal unitPrice;
 // Thành tiền của dòng: unitPrice nhân quantity.
 @Column(nullable=false,precision=19,scale=2) public java.math.BigDecimal subtotal;
}
