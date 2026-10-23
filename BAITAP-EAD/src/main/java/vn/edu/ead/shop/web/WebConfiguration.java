package vn.edu.ead.shop.web;

/** Cấu hình tài nguyên YAML cho Swagger và CORS để prototype gọi backend từ cổng khác. */
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
// Đăng ký cấu hình web với Spring.
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
 private final AccessInterceptor access;
 public WebConfiguration(AccessInterceptor access) {this.access=access;}
 @Override public void addInterceptors(InterceptorRegistry registry) {registry.addInterceptor(access).addPathPatterns("/api/v1/**");}
 // Phục vụ /shop-api.yaml từ resources để Swagger UI đọc đúng hợp đồng thiết kế.
 @Override public void addResourceHandlers(ResourceHandlerRegistry registry) { registry.addResourceHandler("/shop-api.yaml").addResourceLocations("classpath:/"); }
 // Cho phép các origin prototype đã liệt kê gọi API; expose Location để JavaScript đọc URL tài nguyên mới.
 @Override public void addCorsMappings(CorsRegistry registry) { registry.addMapping("/api/v1/**").allowedOrigins("http://localhost:8080","http://localhost:5500","http://127.0.0.1:5500").allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS").exposedHeaders("Location"); }
}
