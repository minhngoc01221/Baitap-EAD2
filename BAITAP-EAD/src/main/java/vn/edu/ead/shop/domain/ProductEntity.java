package vn.edu.ead.shop.domain;

/** Entity sản phẩm: lưu giá và số lượng tồn kho; dùng BigDecimal để tính tiền chính xác. */
import jakarta.persistence.*;
// Đánh dấu lớp là đối tượng được JPA quản lý và lưu xuống database.
@Entity
// Chọn tên bảng; uniqueConstraints (nếu có) ngăn trùng dữ liệu ở mức database.
@Table(name="products")
public class ProductEntity {
 // Quan hệ tùy chọn để dữ liệu từ phiên bản cũ vẫn đọc được sau khi nâng cấp.
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="category_id") public CategoryEntity category;
 @Column(length=500) public String imageUrl;
 // Đọc ID qua getter để tương thích với đối tượng proxy của Hibernate khi dùng LAZY.
 public Long getId() { return id; }
 // Khóa chính; IDENTITY để database tự tăng ID khi INSERT.
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 // Tên hiển thị; nullable=false bắt buộc có giá trị, length giới hạn số ký tự.
 @Column(nullable=false,length=150) public String name;
 // Mô tả tùy chọn của sản phẩm, tối đa 2.000 ký tự.
 @Column(length=2000) public String description;
 // Đơn giá: precision=15 là tổng số chữ số, scale=2 là số chữ số sau dấu phẩy.
 @Column(nullable=false,precision=15,scale=2) public java.math.BigDecimal price;
 // Số lượng còn trong kho; các thao tác đặt/hủy đơn sẽ thay đổi giá trị này.
 @Column(nullable=false) public int stock;
 // Khóa lạc quan: Hibernate kiểm tra version để phát hiện cập nhật bằng dữ liệu cũ.
 @Version public Long version;
}
