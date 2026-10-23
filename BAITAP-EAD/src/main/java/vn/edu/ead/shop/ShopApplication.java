package vn.edu.ead.shop;

/** Điểm khởi động: Spring Boot quét các lớp trong package này, tạo bean và chạy máy chủ HTTP. */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Bật cấu hình tự động, quét component và đánh dấu ứng dụng Spring Boot.
@SpringBootApplication
public class ShopApplication {
    // JVM gọi main; SpringApplication.run tạo application context và khởi động Tomcat.
    public static void main(String[] args) { SpringApplication.run(ShopApplication.class, args); }
}
