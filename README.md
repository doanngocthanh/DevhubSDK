# DevHub SDK

🚀 **DevHub SDK** - Thư viện Java để xác thực bản quyền (license validation) thông qua Google Apps Script

[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

## 📋 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Tính năng](#tính-năng)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt](#cài-đặt)
- [Cấu hình](#cấu-hình)
- [Sử dụng](#sử-dụng)
- [API Reference](#api-reference)
- [Ví dụ](#ví-dụ)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Đóng góp](#đóng-góp)
- [License](#license)

## 🎯 Giới thiệu

DevHub SDK là một thư viện Java mạnh mẽ được thiết kế để xác thực license key thông qua Google Apps Script. Thư viện hỗ trợ nhiều HTTP client, caching thông minh, và retry logic để đảm bảo độ tin cậy cao.

### ✨ Tính năng

- 🔐 **Bảo mật cao**: Sử dụng HMAC SHA256 để tạo chữ ký bảo mật
- 🚄 **Hiệu suất tối ưu**: Cache validation result trong 24 giờ
- 🔄 **Retry logic**: Tự động retry khi gặp lỗi network với exponential backoff
- 🌐 **Đa HTTP client**: Hỗ trợ cả Apache HttpClient và OkHttp
- ⚡ **Async support**: Hỗ trợ validation bất đồng bộ với CompletableFuture
- 🎛️ **Cấu hình linh hoạt**: FlexibleConfig hỗ trợ environment variables
- 📱 **Device fingerprinting**: Tạo device ID duy nhất cho mỗi thiết bị

## 💻 Yêu cầu hệ thống

- **Java**: 8 hoặc cao hơn
- **Maven**: 3.6 hoặc cao hơn
- **Internet connection**: Để kết nối với Google Apps Script

## 📦 Cài đặt

### Maven

Thêm dependency vào `pom.xml`:

```xml
<dependency>
    <groupId>devhub.io.vn</groupId>
    <artifactId>devhub-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'devhub.io.vn:devhub-sdk:0.1.0'
```

### Build từ source

```bash
git clone https://github.com/doanngocthanh/DevhubSDK.git
cd DevhubSDK
mvn clean install
```

## ⚙️ Cấu hình

### 1. Cấu hình Google Apps Script

Đầu tiên, bạn cần thiết lập Google Apps Script endpoint. SDK sẽ tự động ghép các phần của GAS ID:

```java
// Cấu hình trong FlexibleConfig.java
GAS_ID_PART_1 = "AKfycbwr5ZUcUkg26"
GAS_ID_PART_2 = "1nFrALDNJlVACVIgz"
GAS_ID_PART_3 = "l3aywJS_mF_JzHa1A"
GAS_ID_PART_4 = "KWFWWtjYub9KRXv7u"
GAS_ID_PART_5 = "xnNJSw"
```

### 2. Cấu hình Secret Key

```java
SECRET_KEY = "your-secret-key-here-change-this"
```

⚠️ **Lưu ý bảo mật**: Thay đổi secret key mặc định trước khi sử dụng trong production.

### 3. Cấu hình qua Environment Variables

Bạn có thể override cấu hình thông qua environment variables:

```bash
export GAS_WEB_APP_URL="https://script.google.com/macros/s/your-gas-id/exec"
export SECRET_KEY="your-production-secret-key"
export MAX_RETRY="5"
export ENABLE_LOG="false"
```

## 🚀 Sử dụng

### Basic Usage

```java
import security.google.app.script.LicenseValidator;
import security.google.app.script.LicenseValidator.ValidationResult;

// Tạo validator với Apache HttpClient (mặc định)
LicenseValidator validator = new LicenseValidator();

try {
    ValidationResult result = validator.validateLicense("B7GZ-YD59-QMYM-SMSW");
    
    if (result.isSuccess()) {
        System.out.println("✅ License hợp lệ!");
        System.out.println("Email: " + result.getEmail());
        System.out.println("Expires: " + result.getExpires());
    } else {
        System.out.println("❌ License không hợp lệ: " + result.getError());
    }
} catch (LicenseValidationException e) {
    System.err.println("Lỗi xác thực: " + e.getMessage());
}
```

### Sử dụng OkHttp Client

```java
// Tạo validator với OkHttp
LicenseValidator validator = new LicenseValidator(HttpClientType.OK_HTTP);

ValidationResult result = validator.validateLicense("your-license-key");
```

### Async Validation

```java
validator.validateLicenseAsync("your-license-key")
    .thenAccept(result -> {
        System.out.println("Kết quả async: " + result);
    })
    .exceptionally(throwable -> {
        System.err.println("Lỗi async: " + throwable.getMessage());
        return null;
    });
```

### Cache Management

```java
// Kiểm tra cache
if (validator.isLicenseValid()) {
    System.out.println("License đã được cache và còn hợp lệ");
}

// Xóa cache
validator.clearCache();
```

## 📚 API Reference

### LicenseValidator

#### Constructors

- `LicenseValidator()` - Sử dụng Apache HttpClient mặc định
- `LicenseValidator(HttpClientType clientType)` - Chọn HTTP client type

#### Methods

- `ValidationResult validateLicense(String licenseKey)` - Xác thực license đồng bộ
- `CompletableFuture<ValidationResult> validateLicenseAsync(String licenseKey)` - Xác thực license bất đồng bộ
- `boolean isLicenseValid()` - Kiểm tra cache validation
- `void clearCache()` - Xóa cache validation
- `void close()` - Đóng HTTP clients

### ValidationResult

#### Properties

- `boolean isSuccess()` - Kết quả xác thực
- `String getEmail()` - Email của license
- `String getExpires()` - Ngày hết hạn
- `String getMessage()` - Thông báo từ server
- `String getError()` - Thông báo lỗi (nếu có)

### HttpClientType

- `APACHE_HTTP_CLIENT` - Sử dụng Apache HttpClient
- `OK_HTTP` - Sử dụng OkHttp

## 🔧 Ví dụ

### Ví dụ đầy đủ

```java
public class LicenseExample {
    public static void main(String[] args) {
        // Test với Apache HttpClient
        testWithApacheHttpClient();
        
        // Test với OkHttp
        testWithOkHttp();
        
        // Test async validation
        testAsyncValidation();
    }
    
    private static void testWithApacheHttpClient() {
        System.out.println("=== Testing Apache HttpClient ===");
        LicenseValidator validator = new LicenseValidator(HttpClientType.APACHE_HTTP_CLIENT);
        
        try {
            ValidationResult result = validator.validateLicense("B7GZ-YD59-QMYM-SMSW");
            System.out.println("Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                validator.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void testWithOkHttp() {
        System.out.println("=== Testing OkHttp ===");
        LicenseValidator validator = new LicenseValidator(HttpClientType.OK_HTTP);
        
        try {
            ValidationResult result = validator.validateLicense("B7GZ-YD59-QMYM-SMSW");
            System.out.println("Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void testAsyncValidation() {
        System.out.println("=== Testing Async Validation ===");
        LicenseValidator validator = new LicenseValidator();
        
        validator.validateLicenseAsync("B7GZ-YD59-QMYM-SMSW")
            .thenAccept(result -> System.out.println("Async result: " + result))
            .exceptionally(throwable -> {
                System.err.println("Async error: " + throwable.getMessage());
                return null;
            });
    }
}
```

### Error Handling

```java
try {
    ValidationResult result = validator.validateLicense("invalid-key");
} catch (LicenseValidationException e) {
    // Handle validation errors
    System.err.println("Validation failed: " + e.getMessage());
    
    // Check if it's a network error
    if (e.getCause() instanceof IOException) {
        System.err.println("Network error - check internet connection");
    }
} catch (Exception e) {
    // Handle other errors
    System.err.println("Unexpected error: " + e.getMessage());
}
```

## 📁 Cấu trúc dự án

```
DevhubSDK/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── config/
│   │   │   │   └── FlexibleConfig.java      # Cấu hình linh hoạt
│   │   │   └── security/
│   │   │       ├── CryptoUtils.java         # Utilities mã hóa
│   │   │       └── google/
│   │   │           └── app/
│   │   │               └── script/
│   │   │                   └── LicenseValidator.java  # Core validator
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
├── target/                                  # Build artifacts
├── pom.xml                                  # Maven configuration
└── README.md                               # Documentation
```

## 🔒 Security Features

### HMAC Signature

SDK sử dụng HMAC SHA256 để tạo chữ ký bảo mật cho mỗi request:

```java
String payload = licenseKey + deviceId + timestamp;
Mac mac = Mac.getInstance("HmacSHA256");
SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
mac.init(keySpec);
byte[] signature = mac.doFinal(payload.getBytes());
```

### Device Fingerprinting

Tạo device ID duy nhất dựa trên thông tin hệ thống:

```java
String systemInfo = System.getProperty("os.name") + 
                   System.getProperty("os.version") + 
                   System.getProperty("user.name") + 
                   System.getProperty("java.version");
```

## 🎛️ Configuration Options

| Key | Default | Mô tả |
|-----|---------|-------|
| `GAS_WEB_APP_URL` | Auto-generated | URL của Google Apps Script |
| `SECRET_KEY` | `your-secret-key-here-change-this` | Secret key cho HMAC |
| `MAX_RETRY` | `3` | Số lần retry tối đa |
| `ENABLE_LOG` | `true` | Bật/tắt logging |

## 🐛 Troubleshooting

### Common Issues

1. **Network timeout**
   ```
   Solution: Kiểm tra kết nối internet và cấu hình firewall
   ```

2. **Invalid signature**
   ```
   Solution: Đảm bảo SECRET_KEY giống nhau giữa client và server
   ```

3. **Cache not working**
   ```
   Solution: Kiểm tra system time và gọi clearCache() nếu cần
   ```

### Debug Mode

Bật debug logging:

```bash
export ENABLE_LOG=true
```

## 🤝 Đóng góp

Chúng tôi hoan nghênh mọi đóng góp! Vui lòng:

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Tạo Pull Request

### Development Setup

```bash
git clone https://github.com/doanngocthanh/DevhubSDK.git
cd DevhubSDK
mvn clean compile
```

### Running Tests

```bash
mvn test
```

## 📞 Hỗ trợ

- **Email**: dnt.doanngocthanh@gmail.com
- **GitHub Issues**: [Issues page](https://github.com/doanngocthanh/DevhubSDK/issues)
- **Documentation**: [Wiki](https://github.com/doanngocthanh/DevhubSDK/wiki)

## 📄 License

Dự án này được cấp phép theo [MIT License](LICENSE).

```
MIT License

Copyright (c) 2025 Đoàn Ngọc Thành

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

⭐ **Nếu thấy hữu ích, hãy star repository này!**

**Made with ❤️ by [Đoàn Ngọc Thành](https://github.com/doanngocthanh)**
