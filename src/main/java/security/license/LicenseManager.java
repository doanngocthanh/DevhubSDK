package security.license;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import security.license.LicenseValidator.HttpClientType;
import security.license.LicenseValidator.LicenseValidationException;
import security.license.LicenseValidator.ValidationResult;

/**
 * License Manager - Quản lý và validate license cho toàn bộ dự án
 * 
 * Features:
 * - Tự động scan và detect classes có @RequiresLicense
 * - Global license validation
 * - Cache validation results
 * - Interceptor cho method calls
 * 
 * @author Đoàn Ngọc Thành
 * @version 1.0.0
 */
public class LicenseManager {
    
    private static LicenseManager instance = null;
    private static final Object lock = new Object();
    
    private final LicenseValidator validator;
    private boolean isGloballyValidated = false;
    private String validatedLicenseKey = null;
    private ValidationResult validationResult = null;
    
    // Cache các class đã được scan
    private final Set<Class<?>> scannedClasses = new HashSet<>();
    private final Map<Class<?>, RequiresLicense> licenseRequirements = new ConcurrentHashMap<>();
    
    // Private constructor
    private LicenseManager() {
        this.validator = new LicenseValidator(HttpClientType.OK_HTTP);
        
        // Tự động scan khi khởi tạo
        performAutoScan();
    }
    
    /**
     * Singleton instance
     */
    public static LicenseManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new LicenseManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Validate license cho toàn bộ dự án
     * 
     * @param licenseKey License key để validate
     * @return true nếu license hợp lệ
     * @throws LicenseValidationException nếu license không hợp lệ
     */
    public boolean validateGlobalLicense(String licenseKey) throws LicenseValidationException {
        System.out.println("🔐 Validating global license for DevHub SDK...");
        
        try {
            ValidationResult result = validator.validateLicense(licenseKey);
            
            if (result.isSuccess()) {
                this.isGloballyValidated = true;
                this.validatedLicenseKey = licenseKey;
                this.validationResult = result;
                
                System.out.println("✅ Global license validated successfully!");
                System.out.println("📧 Licensed to: " + result.getEmail());
                System.out.println("⏰ Expires: " + (result.getExpires() != null ? result.getExpires() : "Never"));
                System.out.println("🎯 Protected classes: " + licenseRequirements.size());
                
                return true;
            } else {
                throw new LicenseValidationException("License validation failed: " + result.getError());
            }
            
        } catch (Exception e) {
            throw new LicenseValidationException("Failed to validate license: " + e.getMessage(), e);
        }
    }
    
    /**
     * Kiểm tra xem class có yêu cầu license không
     */
    public boolean requiresLicense(Class<?> clazz) {
        return licenseRequirements.containsKey(clazz);
    }
    
    /**
     * Validate license cho một class cụ thể
     */
    public void validateLicenseForClass(Class<?> clazz) {
        RequiresLicense annotation = licenseRequirements.get(clazz);
        if (annotation == null) {
            return; // Class không yêu cầu license
        }
        
        if (!isGloballyValidated) {
            String errorMessage = annotation.message().isEmpty() ? 
                "Class " + clazz.getSimpleName() + " requires a valid license" : 
                annotation.message();
            
            if (annotation.strict()) {
                throw new RuntimeException("❌ " + errorMessage + ". Please call LicenseManager.getInstance().validateGlobalLicense(key) first.");
            } else {
                System.err.println("⚠️ Warning: " + errorMessage);
            }
        }
    }
    
    /**
     * Tự động scan tất cả classes trong project để tìm @RequiresLicense
     */
    private void performAutoScan() {
        System.out.println("🔍 Scanning project for @RequiresLicense annotations...");
        
        try {
            scanPackage("com.devhub.io.vn");
            System.out.println("📊 Found " + licenseRequirements.size() + " classes requiring license validation"); 
            // In ra danh sách classes được protect
            if (!licenseRequirements.isEmpty()) {
                System.out.println("🛡️ Protected classes:");
                licenseRequirements.forEach((clazz, annotation) -> {
                    System.out.println("   - " + clazz.getSimpleName() + " (" + annotation.feature() + ", " + annotation.level() + ")");
                });
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Error during auto-scan: " + e.getMessage());
        }
    }
    
    /**
     * Scan một package để tìm @RequiresLicense annotations
     */
    private void scanPackage(String packageName) {
        try {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                
                if (resource.getProtocol().equals("file")) {
                    // Scan file system
                    scanDirectory(new File(resource.getFile()), packageName);
                } else if (resource.getProtocol().equals("jar")) {
                    // Scan JAR file
                    scanJarFile(resource, packagePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error scanning package " + packageName + ": " + e.getMessage());
        }
    }
    
    /**
     * Scan directory cho classes
     */
    private void scanDirectory(File directory, String packageName) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                processClass(className);
            }
        }
    }
    
    /**
     * Scan JAR file cho classes
     */
    private void scanJarFile(URL resource, String packagePath) {
        try {
            String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    processClass(className);
                }
            }
            
            jarFile.close();
        } catch (Exception e) {
            System.err.println("Error scanning JAR: " + e.getMessage());
        }
    }
    
    /**
     * Xử lý một class để kiểm tra @RequiresLicense annotation
     */
    private void processClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            
            if (scannedClasses.contains(clazz)) {
                return; // Đã scan rồi
            }
            
            scannedClasses.add(clazz);
            
            RequiresLicense annotation = clazz.getAnnotation(RequiresLicense.class);
            if (annotation != null) {
                licenseRequirements.put(clazz, annotation);
                System.out.println("🎯 Found @RequiresLicense: " + clazz.getSimpleName());
            }
            
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Bỏ qua classes không load được
        } catch (Exception e) {
            System.err.println("Error processing class " + className + ": " + e.getMessage());
        }
    }
    
    /**
     * Lấy thông tin license hiện tại
     */
    public LicenseInfo getCurrentLicenseInfo() {
        if (!isGloballyValidated || validationResult == null) {
            return new LicenseInfo(null, null, false, 0);
        }
        
        return new LicenseInfo(
            validationResult.getEmail(),
            validationResult.getExpires(),
            isGloballyValidated,
            licenseRequirements.size()
        );
    }
    
    /**
     * Reset license validation
     */
    public void resetLicense() {
        isGloballyValidated = false;
        validatedLicenseKey = null;
        validationResult = null;
        validator.clearCache();
        
        System.out.println("🔄 Global license reset");
    }
    
    /**
     * Đóng license manager
     */
    public void close() {
        try {
            validator.close();
            System.out.println("🔒 License Manager closed");
        } catch (Exception e) {
            System.err.println("Error closing License Manager: " + e.getMessage());
        }
    }
    
    /**
     * Thông tin license
     */
    public static class LicenseInfo {
        private final String email;
        private final String expires;
        private final boolean isValid;
        private final int protectedClasses;
        
        public LicenseInfo(String email, String expires, boolean isValid, int protectedClasses) {
            this.email = email;
            this.expires = expires;
            this.isValid = isValid;
            this.protectedClasses = protectedClasses;
        }
        
        public String getEmail() { return email; }
        public String getExpires() { return expires; }
        public boolean isValid() { return isValid; }
        public int getProtectedClasses() { return protectedClasses; }
        
        @Override
        public String toString() {
            return String.format("LicenseInfo{email='%s', expires='%s', valid=%s, protectedClasses=%d}", 
                                email, expires != null ? expires : "Never", isValid, protectedClasses);
        }
    }
}
