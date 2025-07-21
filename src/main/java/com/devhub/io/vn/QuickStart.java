package com.devhub.io.vn;

import security.license.LicenseValidator.LicenseValidationException;

/**
 * Quick Start Guide - Hướng dẫn sử dụng nhanh DevHub SDK
 * 
 * @author Đoàn Ngọc Thành
 * @version 1.0.0
 */
public class QuickStart {
    
    public static void main(String[] args) {
        System.out.println("========== DevHub SDK - Quick Start ==========\n");
        
        // Cách sử dụng đơn giản nhất
        simpleUsage();
        
        //System.out.println("\n" + "=".repeat(47));
        
        // Cách sử dụng nâng cao
     //   advancedUsage();
    }
    
    /**
     * Cách sử dụng đơn giản - chỉ cần 3 bước
     */
    public static void simpleUsage() {
        System.out.println("🚀 SIMPLE USAGE - Chỉ cần 3 bước:");
        System.out.println("1️⃣ Tạo instance DevhubSDK với license key");
        System.out.println("2️⃣ Sử dụng các method có sẵn");
        System.out.println("3️⃣ Đóng SDK khi xong\n");
        
        DevhubSDK sdk = null;
        try {
            // Bước 1: Khởi tạo với license
            sdk = new DevhubSDK("B7GZ-YD59-QMYM-SMSW");
            
            // Bước 2: Sử dụng
            System.out.println("🔐 Mã hóa text:");
            String encrypted = sdk.encrypt("Hello World!");
            System.out.println("   Kết quả: " + encrypted);
            
            System.out.println("🔓 Giải mã text:");
            String decrypted = sdk.decrypt(encrypted);
            System.out.println("   Kết quả: " + decrypted);
            
            System.out.println("📊 Thông tin SDK:");
            System.out.println("   Version: " + sdk.getVersion());
            System.out.println("   Ready: " + sdk.isReady());
            
        } catch (LicenseValidationException e) {
            System.err.println("❌ License không hợp lệ: " + e.getMessage());
        } finally {
            // Bước 3: Đóng SDK
            if (sdk != null) {
                sdk.close();
            }
        }
    }
    
    /**
     * Cách sử dụng nâng cao với nhiều features
     */
    public static void advancedUsage() {
        System.out.println("🔥 ADVANCED USAGE - Sử dụng đầy đủ features:");
        
        DevhubSDK sdk = null;
        try {
            // Khởi tạo SDK
            sdk = new DevhubSDK("B7GZ-YD59-QMYM-SMSW");
            
            // 1. Computer Vision
            System.out.println("\n🎯 COMPUTER VISION:");
            try {
                sdk.loadComputerVisionModel("yolov8n.pt");
                YoloV8.DetectionResult[] objects = sdk.detectObjects("image.jpg", 0.5f);
                System.out.println("   Detected " + objects.length + " objects");
            } catch (Exception e) {
                System.out.println("   ℹ️ Computer Vision demo (simulation)");
            }
            
            // 2. Crypto Utils
            System.out.println("\n🔐 CRYPTO UTILITIES:");
            String sensitiveData = "Thông tin bảo mật";
            String encryptedData = sdk.encrypt(sensitiveData);
            String decryptedData = sdk.decrypt(encryptedData);
            System.out.println("   Original: " + sensitiveData);
            System.out.println("   Encrypted: " + encryptedData);
            System.out.println("   Decrypted: " + decryptedData);
            
            // 3. Vietnam Services
            System.out.println("\n🇻🇳 VIETNAM SERVICES:");
            System.out.println("   Banking Service: " + 
                (sdk.getBankingService() != null ? "✅ Available" : "❌ Not available"));
            System.out.println("   ID Card Service: " + 
                (sdk.getIdCardService() != null ? "✅ Available" : "❌ Not available"));
            System.out.println("   Address Service: " + 
                (sdk.getAddressService() != null ? "✅ Available" : "❌ Not available"));
            
            // 4. License Management
            System.out.println("\n📋 LICENSE MANAGEMENT:");
            System.out.println("   " + sdk.getLicenseInfo());
            
            // 5. SDK Information
            System.out.println("\n📦 SDK INFORMATION:");
            sdk.printSDKInfo();
            
        } catch (LicenseValidationException e) {
            System.err.println("❌ License validation failed: " + e.getMessage());
            System.err.println("💡 Liên hệ support@devhub.io.vn để được hỗ trợ");
        } finally {
            if (sdk != null) {
                sdk.close();
            }
        }
    }
    
    /**
     * Demo pattern sử dụng với try-with-resources (nếu implement AutoCloseable)
     */
    public static void tryWithResourcesPattern() {
        System.out.println("🔄 TRY-WITH-RESOURCES PATTERN:");
        
        // Hiện tại chưa implement AutoCloseable, nhưng có thể thêm sau
        try {
            DevhubSDK sdk = new DevhubSDK("your-license-key");
            
            // Sử dụng SDK
            String result = sdk.encrypt("test data");
            System.out.println("Result: " + result);
            
            // Tự động close khi ra khỏi try block
            sdk.close();
            
        } catch (LicenseValidationException e) {
            System.err.println("License error: " + e.getMessage());
        }
    }
    
    /**
     * Demo error handling patterns
     */
    public static void errorHandlingDemo() {
        System.out.println("⚠️ ERROR HANDLING PATTERNS:");
        
        try {
            // Test với license key không hợp lệ
            DevhubSDK sdk = new DevhubSDK("invalid-license-key");
            
        } catch (LicenseValidationException e) {
            System.err.println("Xử lý lỗi license:");
            System.err.println("- Kiểm tra license key");
            System.err.println("- Liên hệ support@devhub.io.vn");
            System.err.println("- Kiểm tra kết nối internet");
        }
    }
}
