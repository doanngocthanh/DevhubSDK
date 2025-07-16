# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2025-07-16

### Added
- Initial release of DevHub SDK
- License validation through Google Apps Script
- HMAC SHA256 signature security
- Support for Apache HttpClient and OkHttp
- Async validation with CompletableFuture
- Smart caching system (24-hour cache)
- Retry logic with exponential backoff
- Device fingerprinting
- Flexible configuration with environment variables support
- FlexibleConfig utility class
- CryptoUtils utility class
- Comprehensive error handling
- MIT License

### Features
- 🔐 High security with HMAC SHA256 signatures
- 🚄 Optimized performance with 24-hour result caching
- 🔄 Auto-retry logic with exponential backoff
- 🌐 Multiple HTTP client support (Apache HttpClient & OkHttp)
- ⚡ Async support with CompletableFuture
- 🎛️ Flexible configuration with environment variables
- 📱 Device fingerprinting for unique device identification

### Security
- HMAC SHA256 signature validation
- Secure device ID generation
- Environment variable support for sensitive configuration
- Configurable secret key management

### Documentation
- Complete README.md with usage examples
- API reference documentation
- Troubleshooting guide
- Configuration options documentation
- Security features explanation

### Dependencies
- Java 8+
- Maven 3.6+
- Google Gson 2.10.1
- Apache HttpClient 4.5.13
- OkHttp 4.12.0
- JSON 20210307
