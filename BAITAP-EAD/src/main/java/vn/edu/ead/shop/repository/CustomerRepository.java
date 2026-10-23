package vn.edu.ead.shop.repository;

/** Tầng truy cập dữ liệu khách hàng. JpaRepository cung cấp sẵn findById, findAll, save, delete. */
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ead.shop.domain.CustomerEntity;
// Kế thừa JpaRepository<Entity, Long>: Entity là kiểu dữ liệu, Long là kiểu khóa chính.
public interface CustomerRepository extends JpaRepository<CustomerEntity,Long> {
 java.util.Optional<CustomerEntity> findByEmailIgnoreCase(String email);
 // Spring Data tự tạo SQL từ tên hàm: kiểm tra email trùng, không phân biệt hoa thường, bỏ qua ID đang sửa.
 boolean existsByEmailIgnoreCaseAndIdNot(String email,Long id);
}
