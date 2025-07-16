package security.google.app.script;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import config.FlexibleConfig;

// Apache HttpClient
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

// OkHttp
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * SDK License Validator - Xác thực bản quyền với Google Apps Script
 * 
 * Chức năng: - Tạo chữ ký HMAC bảo mật - Gửi request xác thực đến GAS (hỗ trợ
 * Apache HttpClient & OkHttp) - Xử lý response và cache kết quả - Auto-retry
 * khi network lỗi
 * 
 * @author Your Name
 * @version 2.0 - Updated with Apache HttpClient & OkHttp
 */
public class LicenseValidator {

	// Cấu hình
	private static final String GAS_WEB_APP_URL =  FlexibleConfig.get("GAS_WEB_APP_URL");//"https://script.google.com/macros/s/..../exec";
	private static final String SECRET_KEY =FlexibleConfig.get("SECRET_KEY");;// "your-secret-key-here-change-this";
	private static final int TIMEOUT_SECONDS = 30;
	private static final int MAX_RETRIES = 3;

	// Cache validation để tránh spam requests
	private static ValidationResult cachedResult = null;
	private static long lastValidationTime = 0;
	private static final long CACHE_DURATION_MS = 24 * 60 * 60 * 1000; // 24 giờ

	// HTTP Clients
	private final CloseableHttpClient apacheHttpClient;
	private final OkHttpClient okHttpClient;
	private final Gson gson;
	private final String deviceId;
	private final HttpClientType clientType;

	public enum HttpClientType {
		APACHE_HTTP_CLIENT, OK_HTTP
	}

	/**
	 * Constructor mặc định - sử dụng Apache HttpClient
	 */
	public LicenseValidator() {
		this(HttpClientType.APACHE_HTTP_CLIENT);
	}

	/**
	 * Constructor với lựa chọn HTTP client
	 */
	public LicenseValidator(HttpClientType clientType) {
		this.clientType = clientType;
		this.gson = new Gson();
		this.deviceId = generateDeviceId();

		// Khởi tạo Apache HttpClient
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT_SECONDS * 1000)
				.setConnectTimeout(TIMEOUT_SECONDS * 1000).setConnectionRequestTimeout(TIMEOUT_SECONDS * 1000).build();

		this.apacheHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

		// Khởi tạo OkHttpClient
		this.okHttpClient = new OkHttpClient.Builder().connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
				.writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS).readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS).build();
	}

	/**
	 * Xác thực license key với server
	 * 
	 * @param licenseKey License key cần xác thực
	 * @return ValidationResult chứa thông tin xác thực
	 * @throws LicenseValidationException nếu có lỗi xác thực
	 */
	public ValidationResult validateLicense(String licenseKey) throws LicenseValidationException {

		// Kiểm tra cache trước
		if (isValidationCached()) {
			System.out.println("✅ Using cached validation result");
			return cachedResult;
		}

		try {
			// Tạo request data
			LicenseRequest request = createLicenseRequest(licenseKey);

			// Gửi request với retry logic
			ValidationResult result = sendRequestWithRetry(request);

			// Cache kết quả nếu thành công
			if (result.isSuccess()) {
				cachedResult = result;
				lastValidationTime = System.currentTimeMillis();
				System.out.println("✅ License validation successful - cached for 24h");
			}

			return result;

		} catch (Exception e) {
			throw new LicenseValidationException("Failed to validate license: " + e.getMessage(), e);
		}
	}

	/**
	 * Xác thực bất đồng bộ
	 */
	public CompletableFuture<ValidationResult> validateLicenseAsync(String licenseKey) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return validateLicense(licenseKey);
			} catch (LicenseValidationException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Tạo license request với chữ ký HMAC
	 */
	private LicenseRequest createLicenseRequest(String licenseKey) throws Exception {
		long timestamp = System.currentTimeMillis();

		LicenseRequest request = new LicenseRequest();
		request.licenseKey = licenseKey;
		request.deviceId = deviceId;
		request.timestamp = timestamp;
		request.signature = createHmacSignature(licenseKey, deviceId, timestamp);

		return request;
	}

	/**
	 * Tạo chữ ký HMAC SHA256
	 */
	private String createHmacSignature(String licenseKey, String deviceId, long timestamp)
			throws NoSuchAlgorithmException, InvalidKeyException {

		String payload = licenseKey + deviceId + timestamp;

		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		mac.init(keySpec);

		byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(signature);
	}

	/**
	 * Gửi request với retry logic
	 */
	private ValidationResult sendRequestWithRetry(LicenseRequest request) throws Exception {
		Exception lastException = null;

		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
			try {
				System.out.println(
						"🔄 Validation attempt " + attempt + "/" + MAX_RETRIES + " using " + clientType.name());
				return sendRequest(request);

			} catch (Exception e) {
				lastException = e;
				System.err.println("❌ Attempt " + attempt + " failed: " + e.getMessage());

				if (attempt < MAX_RETRIES) {
					// Exponential backoff
					long delay = (long) (Math.pow(2, attempt) * 1000);
					System.out.println("⏳ Retrying in " + delay + "ms...");
					Thread.sleep(delay);
				}
			}
		}

		throw new LicenseValidationException("All retry attempts failed", lastException);
	}

	/**
	 * Gửi HTTP request đến GAS - chọn client type
	 */
	private ValidationResult sendRequest(LicenseRequest request) throws Exception {
		switch (clientType) {
		case APACHE_HTTP_CLIENT:
			return sendRequestWithApacheHttpClient(request);
		case OK_HTTP:
			return sendRequestWithOkHttp(request);
		default:
			throw new IllegalStateException("Unsupported HTTP client type: " + clientType);
		}
	}

	/**
	 * Gửi request với Apache HttpClient
	 */
	private ValidationResult sendRequestWithApacheHttpClient(LicenseRequest request) throws Exception {
		String jsonPayload = gson.toJson(request);

		// Tạo client với redirect handling
		CloseableHttpClient clientWithRedirect = HttpClientBuilder.create()
				.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(TIMEOUT_SECONDS * 1000)
						.setConnectTimeout(TIMEOUT_SECONDS * 1000).setConnectionRequestTimeout(TIMEOUT_SECONDS * 1000)
						.setRedirectsEnabled(true).setMaxRedirects(5).build())
				.build();

		HttpPost httpPost = new HttpPost(GAS_WEB_APP_URL);
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setHeader("User-Agent", "Java-SDK-License-Validator-Apache/2.0");

		StringEntity entity = new StringEntity(jsonPayload, StandardCharsets.UTF_8);
		httpPost.setEntity(entity);

		try {
			HttpResponse response = clientWithRedirect.execute(httpPost);

			int statusCode = response.getStatusLine().getStatusCode();
			String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

			if (statusCode != 200) {
				throw new IOException("HTTP " + statusCode + ": " + responseBody);
			}

			return parseResponse(responseBody);
		} finally {
			clientWithRedirect.close();
		}
	}

	/**
	 * Gửi request với OkHttp
	 */
	private ValidationResult sendRequestWithOkHttp(LicenseRequest request) throws Exception {
		String jsonPayload = gson.toJson(request);

		// Tạo client với redirect handling
		OkHttpClient clientWithRedirect = okHttpClient.newBuilder().followRedirects(true).followSslRedirects(true)
				.build();

		MediaType JSON = MediaType.get("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(jsonPayload, JSON);

		Request httpRequest = new Request.Builder().url(GAS_WEB_APP_URL)
				.header("User-Agent", "Java-SDK-License-Validator-OkHttp/2.0").post(body).build();

		try (Response response = clientWithRedirect.newCall(httpRequest).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("HTTP " + response.code() + ": " + response.body().string());
			}

			String responseBody = response.body().string();
			return parseResponse(responseBody);
		}
	}

	/**
	 * Parse JSON response từ GAS
	 */
	private ValidationResult parseResponse(String responseBody) throws Exception {
		JsonObject json = gson.fromJson(responseBody, JsonObject.class);

		ValidationResult result = new ValidationResult();
		result.success = json.get("success").getAsBoolean();

		if (result.success) {
			if (json.has("email")) {
				result.email = json.get("email").getAsString();
			}
			if (json.has("expires")) {
				result.expires = json.get("expires").getAsString();
			}
			if (json.has("message")) {
				result.message = json.get("message").getAsString();
			}
		} else {
			result.error = json.get("error").getAsString();
		}

		return result;
	}

	/**
	 * Kiểm tra cache còn hợp lệ không
	 */
	private boolean isValidationCached() {
		return cachedResult != null && cachedResult.isSuccess()
				&& (System.currentTimeMillis() - lastValidationTime) < CACHE_DURATION_MS;
	}

	/**
	 * Tạo device ID duy nhất
	 */
	private String generateDeviceId() {
		try {
			// Kết hợp các thông tin hệ thống
			String systemInfo = System.getProperty("os.name") + System.getProperty("os.version")
					+ System.getProperty("user.name") + System.getProperty("java.version");

			// Tạo hash từ system info
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(systemInfo.getBytes(StandardCharsets.UTF_8));

			// Chuyển thành hex string
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}

			return "device-" + sb.toString().substring(0, 12);

		} catch (Exception e) {
			// Fallback: random string
			return "device-" + System.currentTimeMillis();
		}
	}

	/**
	 * Clear cache validation
	 */
	public void clearCache() {
		cachedResult = null;
		lastValidationTime = 0;
		System.out.println("🗑️ Validation cache cleared");
	}

	/**
	 * Kiểm tra license có hợp lệ không (từ cache)
	 */
	public boolean isLicenseValid() {
		return isValidationCached();
	}

	/**
	 * Đóng HTTP clients
	 */
	public void close() throws IOException {
		if (apacheHttpClient != null) {
			apacheHttpClient.close();
		}
		// OkHttp tự động quản lý connection pool
	}

	// ============================================================
	// INNER CLASSES
	// ============================================================

	/**
	 * Request data gửi đến GAS
	 */
	private static class LicenseRequest {
		String licenseKey;
		String deviceId;
		long timestamp;
		String signature;

		// Constructor
		public LicenseRequest() {
		}

		// Getters/Setters for JSON serialization
		public String getLicenseKey() {
			return licenseKey;
		}

		public void setLicenseKey(String licenseKey) {
			this.licenseKey = licenseKey;
		}

		public String getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public String getSignature() {
			return signature;
		}

		public void setSignature(String signature) {
			this.signature = signature;
		}
	}

	/**
	 * Kết quả xác thực từ GAS
	 */
	public static class ValidationResult {
		boolean success;
		String email;
		String expires;
		String message;
		String error;

		public boolean isSuccess() {
			return success;
		}

		public String getEmail() {
			return email;
		}

		public String getExpires() {
			return expires;
		}

		public String getMessage() {
			return message;
		}

		public String getError() {
			return error;
		}

		@Override
		public String toString() {
			if (success) {
				return String.format("✅ Valid license for %s (expires: %s)", email,
						expires != null ? expires : "never");
			} else {
				return String.format("❌ Invalid license: %s", error);
			}
		}
	}

	/**
	 * Exception cho license validation
	 */
	public static class LicenseValidationException extends Exception {
		public LicenseValidationException(String message) {
			super(message);
		}

		public LicenseValidationException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	// ============================================================
	// DEMO USAGE
	// ============================================================

	/**
	 * Ví dụ sử dụng SDK
	 */
	public static void main(String[] args) {
		// Test với Apache HttpClient
		System.out.println("🚀 Testing with Apache HttpClient");
		System.out.println("=================================");
		// testWithClient(HttpClientType.APACHE_HTTP_CLIENT);

		System.out.println("\n");

		// Test với OkHttp
		// System.out.println("🚀 Testing with OkHttp");
		System.out.println("======================");
		testWithClient(HttpClientType.OK_HTTP);
	}

	private static void testWithClient(HttpClientType clientType) {
		LicenseValidator validator = new LicenseValidator(clientType);

		// Test license keys
		String[] testKeys = { "B7GZ-YD59-QMYM-SMSW" };

		for (String key : testKeys) {
			try {
				System.out.println("\n🔍 Testing key: " + key);
				ValidationResult result = validator.validateLicense(key);
				System.out.println("Result: " + result);

				if (result.isSuccess()) {
					System.out.println("✅ SUCCESS: License is valid!");
				} else {
					System.out.println("❌ FAILED: " + result.getError());
				}

			} catch (LicenseValidationException e) {
				System.err.println("💥 ERROR: " + e.getMessage());
			}
		}

		// Test async validation
		System.out.println("\n🔄 Testing async validation...");
		validator.validateLicenseAsync("B7GZ-YD59-QMYM-SMSW").thenAccept(result -> {
			System.out.println("🎯 Async result: " + result);
		}).exceptionally(throwable -> {
			System.err.println("💥 Async error: " + throwable.getMessage());
			return null;
		});

		// Test cache
		System.out.println("\n💾 Testing cache...");
		System.out.println("Is cached: " + validator.isLicenseValid());

		// Clear cache
		validator.clearCache();
		System.out.println("Is cached after clear: " + validator.isLicenseValid());

		// Đóng client
		try {
			validator.close();
		} catch (IOException e) {
			System.err.println("Error closing client: " + e.getMessage());
		}
	}
}