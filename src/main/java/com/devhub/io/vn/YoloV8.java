package com.devhub.io.vn;

import security.license.RequiresLicense;
import security.license.RequiresLicense.LicenseLevel;
import security.license.LicenseManager;
import security.license.LicenseProtectedFactory;

/**
 * YoloV8 - Computer Vision Model với License Protection
 * 
 * Features:
 * - Object detection với YoloV8
 * - Automatic license validation
 * - Protected instantiation
 * 
 * @author Đoàn Ngọc Thành
 * @version 1.0.0
 */
@RequiresLicense(
    feature = "YoloV8 Computer Vision",
    level = LicenseLevel.PREMIUM,
    strict = true,
    message = "YoloV8 computer vision requires a Premium license. Contact support@devhub.io.vn"
)
public class YoloV8 {
    
    private static YoloV8 instance = null;
    private static final Object lock = new Object();
    
    private boolean modelLoaded = false;
    private String modelPath = null;
    
    // Protected constructor
    protected YoloV8() {
        // License validation sẽ được thực hiện bởi LicenseManager
        System.out.println("🎯 Initializing YoloV8 Computer Vision Model...");
    }
    
    /**
     * Singleton instance với license protection
     */
    public static YoloV8 getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    // Sử dụng LicenseProtectedFactory để tạo instance
                    instance = LicenseProtectedFactory.createSingleton(YoloV8.class);
                }
            }
        }
        return instance;
    }
    
    /**
     * Load YoloV8 model từ file
     */
    public boolean loadModel(String modelPath) {
        validateLicense();
        
        System.out.println("📁 Loading YoloV8 model from: " + modelPath);
        
        // Simulate model loading
        try {
            Thread.sleep(100); // Simulate loading time
            this.modelPath = modelPath;
            this.modelLoaded = true;
            
            System.out.println("✅ YoloV8 model loaded successfully!");
            return true;
            
        } catch (InterruptedException e) {
            System.err.println("❌ Failed to load model: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Detect objects trong image
     */
    public DetectionResult[] detectObjects(String imagePath) {
        validateLicense();
        
        if (!modelLoaded) {
            throw new IllegalStateException("Model chưa được load. Gọi loadModel() trước.");
        }
        
        System.out.println("🔍 Detecting objects in: " + imagePath);
        
        // Simulate object detection
        return new DetectionResult[] {
            new DetectionResult("person", 0.95f, 100, 50, 200, 300),
            new DetectionResult("car", 0.87f, 300, 150, 150, 100),
            new DetectionResult("bicycle", 0.72f, 50, 200, 80, 120)
        };
    }
    
    /**
     * Detect objects với confidence threshold
     */
    public DetectionResult[] detectObjects(String imagePath, float confidenceThreshold) {
        validateLicense();
        
        DetectionResult[] allResults = detectObjects(imagePath);
        
        // Filter by confidence
        return java.util.Arrays.stream(allResults)
            .filter(result -> result.getConfidence() >= confidenceThreshold)
            .toArray(DetectionResult[]::new);
    }
    
    /**
     * Lấy thông tin model
     */
    public ModelInfo getModelInfo() {
        validateLicense();
        
        return new ModelInfo(
            "YoloV8",
            "1.0.0",
            modelPath,
            modelLoaded,
            "Premium license required"
        );
    }
    
    /**
     * Validate license cho class này
     */
    private void validateLicense() {
        LicenseManager.getInstance().validateLicenseForClass(this.getClass());
    }
    
    /**
     * Đóng model và giải phóng resources
     */
    public void close() {
        validateLicense();
        
        System.out.println("🔒 Closing YoloV8 model...");
        modelLoaded = false;
        modelPath = null;
    }
    
    /**
     * Detection Result class
     */
    public static class DetectionResult {
        private final String className;
        private final float confidence;
        private final int x, y, width, height;
        
        public DetectionResult(String className, float confidence, int x, int y, int width, int height) {
            this.className = className;
            this.confidence = confidence;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public String getClassName() { return className; }
        public float getConfidence() { return confidence; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        
        @Override
        public String toString() {
            return String.format("DetectionResult{class='%s', confidence=%.2f, bbox=[%d,%d,%d,%d]}", 
                                className, confidence, x, y, width, height);
        }
    }
    
    /**
     * Model Info class
     */
    public static class ModelInfo {
        private final String name;
        private final String version;
        private final String path;
        private final boolean loaded;
        private final String licenseInfo;
        
        public ModelInfo(String name, String version, String path, boolean loaded, String licenseInfo) {
            this.name = name;
            this.version = version;
            this.path = path;
            this.loaded = loaded;
            this.licenseInfo = licenseInfo;
        }
        
        public String getName() { return name; }
        public String getVersion() { return version; }
        public String getPath() { return path; }
        public boolean isLoaded() { return loaded; }
        public String getLicenseInfo() { return licenseInfo; }
        
        @Override
        public String toString() {
            return String.format("ModelInfo{name='%s', version='%s', path='%s', loaded=%s, license='%s'}", 
                                name, version, path != null ? path : "Not loaded", loaded, licenseInfo);
        }
    }
}
