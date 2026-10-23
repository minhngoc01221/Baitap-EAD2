package vn.edu.ead.shop.web;

/** Xử lý ngoại lệ tập trung: mọi controller dùng chung cấu trúc JSON lỗi code, message, details. */
import java.util.List;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.LoggerFactory;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.edu.ead.shop.generated.model.ApiError;
import vn.edu.ead.shop.service.BusinessException;
// Tự động bắt các ngoại lệ từ tất cả REST controller.
@RestControllerAdvice
public class ApiExceptionHandler {
 // Hàm tạo response lỗi dùng chung: đặt HTTP status và dựng DTO gồm code/message/details.
 private ResponseEntity<ApiError> error(int status,String code,String message,List<String> details) { return ResponseEntity.status(status).body(new ApiError().code(code).message(message).details(details)); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler(MethodArgumentNotValidException.class)
 // Lỗi @Valid trên request body: lấy từng tên field và thông báo để frontend biết cần sửa ở đâu; trả 400.
 ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) { return error(400,"VALIDATION_ERROR","Invalid request",e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).toList()); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler({IllegalArgumentException.class,ConstraintViolationException.class,HttpMessageNotReadableException.class,HandlerMethodValidationException.class,MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class})
 // JSON hỏng, sai kiểu, thiếu tham số hoặc vi phạm validation đều được đổi thành 400.
 ResponseEntity<ApiError> badRequest(Exception e) { return error(400,"VALIDATION_ERROR","Invalid request",List.of(e instanceof IllegalArgumentException?e.getMessage():"Check parameter types, required fields and JSON values")); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler(ResponseStatusException.class)
 // Giữ HTTP status của ResponseStatusException; service dùng loại này để báo tài nguyên không tồn tại.
 ResponseEntity<ApiError> missing(ResponseStatusException e) { return error(e.getStatusCode().value(),e.getStatusCode().value()==401?"UNAUTHORIZED":e.getStatusCode().value()==403?"FORBIDDEN":"NOT_FOUND",e.getReason(),List.of()); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler(NoResourceFoundException.class)
 // URL hoặc file tài nguyên không tồn tại trả JSON 404 thống nhất.
 ResponseEntity<ApiError> missingRoute(Exception e) { return error(404,"NOT_FOUND","Resource not found",List.of()); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler(BusinessException.class)
 // Lỗi quy tắc kinh doanh, ví dụ thiếu tồn hoặc email trùng, trả 422.
 ResponseEntity<ApiError> business(BusinessException e) { return error(422,e.getCode(),e.getMessage(),List.of()); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler({DataIntegrityViolationException.class,ConcurrencyFailureException.class})
 // Vi phạm ràng buộc database hoặc xung đột cập nhật: trả 422 và gợi ý tải lại.
 ResponseEntity<ApiError> conflict(Exception e) { return error(422,"BUSINESS_RULE","Data conflict; reload and retry",List.of()); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
 // URL tồn tại nhưng HTTP method không phù hợp: trả 405.
 ResponseEntity<ApiError> method(Exception e) { return error(405,"METHOD_NOT_ALLOWED","HTTP method is not supported",List.of()); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
 // Request gửi sai Content-Type: trả 415, yêu cầu application/json.
 ResponseEntity<ApiError> media(Exception e) { return error(415,"UNSUPPORTED_MEDIA_TYPE","Use application/json",List.of()); }
 // Chọn loại ngoại lệ mà phương thức bên dưới sẽ xử lý.
 @ExceptionHandler(Exception.class)
 // Lỗi chưa dự kiến: ghi chi tiết vào log máy chủ, chỉ gửi thông báo chung và HTTP 500 cho client.
 ResponseEntity<ApiError> unexpected(Exception e) { LoggerFactory.getLogger(getClass()).error("Unhandled request failure",e);return error(500,"INTERNAL_ERROR","Unexpected server error",List.of()); }
}
