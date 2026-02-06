# HƯỚNG DẪN CẤU HÌNH GOOGLE OAUTH2
## Dự án: Book Management API - Đăng nhập bằng Google

---

## 1. THÔNG TIN CẤU HÌNH ĐÃ SỬ DỤNG

### Google OAuth2 Credentials:
- **Client ID**: `YOUR_GOOGLE_CLIENT_ID`
- **Client Secret**: `YOUR_GOOGLE_CLIENT_SECRET`

### Authorized JavaScript origins:
- `http://localhost:8082`
- `http://localhost:5500`

### Authorized redirect URIs:
- `http://localhost:8082/login/oauth2/code/google`

---

## 2. CẤU HÌNH TRONG ỨNG DỤNG

### File `application.properties`:

```properties
# Google OAuth2 Client ID
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID

# Google OAuth2 Client Secret
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET

# Scope: chi can email va profile
spring.security.oauth2.client.registration.google.scope=email,profile

# Frontend URL
frontend.url=http://localhost:8082/frontend/pages
```

---

## 3. KIỂM TRA HOẠT ĐỘNG

### Bước 1: Khởi động ứng dụng
```bash
mvn spring-boot:run
```

### Bước 2: Truy cập trang đăng nhập
1. Mở trình duyệt: http://localhost:8082/frontend/pages/login.html
2. Click nút "Đăng nhập bằng Google"
3. Chọn tài khoản Google
4. Cho phép quyền truy cập
5. Sau khi thành công, sẽ tự động chuyển về trang chủ

---

## 4. LUỒNG OAUTH2 (AUTHORIZATION CODE GRANT)

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Browser   │      │   Backend   │      │   Google    │      │  Database   │
│  (Frontend) │      │ (Spring)    │      │   OAuth     │      │  (MongoDB)  │
└──────┬──────┘      └──────┬──────┘      └──────┬──────┘      └──────┬──────┘
       │                    │                    │                    │
       │ 1. Click "Google"  │                    │                    │
       │───────────────────>│                    │                    │
       │                    │                    │                    │
       │ 2. Redirect to     │                    │                    │
       │    Google OAuth    │                    │                    │
       │<───────────────────│                    │                    │
       │                    │                    │                    │
       │ 3. Login Google    │                    │                    │
       │────────────────────────────────────────>│                    │
       │                    │                    │                    │
       │ 4. Grant Permission│                    │                    │
       │────────────────────────────────────────>│                    │
       │                    │                    │                    │
       │ 5. Redirect with   │                    │                    │
       │    Auth Code       │                    │                    │
       │<────────────────────────────────────────│                    │
       │                    │                    │                    │
       │ 6. Send Auth Code  │                    │                    │
       │───────────────────>│                    │                    │
       │                    │                    │                    │
       │                    │ 7. Exchange Code   │                    │
       │                    │    for Token       │                    │
       │                    │───────────────────>│                    │
       │                    │                    │                    │
       │                    │ 8. Access Token    │                    │
       │                    │<───────────────────│                    │
       │                    │                    │                    │
       │                    │ 9. Get User Info   │                    │
       │                    │───────────────────>│                    │
       │                    │                    │                    │
       │                    │ 10. User Info      │                    │
       │                    │<───────────────────│                    │
       │                    │                    │                    │
       │                    │ 11. Find/Create    │                    │
       │                    │     User           │                    │
       │                    │────────────────────────────────────────>│
       │                    │                    │                    │
       │                    │ 12. User Data      │                    │
       │                    │<────────────────────────────────────────│
       │                    │                    │                    │
       │                    │ 13. Generate JWT   │                    │
       │                    │                    │                    │
       │ 14. Redirect with  │                    │                    │
       │     JWT Token      │                    │                    │
       │<───────────────────│                    │                    │
       │                    │                    │                    │
       │ 15. Save JWT to    │                    │                    │
       │     localStorage   │                    │                    │
       │                    │                    │                    │
       │ 16. Redirect to    │                    │                    │
       │     Home Page      │                    │                    │
       │                    │                    │                    │
```

---

## 5. CÁC FILE ĐÃ TẠO/SỬA ĐỔI

### Backend (Spring Boot):

| File | Mục đích |
|------|----------|
| `pom.xml` | Thêm dependency `spring-boot-starter-oauth2-client` |
| `application.properties` | Cấu hình Google OAuth2 |
| `model/User.java` | Thêm trường `googleId` |
| `service/OAuth2Service.java` | Xử lý logic tạo/cập nhật user từ Google |
| `controller/OAuth2Controller.java` | Route `/auth/google` (redirect) |
| `security/OAuth2SuccessHandler.java` | Xử lý sau khi đăng nhập Google thành công |
| `security/CookieOAuth2AuthorizationRequestRepository.java` | Lưu OAuth2 request trong cookie |
| `config/SecurityConfig.java` | Cấu hình OAuth2 login |

### Frontend:

| File | Mục đích |
|------|----------|
| `pages/login.html` | Thêm nút "Đăng nhập bằng Google" |
| `js/auth.js` | Xử lý callback OAuth2, lưu token |
| `css/style.css` | Style cho nút Google (đã có sẵn) |

---

## 6. PHÂN QUYỀN VÀ BẢO MẬT

### Quy tắc phân quyền:
- User đăng nhập Google **chỉ có role USER**
- **Không thể tạo ADMIN** bằng Google OAuth2
- ADMIN chỉ được tạo qua đăng ký thường hoặc database

### Bảo mật:
- JWT có thời hạn 24 giờ
- Không lưu password cho user OAuth2
- Token truyền qua URL chỉ 1 lần, sau đó được xóa

---

## 7. XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi: "redirect_uri_mismatch"
- **Nguyên nhân**: URI callback không khớp
- **Giải pháp**: Kiểm tra `Authorized redirect URIs` trên Google Console phải chính xác: `http://localhost:8082/login/oauth2/code/google`

### Lỗi: "invalid_client"
- **Nguyên nhân**: Client ID hoặc Client Secret sai
- **Giải pháp**: Copy lại từ Google Console và cập nhật `application.properties`

### Lỗi: "Access blocked"
- **Nguyên nhân**: App chưa được publish
- **Giải pháp**: Thêm email vào Test users trong OAuth consent screen

---

## 8. DEMO CHO GIẢNG VIÊN

### Chuẩn bị demo:
1. Đảm bảo MongoDB đang chạy
2. Khởi động ứng dụng: `mvn spring-boot:run`

### Các bước demo:
1. Truy cập: http://localhost:8082/frontend/pages/login.html
2. Click "Đăng nhập bằng Google"
3. Chọn tài khoản Google
4. Cho phép quyền truy cập
5. Hệ thống tự động:
   - Tạo user mới (nếu chưa có) hoặc lấy user đã tồn tại
   - Sinh JWT token
   - Redirect về trang chủ
6. Kiểm tra localStorage: thấy `token` và `user`
7. Đăng xuất và đăng nhập lại để verify

### Giải thích code:
1. `OAuth2Controller.java` - Entry point `/auth/google`
2. `OAuth2SuccessHandler.java` - Xử lý sau khi Google trả về
3. `OAuth2Service.java` - Logic tạo/cập nhật user
4. `SecurityConfig.java` - Cấu hình OAuth2 login
5. `login.html` - Nút đăng nhập Google
6. `auth.js` - Xử lý token từ URL

---

**Hoàn thành!** Chức năng đăng nhập bằng Google đã sẵn sàng để demo.
