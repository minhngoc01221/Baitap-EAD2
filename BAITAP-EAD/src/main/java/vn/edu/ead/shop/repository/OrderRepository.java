package vn.edu.ead.shop.repository;

/** Tầng truy cập đơn hàng: phân trang, kiểm tra lịch sử và khóa đơn trước khi cập nhật. */
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.edu.ead.shop.domain.OrderEntity;
// Kế thừa JpaRepository<Entity, Long>: Entity là kiểu dữ liệu, Long là kiểu khóa chính.
public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
 // Lấy đơn của một khách theo Pageable; Page chứa cả dữ liệu và thông tin tổng số trang.
 Page<OrderEntity> findByCustomerId(Long id,Pageable pageable);
 // Kiểm tra khách đã có đơn để ngăn xóa mất liên kết lịch sử.
 boolean existsByCustomerId(Long id);
 // JPQL truy vấn theo tên entity/thuộc tính Java, không dùng trực tiếp tên bảng/cột SQL.
 @Query("select (count(l)>0) from OrderLineEntity l where l.product.id=:id")
 // Trả true nếu sản phẩm xuất hiện trong chi tiết đơn hàng.
 boolean usesProduct(@Param("id") Long id);
 // Khóa ghi bi quan giữ tới cuối transaction: giao dịch khác phải chờ khi sửa cùng bản ghi.
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select o from OrderEntity o where o.id=:id")
 // Optional biểu diễn có hoặc không có bản ghi; service đổi trường hợp rỗng thành HTTP 404.
 Optional<OrderEntity> findLockedById(@Param("id") Long id);
}
