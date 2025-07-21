package security.license;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu class yêu cầu license validation
 * 
 * @author Đoàn Ngọc Thành
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresLicense {
    
    /**
     * Tên feature/module cần license
     */
    String feature() default "default";
    
    /**
     * Level license yêu cầu
     */
    LicenseLevel level() default LicenseLevel.BASIC;
    
    /**
     * Thông báo lỗi custom nếu không có license
     */
    String message() default "This feature requires a valid license";
    
    /**
     * Có bắt buộc license hay chỉ warning
     */
    boolean strict() default true;
    
    public enum LicenseLevel {
        BASIC,    // License cơ bản
        PREMIUM,  // License cao cấp
        ENTERPRISE // License doanh nghiệp
    }
}
