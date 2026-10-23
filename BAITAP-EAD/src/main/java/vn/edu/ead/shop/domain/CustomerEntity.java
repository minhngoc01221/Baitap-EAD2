package vn.edu.ead.shop.domain;

/** Entity ánh xạ hồ sơ khách hàng sang bảng customers. Mỗi đối tượng tương ứng một bản ghi. */
import jakarta.persistence.*;
// Đánh dấu lớp là đối tượng được JPA quản lý và lưu xuống database.
@Entity
// Chọn tên bảng; uniqueConstraints (nếu có) ngăn trùng dữ liệu ở mức database.
@Table(name="customers", uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class CustomerEntity {
 // Thông tin đăng nhập không được đưa vào DTO/JSON phản hồi.
 @Column(length=16) public String phone;
 @Column(length=100) public String passwordHash;
 @Column(nullable=false) public String role="CUSTOMER";
 // Đọc ID qua getter để tương thích với đối tượng proxy của Hibernate khi dùng LAZY.
 public Long getId() { return id; }
 // Khóa chính; IDENTITY để database tự tăng ID khi INSERT.
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 // Tên hiển thị; nullable=false bắt buộc có giá trị, length giới hạn số ký tự.
 @Column(nullable=false,length=100) public String name;
 // Email khách hàng; độ dài tối đa 150 ký tự, tính duy nhất được ràng buộc ở bảng.
 @Column(nullable=false,length=150) public String email;
 // Địa chỉ khách hàng, bắt buộc có giá trị và tối đa 255 ký tự.
 @Column(nullable=false,length=255) public String address;
}
