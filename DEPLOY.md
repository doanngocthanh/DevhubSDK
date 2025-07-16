# Deploy to Maven Central Guide

## 🔑 Required Secrets

Thêm vào GitHub Repository → Settings → Secrets and variables → Actions:

### 1. Lấy Central Token từ Sonatype Central

```bash
# 1. Đăng nhập: https://central.sonatype.com/
# 2. Vào Account → Generate User Token
# 3. Copy username và password
```

### 2. Thêm GitHub Secrets

```bash
CENTRAL_USERNAME=your-central-username  # Từ User Token
CENTRAL_TOKEN=your-central-token        # Từ User Token (password)
```

## 🚀 Cách Deploy

### Option 1: Automatic Deploy (qua GitHub Release)

```bash
# 1. Tag và push
git tag v1.0.0
git push origin v1.0.0

# 2. Tạo GitHub Release
# GitHub → Releases → Create a new release
# Tag: v1.0.0
# Title: DevHub SDK v1.0.0
# Click "Publish release"

# 3. GitHub Actions sẽ tự động deploy
```

### Option 2: Manual Deploy (local)

```bash
# 1. Set environment variables
export CENTRAL_USERNAME=your-central-username
export CENTRAL_TOKEN=your-central-token

# 2. Deploy
mvn clean deploy -DskipTests -s .github/settings.xml

# Hoặc sử dụng Central Publishing Plugin
mvn central:upload -DserverId=central
```

## 📝 Test Deployment

### 1. Kiểm tra upload status

```bash
# Vào: https://central.sonatype.com/publishing/deployments
# Xem status của deployment
```

### 2. Kiểm tra package trên Maven Central

```bash
# URL: https://central.sonatype.com/artifact/io.github.doanngocthanh/devhub-sdk
# Hoặc: https://repo1.maven.org/maven2/io/github/doanngocthanh/devhub-sdk/
```

### 3. Test download

```xml
<dependency>
    <groupId>io.github.doanngocthanh</groupId>
    <artifactId>devhub-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🔍 Troubleshooting

### Common Issues

1. **Authentication failed**
   ```
   Solution: Kiểm tra CENTRAL_USERNAME và CENTRAL_TOKEN
   Đảm bảo User Token còn valid
   ```

2. **Namespace not found**
   ```
   Solution: Đảm bảo namespace io.github.doanngocthanh đã được verified
   ```

3. **Missing required metadata**
   ```
   Solution: Kiểm tra pom.xml có đầy đủ:
   - name, description, url
   - licenses
   - developers
   - scm
   ```

4. **Upload timeout**
   ```
   Solution: Retry deployment
   mvn central:upload -DserverId=central
   ```

## 📊 Central Publishing Plugin Commands

```bash
# Upload artifacts
mvn central:upload

# Check upload status
mvn central:publish

# Upload và publish luôn
mvn clean deploy
```

## 🎯 Verify Successful Deploy

1. ✅ GitHub Actions workflow completed successfully
2. ✅ Package visible at: https://central.sonatype.com/artifact/io.github.doanngocthanh/devhub-sdk
3. ✅ Available on Maven Central: https://repo1.maven.org/maven2/io/github/doanngocthanh/devhub-sdk/
4. ✅ Can download via Maven/Gradle

## 📦 Using the Published Package

### Maven
```xml
<dependency>
    <groupId>io.github.doanngocthanh</groupId>
    <artifactId>devhub-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.doanngocthanh:devhub-sdk:1.0.0'
```
