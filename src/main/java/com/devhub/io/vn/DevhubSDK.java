package com.devhub.io.vn;

import security.license.LicenseManager;
import security.license.LicenseValidator.LicenseValidationException;
import com.devhub.io.vn.service.*;

/**
 * DevhubSDK - Main API Class
 * 
 * Tổng hợp tất cả các API của DevHub SDK
 * 
 * Features:
 * - License Management
 * - Computer Vision (YoloV8)
 * - Crypto Utils
 * - Vietnam Services (Banking, ID Card, Address)
 * - Configuration Management
 * 
 * @author Đoàn Ngọc Thành
 * @version 1.0.0
 */
public class DevhubSDK {
	private static LicenseManager licenseManager;
	public static String scriptID="AKfycbwr5ZUcUkg261nFrALDNJlVACVIgzl3aywJS_mF_JzHa1AKWFWWtjYub9KRXv7uxnNJSw0";
	
	// SDK Components
	private YoloV8 yoloV8Instance;
	private VietNamBankingService bankingService;
	private VietNamCitizenIdentityCardService idCardService;
	private VietNamAddressService addressService;
	
	static {
		licenseManager = LicenseManager.getInstance();
		System.out.println("================================");
		System.out.println("🏠 HomePage: https://script.google.com/macros/s/"+scriptID+"/exec");
		System.out.println("💳 BuyLicense: https://script.google.com/macros/s/"+scriptID+"/exec");
		System.out.println("📱 Github Google App Script: ");
		System.out.println("🔗 Github Java SDK: https://github.com/doanngocthanh/DevhubSDK");
		System.out.println("📧 Support: support@devhub.io.vn");
		System.out.println("================================");
	}

	/**
	 * Constructor - Khởi tạo DevHub SDK với license key
	 * 
	 * @param licenseKey License key để validate
	 * @throws LicenseValidationException nếu license không hợp lệ
	 */
	public DevhubSDK(String licenseKey) throws LicenseValidationException {
		System.out.println("� Initializing DevHub SDK...");
		System.out.println("�📋 Current License Status:");
		System.out.println(licenseManager.getCurrentLicenseInfo());
		
		try {
			boolean isValid = licenseManager.validateGlobalLicense(licenseKey);
			if (isValid) {
				System.out.println("✅ License validation successful!");
				System.out.println("📋 Updated License Status:");
				System.out.println(licenseManager.getCurrentLicenseInfo());
				
				// Khởi tạo các services
				initializeServices();
			}
		} catch (LicenseValidationException e) {
			System.err.println("❌ License validation failed: " + e.getMessage());
			System.err.println("💡 Tip: Liên hệ support@devhub.io.vn để được cấp license");
			throw e;
		}
	}
	
	/**
	 * Khởi tạo các services sau khi validate license thành công
	 */
	private void initializeServices() {
		System.out.println("🔧 Initializing SDK services...");
		this.bankingService = new VietNamBankingService();
		this.idCardService = new VietNamCitizenIdentityCardService();
		this.addressService = new VietNamAddressService();
		System.out.println("✅ All services initialized successfully!");
	}
	
	// ===================== LICENSE MANAGEMENT =====================
	
	/**
	 * Lấy thông tin license hiện tại
	 */
	public LicenseManager.LicenseInfo getLicenseInfo() {
		return licenseManager.getCurrentLicenseInfo();
	}
	
	/**
	 * Reset license validation
	 */
	public void resetLicense() {
		licenseManager.resetLicense();
	}
	
	// ===================== COMPUTER VISION =====================
	
	/**
	 * Lấy YoloV8 Computer Vision instance
	 * 
	 * @return YoloV8 instance được protect bởi license
	 */
	public YoloV8 getComputerVision() {
		if (yoloV8Instance == null) {
			yoloV8Instance = YoloV8.getInstance();
		}
		return yoloV8Instance;
	}
	
	/**
	 * Detect objects trong image
	 * 
	 * @param imagePath đường dẫn tới image
	 * @return mảng kết quả detection
	 */
	public YoloV8.DetectionResult[] detectObjects(String imagePath) {
		return getComputerVision().detectObjects(imagePath);
	}
	
	/**
	 * Detect objects với confidence threshold
	 * 
	 * @param imagePath đường dẫn tới image
	 * @param confidenceThreshold ngưỡng confidence (0.0 - 1.0)
	 * @return mảng kết quả detection đã filter
	 */
	public YoloV8.DetectionResult[] detectObjects(String imagePath, float confidenceThreshold) {
		return getComputerVision().detectObjects(imagePath, confidenceThreshold);
	}
	
	/**
	 * Load YoloV8 model
	 * 
	 * @param modelPath đường dẫn tới model file
	 * @return true nếu load thành công
	 */
	public boolean loadComputerVisionModel(String modelPath) {
		return getComputerVision().loadModel(modelPath);
	}
	
	// ===================== CRYPTO UTILITIES =====================
	
	/**
	 * Mã hóa chuỗi văn bản
	 * 
	 * @param plainText văn bản cần mã hóa
	 * @return chuỗi đã mã hóa (Base64)
	 */
	public String encrypt(String plainText) {
		return CryptoUtils.encrypt(plainText);
	}
	
	/**
	 * Giải mã chuỗi văn bản
	 * 
	 * @param encryptedText chuỗi đã mã hóa
	 * @return văn bản gốc
	 */
	public String decrypt(String encryptedText) {
		return CryptoUtils.decrypt(encryptedText);
	}
	
	// ===================== VIETNAM SERVICES =====================
	
	/**
	 * Lấy Vietnam Banking Service
	 * 
	 * @return VietNamBankingService instance
	 */
	public VietNamBankingService getBankingService() {
		return bankingService;
	}
	
	/**
	 * Lấy Vietnam Citizen ID Card Service
	 * 
	 * @return VietNamCitizenIdentityCardService instance
	 */
	public VietNamCitizenIdentityCardService getIdCardService() {
		return idCardService;
	}
	
	/**
	 * Lấy Vietnam Address Service
	 * 
	 * @return VietNamAddressService instance
	 */
	public VietNamAddressService getAddressService() {
		return addressService;
	}
	
	// ===================== UTILITY METHODS =====================
	
	/**
	 * Kiểm tra trạng thái SDK
	 * 
	 * @return true nếu SDK đã được khởi tạo và license hợp lệ
	 */
	public boolean isReady() {
		return licenseManager.getCurrentLicenseInfo().isValid();
	}
	
	/**
	 * Lấy thông tin version của SDK
	 * 
	 * @return thông tin version
	 */
	public String getVersion() {
		return "1.0.0";
	}
	
	/**
	 * Lấy thông tin tác giả
	 * 
	 * @return tên tác giả
	 */
	public String getAuthor() {
		return "Đoàn Ngọc Thành";
	}
	
	/**
	 * In thông tin SDK
	 */
	public void printSDKInfo() {
		System.out.println("=================== DevHub SDK Info ===================");
		System.out.println("📦 Version: " + getVersion());
		System.out.println("👨‍💻 Author: " + getAuthor());
		System.out.println("📧 Support: support@devhub.io.vn");
		System.out.println("🔗 GitHub: https://github.com/doanngocthanh/DevhubSDK");
		System.out.println("📋 License Status: " + (isReady() ? "✅ Valid" : "❌ Invalid"));
		System.out.println("🛡️ Protected Classes: " + getLicenseInfo().getProtectedClasses());
		System.out.println("======================================================");
	}
	
	/**
	 * Đóng SDK và giải phóng resources
	 */
	public void close() {
		System.out.println("🔒 Closing DevHub SDK...");
		
		if (yoloV8Instance != null) {
			yoloV8Instance.close();
		}
		
		licenseManager.close();
		System.out.println("✅ DevHub SDK closed successfully");
	}

	/**
	 * Main method - Demo sử dụng DevHub SDK
	 */
	public static void main(String[] args) {
		DevhubSDK sdk = null;
		
		try {
			// 1. Khởi tạo SDK với license key
			System.out.println("🚀 Starting DevHub SDK Demo...\n");
			sdk = new DevhubSDK("B7GZ-YD59-QMYM-SMSW");
			
			// 2. In thông tin SDK
			sdk.printSDKInfo();
			System.out.println();
			
			// 3. Test Crypto Utils
			System.out.println("🔐 Testing Crypto Utils:");
			String originalText = "Hello DevHub SDK!";
			String encrypted = sdk.encrypt(originalText);
			String decrypted = sdk.decrypt(encrypted);
			System.out.println("   Original: " + originalText);
			System.out.println("   Encrypted: " + encrypted);
			System.out.println("   Decrypted: " + decrypted);
			System.out.println();
			
			// 4. Test Computer Vision
			System.out.println("🎯 Testing Computer Vision:");
			boolean modelLoaded = sdk.loadComputerVisionModel("models/yolov8n.pt");
			if (modelLoaded) {
				YoloV8.DetectionResult[] results = sdk.detectObjects("test_image.jpg");
				System.out.println("   Detection results: " + results.length + " objects found");
				for (YoloV8.DetectionResult result : results) {
					System.out.println("     " + result);
				}
				
				// Test với confidence threshold
				YoloV8.DetectionResult[] highConfResults = sdk.detectObjects("test_image.jpg", 0.8f);
				System.out.println("   High confidence results: " + highConfResults.length + " objects");
			}
			System.out.println();
			
			// 5. Test Services
			System.out.println("🇻🇳 Testing Vietnam Services:");
			System.out.println("   Banking Service: " + (sdk.getBankingService() != null ? "✅ Ready" : "❌ Not available"));
			System.out.println("   ID Card Service: " + (sdk.getIdCardService() != null ? "✅ Ready" : "❌ Not available"));
			System.out.println("   Address Service: " + (sdk.getAddressService() != null ? "✅ Ready" : "❌ Not available"));
			System.out.println();
			
			// 6. Check SDK status
			System.out.println("� SDK Status Check:");
			System.out.println("   Ready: " + (sdk.isReady() ? "✅ Yes" : "❌ No"));
			System.out.println("   Version: " + sdk.getVersion());
			System.out.println("   License Info: " + sdk.getLicenseInfo());
			
		} catch (LicenseValidationException e) {
			System.err.println("❌ Failed to initialize DevHub SDK: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("❌ Unexpected error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// 7. Đóng SDK
			if (sdk != null) {
				sdk.close();
			}
		}
		
		System.out.println("\n🎉 DevHub SDK Demo completed!");
	}
}
