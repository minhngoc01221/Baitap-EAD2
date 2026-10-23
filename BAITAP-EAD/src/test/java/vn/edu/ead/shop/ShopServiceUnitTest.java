package vn.edu.ead.shop;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import vn.edu.ead.shop.domain.*;
import vn.edu.ead.shop.repository.*;
import vn.edu.ead.shop.service.*;
import vn.edu.ead.shop.generated.model.*;

/** Unit test chỉ kiểm tra nghiệp vụ; repository giả lập, không khởi động Spring/database. */
@ExtendWith(MockitoExtension.class)
class ShopServiceUnitTest {
    @Mock CustomerRepository customers;
    @Mock ProductRepository products;
    @Mock OrderRepository orders;
    @Mock CategoryRepository categories;
    ShopService service;
    CustomerEntity customer;
    ProductEntity product;
    @BeforeEach void setup() {
        service=new ShopService(customers,products,orders,categories);
        customer=new CustomerEntity();customer.id=1L;
        product=new ProductEntity();product.id=2L;product.name="Keyboard";product.price=new BigDecimal("750000");product.stock=3;
    }
    OrderInput input(int quantity) {return new OrderInput().customerId(1L).items(List.of(new OrderLineInput().productId(2L).quantity(quantity)));}
    @Test void checkoutCalculatesSnapshotAndStock() {
        when(customers.findById(1L)).thenReturn(Optional.of(customer));
        when(products.findLockedById(2L)).thenReturn(Optional.of(product));
        when(orders.save(any())).thenAnswer(i->{OrderEntity o=i.getArgument(0);o.id=5L;return o;});
        var result=service.createOrder(input(2));
        assertThat(result.getTotal()).isEqualByComparingTo("1500000");assertThat(product.stock).isEqualTo(1);
        assertThat(result.getStatus().getValue()).isEqualTo("PENDING");assertThat(result.getItems().getFirst().getUnitPrice()).isEqualByComparingTo("750000");
    }
    @Test void insufficientStockDoesNotSaveOrder() {
        when(customers.findById(1L)).thenReturn(Optional.of(customer));
        when(products.findLockedById(2L)).thenReturn(Optional.of(product));
        assertThatThrownBy(()->service.createOrder(input(4))).isInstanceOf(BusinessException.class).hasMessageContaining("2");
        assertThat(product.stock).isEqualTo(3);verify(orders,never()).save(any());
    }
    @Test void terminalOrderCannotBeCancelled() {
        var order=new OrderEntity();order.id=1L;order.status="SHIPPED";
        when(orders.findLockedById(1L)).thenReturn(Optional.of(order));
        assertThatThrownBy(()->service.updateOrderStatus(1L,new OrderStatusInput().status(OrderStatusInput.StatusEnum.CANCELLED))).isInstanceOf(BusinessException.class);
        verifyNoInteractions(products);
    }
    @Test void invalidPriceRangeIsRejectedBeforeQuery() {
        assertThatThrownBy(()->service.listProducts(0,10,"id,asc",null,null,new BigDecimal("200"),new BigDecimal("100"))).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(products);
    }
    @Test void generatedDtosAreImmutableRecords() {
        assertThat(Product.class.isRecord()).isTrue();
        var original=new Product().name("Original");
        var changed=original.name("Changed");
        assertThat(original.getName()).isEqualTo("Original");assertThat(changed.getName()).isEqualTo("Changed");
        var page=new ProductPage().content(new java.util.ArrayList<>(List.of(original)));
        assertThatThrownBy(()->page.getContent().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
