package vn.edu.ead.shop.web;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import vn.edu.ead.shop.service.AuthService;
import vn.edu.ead.shop.repository.OrderRepository;

/** Phân quyền ở server; ẩn nút trên giao diện không đủ để bảo vệ API. Bearer không dùng cookie nên không có cookie tự gửi chéo nguồn. */
@Component
public class AccessInterceptor implements HandlerInterceptor {
 private final AuthService auth;private final OrderRepository orders;
 public AccessInterceptor(AuthService a,OrderRepository o){auth=a;orders=o;}
 @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) {
  String path=req.getRequestURI().substring("/api/v1".length()),method=req.getMethod();
  if(method.equals("OPTIONS")||(method.equals("POST")&&(path.equals("/sessions")||path.equals("/registrations")))||
    (method.equals("GET")&&(path.matches("/products(?:/[^/]+)?")||path.matches("/categories(?:/[^/]+)?"))))return true;
  var actor=auth.authenticate(req);boolean admin="ADMIN".equals(actor.role);req.setAttribute("actor",actor);
  if(path.startsWith("/products")||path.startsWith("/categories")||path.equals("/customers")||(path.equals("/orders")&&method.equals("GET"))||(path.startsWith("/customers/")&&method.equals("DELETE"))) {
   if(!admin)throw new ResponseStatusException(FORBIDDEN,"Administrator access required");
  }
  if(path.matches("/customers/[0-9]+(?:/orders)?"))auth.requireOwner(actor,Long.valueOf(path.split("/")[2]));
  if(path.matches("/orders/[0-9]+")) {
   var order=orders.findById(Long.valueOf(path.split("/")[2])).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Order not found"));
   auth.requireOwner(actor,order.customer.getId());
  }
  return true;
 }
}
