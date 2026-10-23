package vn.edu.ead.shop.repository;

/** Tầng truy cập sản phẩm; bổ sung truy vấn có khóa để tránh hai giao dịch cùng sửa tồn kho. */
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.edu.ead.shop.domain.ProductEntity;
// Kế thừa JpaRepository<Entity, Long>: Entity là kiểu dữ liệu, Long là kiểu khóa chính.
public interface ProductRepository extends JpaRepository<ProductEntity,Long>, JpaSpecificationExecutor<ProductEntity> {
 boolean existsByCategoryId(Long id);
 // Khóa ghi bi quan giữ tới cuối transaction: giao dịch khác phải chờ khi sửa cùng bản ghi.
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from ProductEntity p where p.id=:id")
 // Optional biểu diễn có hoặc không có bản ghi; service đổi trường hợp rỗng thành HTTP 404.
 Optional<ProductEntity> findLockedById(@Param("id") Long id);
}
