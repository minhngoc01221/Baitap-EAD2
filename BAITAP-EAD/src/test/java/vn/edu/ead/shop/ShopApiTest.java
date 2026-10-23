package vn.edu.ead.shop;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

// Nạp ứng dụng thật với database H2; MockMvc gọi HTTP controller trong bộ nhớ, không cần mở cổng.
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("demo")
class ShopApiTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;
 private static int serial=0;
 @Autowired org.springframework.web.context.WebApplicationContext context;
 @Autowired vn.edu.ead.shop.service.ShopService service;
 long categoryId;
 @BeforeEach void adminSession() throws Exception {
  // Đăng nhập thật: các test controller đi qua interceptor phân quyền giống request ngoài trình duyệt.
  var result=mvc.perform(post("/api/v1/sessions").contentType("application/json").content("{\"email\":\"admin@shop.local\",\"password\":\"Admin123!\"}")).andExpect(status().isCreated()).andReturn();
  String token=mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
  mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context).defaultRequest(get("/").header("Authorization","Bearer "+token)).build();
  categoryId=create("/categories","{\"name\":\"Test category\"}").get("id").asLong();
 }
 // Hàm hỗ trợ POST: bắt buộc response 201 có Location và ID đúng trước khi trả JSON cho test.
 JsonNode create(String path,String body) throws Exception {
  var result=mvc.perform(post("/api/v1"+path).contentType("application/json").content(body)).andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();
  JsonNode node=mapper.readTree(result.getResponse().getContentAsString());
  long createdId=node.has("id")?node.get("id").asLong():node.get("customer").get("id").asLong();
  assertThat(result.getResponse().getHeader("Location")).endsWith("/"+createdId);return node;
 }
 long customer() throws Exception { return create("/customers","""
  {"name":"Nguyen Van An","email":"test%s@example.com","address":"123 Nguyen Trai","phone":"0901234567"}
  """.formatted(++serial)).get("id").asLong(); }
 long product(int stock) throws Exception { return create("/products","""
  {"name":"Ban phim","description":"USB","price":750000,"stock":%d,"categoryId":%d,"imageUrl":"/images/keyboard.svg"}
  """.formatted(stock,categoryId)).get("id").asLong(); }
 String order(long c,long p,int qty) {return "{\"customerId\":"+c+",\"items\":[{\"productId\":"+p+",\"quantity\":"+qty+"}]}";}
 // Kiểm tra tổng tiền, trừ tồn, ID quan hệ, cấm chuyển trạng thái sai, hủy lặp chỉ hoàn tồn một lần và bảo vệ lịch sử.
 @Test void orderTotalStockCancellationAndHistoricalGuards() throws Exception {
  long c=customer(),p=product(5);var o=create("/orders",order(c,p,2));long id=o.get("id").asLong();assertThat(o.get("total").decimalValue()).isEqualByComparingTo("1500000");
  mvc.perform(get("/api/v1/products/"+p)).andExpect(jsonPath("$.stock").value(3));
  mvc.perform(get("/api/v1/customers/"+c+"/orders")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].customerId").value(c)).andExpect(jsonPath("$.content[0].items[0].productId").value(p));
  mvc.perform(patch("/api/v1/orders/"+id).contentType("application/json").content("{\"status\":\"SHIPPED\"}")).andExpect(status().isUnprocessableEntity());
  for(int i=0;i<2;i++)mvc.perform(patch("/api/v1/orders/"+id).contentType("application/json").content("{\"status\":\"CANCELLED\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/v1/products/"+p)).andExpect(jsonPath("$.stock").value(5));
  mvc.perform(delete("/api/v1/customers/"+c)).andExpect(status().isUnprocessableEntity());
  mvc.perform(delete("/api/v1/products/"+p)).andExpect(status().isUnprocessableEntity());
 }
 // Dòng đầu còn hàng, dòng sau hết hàng: toàn bộ giao dịch phải rollback, không mất tồn và không lưu đơn.
 @Test void failedOrderRollsBackEarlierStockChanges() throws Exception {
  long c=customer(),a=product(5),b=product(0);
  mvc.perform(post("/api/v1/orders").contentType("application/json").content("{\"customerId\":"+c+",\"items\":[{\"productId\":"+a+",\"quantity\":2},{\"productId\":"+b+",\"quantity\":1}]}"))
   .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("ERR_STOCK_EMPTY"));
  mvc.perform(get("/api/v1/products/"+a)).andExpect(jsonPath("$.stock").value(5));
  mvc.perform(get("/api/v1/customers/"+c+"/orders")).andExpect(jsonPath("$.totalElements").value(0));
 }
 // Dữ liệu sai phải trả 400, ID không có phải trả 404; JSON lỗi luôn có cấu trúc thống nhất.
 @Test void validationAndErrorContract() throws Exception {
  mvc.perform(post("/api/v1/customers").contentType("application/json").content("{\"name\":\"A\",\"email\":\"bad\",\"address\":\"x\"}"))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR")).andExpect(jsonPath("$.details").isArray());
  mvc.perform(get("/api/v1/products/99999999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
  for(String query:new String[]{"page=-1","size=101","sort=invalid,desc","sort=price,wrong"})mvc.perform(get("/api/v1/products?"+query)).andExpect(status().isBadRequest());
  mvc.perform(post("/api/v1/orders").contentType("application/json").content("{\"customerId\":1,\"items\":[{\"productId\":1,\"quantity\":0}]}" )).andExpect(status().isBadRequest());
  mvc.perform(patch("/api/v1/orders/1").contentType("application/json").content("{\"status\":\"INVALID\"}")).andExpect(status().isBadRequest());
 }
 // Kiểm tra CRUD, email trùng không phân biệt hoa thường, giới hạn trang và DELETE không có body.
 @Test void crudPaginationAndDuplicateEmail() throws Exception {
  long c=customer(),p=product(7);
  mvc.perform(put("/api/v1/customers/"+c).contentType("application/json").content("{\"name\":\"Updated Name\",\"email\":\"update"+c+"@example.com\",\"address\":\"456 Nguyen Trai\",\"phone\":\"0901234567\"}")).andExpect(status().isOk());
  mvc.perform(post("/api/v1/customers").contentType("application/json").content("{\"name\":\"Duplicate\",\"email\":\"UPDATE"+c+"@example.com\",\"address\":\"456 Nguyen Trai\",\"phone\":\"0901234567\"}")).andExpect(status().isUnprocessableEntity());
  mvc.perform(put("/api/v1/products/"+p).contentType("application/json").content("{\"name\":\"New Product\",\"price\":99000,\"stock\":9,\"categoryId\":"+categoryId+",\"imageUrl\":\"/images/keyboard.svg\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(9));
  mvc.perform(get("/api/v1/products?page=0&size=1&sort=price,desc")).andExpect(status().isOk()).andExpect(jsonPath("$.size").value(1)).andExpect(jsonPath("$.content.length()").value(1));
  mvc.perform(delete("/api/v1/products/"+p)).andExpect(status().isNoContent()).andExpect(content().string(""));
  mvc.perform(delete("/api/v1/customers/"+c)).andExpect(status().isNoContent());
 }
 // Đảm bảo hợp đồng YAML truy cập được và đường dẫn Swagger điều hướng đúng.
 @Test void swaggerAndContractAreAvailable() throws Exception {
  mvc.perform(get("/shop-api.yaml")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("3.0.3")));
  mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
 }

 // Quyền truy cập được thực thi ở server: token khách không được sửa catalog hoặc đọc đơn của người khác.
 @Test void registrationAuthenticationOwnershipAndLogout() throws Exception {
  String email="new"+(++serial)+"@example.com";
  var auth=create("/registrations","{\"name\":\"New Customer\",\"email\":\""+email+"\",\"password\":\"Customer123!\",\"phone\":\"0901234567\",\"address\":\"123 Nguyen Trai\"}");
  String token=auth.get("token").asText();long id=auth.get("customer").get("id").asLong();
  assertThat(auth.toString()).doesNotContain("passwordHash");
  mvc.perform(get("/api/v1/profiles/current").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
  mvc.perform(get("/api/v1/orders").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
  long other=customer(),p=product(5);
  mvc.perform(post("/api/v1/orders").header("Authorization","Bearer "+token).contentType("application/json").content(order(other,p,1))).andExpect(status().isForbidden());
  var o=create("/orders",order(other,p,1));
  mvc.perform(get("/api/v1/orders/"+o.get("id").asLong()).header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
  mvc.perform(post("/api/v1/sessions").contentType("application/json").content("{\"email\":\""+email+"\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
  mvc.perform(delete("/api/v1/sessions").header("Authorization","Bearer "+token)).andExpect(status().isNoContent());
  mvc.perform(get("/api/v1/profiles/current").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/v1/registrations").contentType("application/json").content("{\"name\":\"Valid Name\",\"email\":\"phone@example.com\",\"password\":\"Customer123!\",\"phone\":\"abc\",\"address\":\"123 Street\"}")).andExpect(status().isBadRequest());
 }
 @Test void paidShippedTransitionsAndStockPatch() throws Exception {
  long c=customer(),p=product(5);long id=create("/orders",order(c,p,2)).get("id").asLong();
  for(String next:new String[]{"PAID","SHIPPED"})mvc.perform(patch("/api/v1/orders/"+id).contentType("application/json").content("{\"status\":\""+next+"\"}")).andExpect(status().isOk());
  mvc.perform(patch("/api/v1/orders/"+id).contentType("application/json").content("{\"status\":\"CANCELLED\"}")).andExpect(status().isUnprocessableEntity());
  mvc.perform(patch("/api/v1/products/"+p+"/stock").contentType("application/json").content("{\"stock\":12}")).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(12));
  mvc.perform(get("/api/v1/products?categoryId="+categoryId+"&q=Ban&minPrice=700000&maxPrice=800000")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
  mvc.perform(get("/api/v1/products?minPrice=800000&maxPrice=100")).andExpect(status().isBadRequest());
 }
 @Test void concurrentCheckoutNeverOversells() throws Exception {
  long c=customer(),p=product(2);
  var input=new vn.edu.ead.shop.generated.model.OrderInput().customerId(c).items(java.util.List.of(new vn.edu.ead.shop.generated.model.OrderLineInput().productId(p).quantity(2)));
  try(var pool=java.util.concurrent.Executors.newFixedThreadPool(2)) {
   java.util.concurrent.Callable<Boolean> task=()->{try{service.createOrder(input);return true;}catch(vn.edu.ead.shop.service.BusinessException e){return false;}};
   var results=pool.invokeAll(java.util.List.of(task,task));int success=0;for(var r:results)if(r.get())success++;
   assertThat(success).isEqualTo(1);
  }
  mvc.perform(get("/api/v1/products/"+p)).andExpect(jsonPath("$.stock").value(0));
 }
}
