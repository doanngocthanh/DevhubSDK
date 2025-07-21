package security.license;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * License Interceptor - Tự động intercept các method calls và validate license
 * 
 * Features:
 * - Automatic license checking before method execution
 * - Dynamic proxy support
 * - Method-level license enforcement
 * 
 * @author Đoàn Ngọc Thành
 * @version 1.0.0
 */
public class LicenseInterceptor implements InvocationHandler {
    
    private final Object target;
    private final LicenseManager licenseManager;
    
    public LicenseInterceptor(Object target) {
        this.target = target;
        this.licenseManager = LicenseManager.getInstance();
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> targetClass = target.getClass();
        
        // Kiểm tra xem class có yêu cầu license không
        if (licenseManager.requiresLicense(targetClass)) {
            System.out.println("🔐 Checking license for " + targetClass.getSimpleName() + "." + method.getName() + "()");
            
            // Validate license trước khi execute method
            licenseManager.validateLicenseForClass(targetClass);
        }
        
        // Execute method gốc
        return method.invoke(target, args);
    }
    
    /**
     * Tạo proxy wrapper cho object cần protect
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target, Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            new Class<?>[]{interfaceClass},
            new LicenseInterceptor(target)
        );
    }
    
    /**
     * Tạo proxy wrapper cho object với multiple interfaces
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target, Class<?>... interfaces) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            interfaces,
            new LicenseInterceptor(target)
        );
    }
}
