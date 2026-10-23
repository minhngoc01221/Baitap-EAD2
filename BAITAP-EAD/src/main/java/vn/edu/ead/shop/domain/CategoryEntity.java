package vn.edu.ead.shop.domain;
import jakarta.persistence.*;
/** Danh mục dùng chung cho nhiều sản phẩm. */
@Entity @Table(name="categories")
public class CategoryEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=100) public String name;
 @Column(length=1000) public String description;
 public Long getId() { return id; }
}
