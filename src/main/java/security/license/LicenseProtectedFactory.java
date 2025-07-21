package security.license;

import java.lang.reflect.Method;

/**
 * License Protected Factory - Factory pattern để tạo các instance được protect bởi license
 * 
 * Features:
 * - Automatic license validation on object creation
 * - Factory pattern cho protected instances
 * - Reflection-based instantiation
 * 
 * @author Đoàn Ngọc Thành
 * @version 1.0.0
 */
public class LicenseProtectedFactory {
    
    private static final LicenseManager licenseManager = LicenseManager.getInstance();
    
    /**
     * Tạo instance của class được protect bởi license
     * 
     * @param clazz Class cần tạo instance
     * @param <T> Type của class
     * @return Instance được protect
     * @throws RuntimeException nếu license không hợp lệ hoặc class không thể tạo
     */
    public static <T> T createInstance(Class<T> clazz) {
        // Validate license trước khi tạo instance
        licenseManager.validateLicenseForClass(clazz);
        
        try {
            System.out.println("🏭 Creating protected instance of " + clazz.getSimpleName());
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("\nFailed to create instance of " + clazz.getName(), e);
        }
    }
    
    /**
     * Tạo instance với constructor parameters
     */
    public static <T> T createInstance(Class<T> clazz, Class<?>[] paramTypes, Object... args) {
        // Validate license trước khi tạo instance
        licenseManager.validateLicenseForClass(clazz);
        
        try {
            System.out.println("🏭 Creating protected instance of " + clazz.getSimpleName() + " with parameters");
            return clazz.getDeclaredConstructor(paramTypes).newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }
    
    /**
     * Tạo singleton instance được protect
     */
    public static <T> T createSingleton(Class<T> clazz) {
        // Validate license trước khi tạo instance
        licenseManager.validateLicenseForClass(clazz);
        
        try {
            System.out.println("🏭 Creating protected singleton of " + clazz.getSimpleName());
            
            // Tìm getInstance method
            try {
                Method getInstanceMethod = clazz.getMethod("getInstance");
                return clazz.cast(getInstanceMethod.invoke(null));
            } catch (NoSuchMethodException e) {
                // Fallback to constructor
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create singleton of " + clazz.getName(), e);
        }
    }
}
