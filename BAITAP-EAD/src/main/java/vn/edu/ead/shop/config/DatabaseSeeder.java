package vn.edu.ead.shop.config;

import java.math.BigDecimal;
import java.util.*;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import vn.edu.ead.shop.domain.*;
import vn.edu.ead.shop.repository.*;

/** Seed có kiểm tra tồn tại: khởi động lại không nhân đôi catalog và không ghi đè mật khẩu người dùng. */
@Component
@ConditionalOnProperty(name="shop.seed.enabled",havingValue="true",matchIfMissing=true)
public class DatabaseSeeder implements CommandLineRunner {
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final EntityManager em;
    public DatabaseSeeder(CustomerRepository c,ProductRepository p,CategoryRepository cat,EntityManager em) {
        customers=c;products=p;categories=cat;this.em=em;
    }

    @Override @Transactional
    public void run(String... args) {
        // Giữ dữ liệu phiên bản cũ, chỉ đổi tên trạng thái tương đương với hợp đồng mới.
        em.createQuery("update OrderEntity o set o.status='PAID' where o.status='CONFIRMED'").executeUpdate();
        em.createQuery("update OrderEntity o set o.status='SHIPPED' where o.status='COMPLETED'").executeUpdate();
        Faker faker=new Faker(new Locale("en"),new Random(42));
        seedAccount("admin@shop.local","Admin123!","ADMIN","Quan tri vien","0901000001");
        seedAccount("demo@shop.local","Customer123!","CUSTOMER","Nguyen Van An","0901000002");
        if(categories.count()==0) {
            String[] names={"Bàn phím","Âm thanh","Chuột & bàn di","Phụ kiện"};
            String[] pictures={"keyboard","headphones","mouse","accessories"};
            String[][] titles={{"Nova Mechanical K75","Studio Keys TKL","AirType Wireless","Mono Keys Mini"},{"Wave Pro Headphones","Studio Buds Air","Soundbar Mini","Echo Wireless"},{"Orbit Mouse Pro","Glide Mouse Wireless","Desk Mat XL","Precision Mouse"},{"Dock USB-C 7 in 1","Laptop Stand Air","Webcam Focus HD","Desk Light Halo"}};
            for(int i=0;i<names.length;i++) {
                var category=new CategoryEntity();category.name=names[i];category.description=faker.commerce().department();categories.save(category);
                for(int j=0;j<4;j++) {
                    var p=new ProductEntity();p.name=titles[i][j];p.category=category;
                    p.description="Thiết kế tối giản cho góc làm việc. "+faker.lorem().sentence(10);
                    p.price=BigDecimal.valueOf(350000L+i*240000L+j*175000L);p.stock=15+j*5;
                    p.imageUrl="/images/"+pictures[i]+".svg";products.save(p);
                }
            }
            // Khách hàng giả lập dùng Faker để thử danh sách, profile và phân trang.
            for(int i=0;i<8;i++)seedAccount("sample"+i+"@shop.local","Customer123!","CUSTOMER",faker.name().fullName(),"090200000"+i);
        }
        // Bổ sung dữ liệu cũ còn thiếu trường mới; không thay đổi giá hay số lượng tồn kho.
        var first=categories.findAll().stream().findFirst();
        for(var p:products.findAll()) {if(p.category==null)first.ifPresent(c->p.category=c);if(p.imageUrl==null)p.imageUrl="/images/keyboard.svg";}
        for(var c:customers.findAll()) {
            if(c.phone==null)c.phone=String.format("090%07d",c.id);
            if(c.role==null||c.role.isBlank())c.role="CUSTOMER";
        }
    }

    private void seedAccount(String email,String password,String role,String name,String phone) {
        if(customers.findByEmailIgnoreCase(email).isPresent())return;
        var c=new CustomerEntity();c.email=email;c.name=name;c.address="123 Nguyễn Trãi, TP HCM";c.phone=phone;
        c.role=role;c.passwordHash=new BCryptPasswordEncoder().encode(password);customers.save(c);
    }
}
