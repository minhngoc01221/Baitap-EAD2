package vn.edu.ead.shop.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ead.shop.domain.CategoryEntity;
public interface CategoryRepository extends JpaRepository<CategoryEntity,Long> {}
