package vn.edu.ead.shop.service;
/** Lỗi nghiệp vụ có mã ổn định để frontend biết cách xử lý, không phải phân tích message. */
public class BusinessException extends RuntimeException {
 private final String code;
 public BusinessException(String message) { this("BUSINESS_RULE",message); }
 public BusinessException(String code,String message) { super(message);this.code=code; }
 public String getCode() { return code; }
}
