package com.devhub.io.vn;
import java.util.HashMap;
import java.util.Map;

public class FlexibleConfig {

    // Bộ nhớ tạm cấu hình (runtime)
    private static final Map<String, String> configMap = new HashMap<>();

    static {
    	configMap.put("HOST", "https://script.google.com/macros");
    	configMap.put("PATH", "/s/");
    	configMap.put("GAS_ID_PART_1", "AKfycbwr5ZUcUkg26");
    	configMap.put("GAS_ID_PART_2", "1nFrALDNJlVACVIgz");
    	configMap.put("GAS_ID_PART_3", "l3aywJS_mF_JzHa1A");
    	configMap.put("GAS_ID_PART_4", "KWFWWtjYub9KRXv7u");
    	configMap.put("GAS_ID_PART_5", "xnNJSw");
    	String gasId = configMap.get("GAS_ID_PART_1")
    	           + configMap.get("GAS_ID_PART_2")
    	           + configMap.get("GAS_ID_PART_3")
    	           + configMap.get("GAS_ID_PART_4")
    	           + configMap.get("GAS_ID_PART_5");
    	configMap.put("GAS_WEB_APP_URL", configMap.get("HOST") + configMap.get("PATH") + gasId + "/exec");
        configMap.put("SECRET_KEY", "your-secret-key-here-change-this");
        configMap.put("MAX_RETRY", "3");
        configMap.put("ENABLE_LOG", "true");
    }

    // Lấy giá trị cấu hình (ưu tiên biến môi trường)
    public static String get(String key) {
        String env = System.getenv(key);
        if (env != null) return env;
        return configMap.get(key);
    }

    // Ghi đè cấu hình tại runtime
    public static void set(String key, String value) {
        configMap.put(key, value);
    }

    // Kiểm tra cấu hình đã tồn tại
    public static boolean has(String key) {
        return configMap.containsKey(key);
    }

    // Ví dụ: chuyển sang int/bool
    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        return val != null ? val.equalsIgnoreCase("true") : defaultValue;
    }
}
