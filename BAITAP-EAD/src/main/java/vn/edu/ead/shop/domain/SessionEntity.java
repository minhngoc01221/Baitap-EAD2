package vn.edu.ead.shop.domain;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
/** Chỉ lưu hash SHA-256 của token; không lưu token gốc dùng để xác thực. */
@Entity @Table(name="account_sessions")
public class SessionEntity {
 @Id @Column(length=64) public String tokenHash;
 @ManyToOne(fetch=FetchType.EAGER,optional=false) public CustomerEntity customer;
 @Column(nullable=false) public OffsetDateTime expiresAt;
}
