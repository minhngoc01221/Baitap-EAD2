package vn.edu.ead.shop.service;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import vn.edu.ead.shop.domain.*;
import vn.edu.ead.shop.repository.*;
import vn.edu.ead.shop.generated.model.*;

/** Phiên Bearer hết hạn sau 8 giờ, lưu bền trong MySQL và có thể thu hồi khi đăng xuất. */
@Service @Transactional(readOnly=true)
public class AuthService {
 private final CustomerRepository customers;
 private final SessionRepository sessions;
 private final ShopService shop;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
 public AuthService(CustomerRepository c,SessionRepository s,ShopService shop) { customers=c;sessions=s;this.shop=shop; }
 private String hash(String token) {
  try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
  catch(NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
 }
 public CustomerEntity authenticate(HttpServletRequest request) {
  var header=request.getHeader("Authorization");
  if(header==null||!header.startsWith("Bearer ")) throw new ResponseStatusException(UNAUTHORIZED,"Please sign in");
  var session=sessions.findById(hash(header.substring(7))).orElseThrow(()->new ResponseStatusException(UNAUTHORIZED,"Invalid session"));
  if(!session.expiresAt.isAfter(OffsetDateTime.now(ZoneOffset.UTC)))throw new ResponseStatusException(UNAUTHORIZED,"Session expired");
  return session.customer;
 }
 public void requireOwner(CustomerEntity actor,Long id) {
  if(!"ADMIN".equals(actor.role)&&!actor.id.equals(id))throw new ResponseStatusException(FORBIDDEN,"You may only access your own data");
 }
 private AuthSession issue(CustomerEntity customer) {
  byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);
  String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  var e=new SessionEntity();e.tokenHash=hash(token);e.customer=customer;e.expiresAt=OffsetDateTime.now(ZoneOffset.UTC).plusHours(8);sessions.save(e);
  return new AuthSession().token(token).customer(shop.getCustomer(customer.id)).role(AuthSession.RoleEnum.fromValue(customer.role)).expiresAt(e.expiresAt);
 }
 @Transactional public AuthSession register(RegistrationInput input) {
  if(input.getPassword().getBytes(StandardCharsets.UTF_8).length>72)throw new IllegalArgumentException("Password exceeds 72 UTF-8 bytes");
  // Validation nghiệp vụ dùng chung với cập nhật hồ sơ; password chỉ được lưu dưới dạng BCrypt hash.
  var dto=shop.createCustomer(new CustomerInput().name(input.getName()).email(input.getEmail()).address(input.getAddress()).phone(input.getPhone()));
  var e=customers.findById(dto.getId()).orElseThrow();e.passwordHash=encoder.encode(input.getPassword());return issue(e);
 }
 @Transactional public AuthSession login(LoginInput input) {
  var e=customers.findByEmailIgnoreCase(input.getEmail().trim()).orElseThrow(()->new ResponseStatusException(UNAUTHORIZED,"Email or password is incorrect"));
  if(e.passwordHash==null||input.getPassword().getBytes(StandardCharsets.UTF_8).length>72||!encoder.matches(input.getPassword(),e.passwordHash))throw new ResponseStatusException(UNAUTHORIZED,"Email or password is incorrect");
  return issue(e);
 }
 @Transactional public void logout(HttpServletRequest request) {
  authenticate(request);sessions.deleteById(hash(request.getHeader("Authorization").substring(7)));
 }
}
