package vn.edu.ead.shop.web;
import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ead.shop.generated.api.AccountsApi;
import vn.edu.ead.shop.generated.model.*;
import vn.edu.ead.shop.service.*;
/** Endpoint tài khoản dùng DTO/interface sinh từ OpenAPI. */
@RestController @RequestMapping("/api/v1")
public class AccountsController implements AccountsApi {
 private final AuthService auth;private final ShopService shop;private final HttpServletRequest request;
 public AccountsController(AuthService a,ShopService s,HttpServletRequest r) {auth=a;shop=s;request=r;}
 @Override public ResponseEntity<AuthSession> register(RegistrationInput input) { var result=auth.register(input);return ResponseEntity.created(URI.create("/api/v1/customers/"+result.getCustomer().getId())).body(result); }
 @Override public ResponseEntity<AuthSession> login(LoginInput input) {return ResponseEntity.created(URI.create("/api/v1/sessions")).body(auth.login(input));}
 @Override public ResponseEntity<Void> logout() {auth.logout(request);return ResponseEntity.noContent().build();}
 @Override public ResponseEntity<Customer> getProfile() {return ResponseEntity.ok(shop.getCustomer(auth.authenticate(request).id));}
 @Override public ResponseEntity<Customer> updateProfile(CustomerInput input) {return ResponseEntity.ok(shop.updateCustomer(auth.authenticate(request).id,input));}
}
