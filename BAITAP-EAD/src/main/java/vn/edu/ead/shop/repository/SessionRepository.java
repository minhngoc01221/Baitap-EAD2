package vn.edu.ead.shop.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ead.shop.domain.SessionEntity;
public interface SessionRepository extends JpaRepository<SessionEntity,String> {}
