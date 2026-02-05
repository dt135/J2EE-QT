# BookStore Frontend

Hệ thống quản lý và bán sách trực tuyến - Frontend Application

## 📚 Giới thiệu

Đây là frontend cho hệ thống quản lý sách sử dụng:
- **HTML5** - Cấu trúc trang web
- **CSS3 (Bootstrap 5)** - Giao diện responsive
- **JavaScript thuần (Vanilla JS)** - Xử lý logic
- **Fetch API** - Giao tiếp với backend

## 📁 Cấu trúc thư mục

```
/frontend
├── /pages
│   ├── index.html          # Trang chủ - Danh sách sách
│   ├── login.html          # Trang đăng nhập
│   ├── register.html       # Trang đăng ký
│   ├── cart.html           # Trang giỏ hàng
│   └── admin.html          # Trang quản lý (Admin)
├── /css
│   └── style.css           # Custom styles
├── /js
│   ├── api.js              # Cấu hình API & utilities
│   ├── auth.js             # Xác thực (login/register/logout)
│   ├── book.js             # Quản lý sách
│   ├── cart.js             # Quản lý giỏ hàng
│   └── admin.js            # Chức năng admin
├── /assets                 # Hình ảnh, fonts
└── README.md
```

## 🚀 Hướng dẫn chạy

### 1. Cấu hình Backend URL

Mở file `js/api.js` và cập nhật URL backend:

```javascript
const API_BASE_URL = 'http://localhost:5000/api';
```

Thay đổi `5000` thành port mà backend của bạn đang chạy.

### 2. Chạy Frontend

#### Cách 1: Sử dụng Live Server (VS Code)
1. Cài đặt extension "Live Server" trong VS Code
2. Mở thư mục `frontend`
3. Click chuột phải vào `pages/index.html` → "Open with Live Server"

#### Cách 2: Sử dụng Python
```bash
cd frontend
python -m http.server 3000
```
Truy cập: http://localhost:3000/pages/

#### Cách 3: Sử dụng Node.js (http-server)
```bash
npm install -g http-server
cd frontend
http-server -p 3000
```
Truy cập: http://localhost:3000/pages/

### 3. Lưu ý về CORS

Backend cần bật CORS để cho phép frontend gọi API:

```javascript
// Trong backend Node.js/Express
const cors = require('cors');
app.use(cors({
    origin: 'http://localhost:3000', // URL của frontend
    credentials: true
}));
```

## 🔌 API Endpoints

### Authentication
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| POST | `/api/auth/register` | Đăng ký tài khoản | ❌ |
| POST | `/api/auth/login` | Đăng nhập | ❌ |
| GET | `/api/auth/me` | Lấy thông tin user | ✅ |

### Books
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/books` | Lấy danh sách sách | ❌ |
| POST | `/api/books` | Thêm sách mới | ✅ (Admin) |
| PUT | `/api/books/:id` | Cập nhật sách | ✅ (Admin) |
| DELETE | `/api/books/:id` | Xóa sách | ✅ (Admin) |

### Cart
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/cart` | Lấy giỏ hàng | ✅ |
| POST | `/api/cart/add` | Thêm vào giỏ | ✅ |
| PUT | `/api/cart/update` | Cập nhật số lượng | ✅ |
| DELETE | `/api/cart/remove/:id` | Xóa khỏi giỏ | ✅ |

### Checkout
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| POST | `/api/checkout` | Thanh toán | ✅ |

### Categories
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/categories` | Lấy danh sách category | ✅ |

**Legend:**
- ❌ Không cần authentication
- ✅ Cần JWT token trong header

## 📋 Chức năng

### 1. User (Ngườ dùng thông thường)
- ✅ Xem danh sách sách
- ✅ Tìm kiếm sách theo từ khóa
- ✅ Lọc sách theo giá, thể loại
- ✅ Thêm sách vào giỏ hàng
- ✅ Xem giỏ hàng
- ✅ Cập nhật số lượng
- ✅ Xóa sách khỏi giỏ
- ✅ Thanh toán (Checkout)

### 2. Admin (Quản trị viên)
- ✅ Xem danh sách sách
- ✅ Thêm sách mới
- ✅ Chỉnh sửa thông tin sách
- ✅ Xóa sách
- ✅ Quản lý category

## 🔐 JWT Authentication

### Lưu trữ Token
- Token được lưu vào `localStorage` sau khi đăng nhập
- Thông tin user được lưu kèm theo

### Gửi Token trong Request
```javascript
headers: {
    'Authorization': 'Bearer <token>',
    'Content-Type': 'application/json'
}
```

### Đăng xuất
- Xóa token khỏi localStorage
- Chuyển về trang login

## 🎨 Giao diện

### Responsive Design
- Mobile-first approach
- Bootstrap 5 Grid System
- Breakpoints: xs, sm, md, lg, xl

### Color Scheme
```css
--primary-color: #2c3e50;    /* Navbar, headings */
--secondary-color: #e74c3c;   /* Price, danger */
--accent-color: #3498db;      /* Buttons, links */
--success-color: #27ae60;     /* Success states */
--warning-color: #f39c12;     /* Warnings */
```

### Components
- Book Cards
- Cart Items
- Admin Tables
- Forms (Login, Register, Book CRUD)
- Modals
- Toast Notifications
- Loading Spinners

## 📱 Responsive Breakpoints

| Breakpoint | Width | Description |
|------------|-------|-------------|
| xs | < 576px | Mobile |
| sm | ≥ 576px | Large phones |
| md | ≥ 768px | Tablets |
| lg | ≥ 992px | Desktops |
| xl | ≥ 1200px | Large desktops |

## 🔧 Tùy chỉnh

### Thay đổi màu sắc
Mở `css/style.css` và sửa CSS variables:

```css
:root {
    --primary-color: #your-color;
    --secondary-color: #your-color;
    --accent-color: #your-color;
}
```

### Thêm endpoint API mới
Trong `js/api.js`, sử dụng các hàm có sẵn:

```javascript
// GET request
const data = await apiGet('/endpoint');

// POST request
const result = await apiPost('/endpoint', { key: 'value' });

// PUT request
const result = await apiPut('/endpoint', { key: 'value' });

// DELETE request
const result = await apiDelete('/endpoint');
```

## 🐛 Xử lý lỗi

### CORS Error
Nếu gặp lỗi CORS, kiểm tra:
1. Backend đã bật CORS chưa
2. Origin trong CORS config có khớp với URL frontend không

### 401 Unauthorized
- Token hết hạn hoặc không hợp lệ
- User sẽ được chuyển về trang login

### 403 Forbidden
- User không có quyền truy cập
- Thường xảy ra khi user thường truy cập trang admin

## 📝 Code Convention

### Comment
- Mỗi file JS có header comment giải thích chức năng
- Mỗi function có JSDoc comment
- Các đoạn code phức tạp có inline comment

### Naming
- Functions: camelCase (`fetchBooks`, `handleLogin`)
- Variables: camelCase (`cartItems`, `currentUser`)
- Constants: UPPER_CASE (`API_BASE_URL`)
- Files: lowercase with hyphens (`api.js`, `book.js`)

### File Structure
- Mỗi file JS phụ trách 1 chức năng cụ thể
- Không duplicate code
- Sử dụng utility functions từ `api.js`

## 🎯 Demo Checklist

### Trang chủ (index.html)
- [ ] Hiển thị danh sách sách dạng card
- [ ] Tìm kiếm sách realtime
- [ ] Lọc theo category
- [ ] Lọc theo giá
- [ ] Sắp xếp sách
- [ ] Thêm vào giỏ hàng

### Đăng nhập (login.html)
- [ ] Form validation
- [ ] Hiển thị lỗi
- [ ] Lưu token vào localStorage
- [ ] Chuyển hướng sau đăng nhập

### Đăng ký (register.html)
- [ ] Form validation
- [ ] Kiểm tra password match
- [ ] Hiển thị thông báo thành công
- [ ] Chuyển đến trang login

### Giỏ hàng (cart.html)
- [ ] Hiển thị items
- [ ] Tăng/giảm số lượng
- [ ] Xóa item
- [ ] Tính tổng tiền
- [ ] Checkout

### Admin (admin.html)
- [ ] Bảo vệ route (chỉ admin)
- [ ] CRUD sách
- [ ] Quản lý category
- [ ] Thống kê

## 👨‍💻 Tác giả

**BookStore Team**
- Frontend: HTML5, CSS3, JavaScript
- Backend: Node.js, Express, MongoDB
- Authentication: JWT

## 📄 License

MIT License - Free for educational use

## 📞 Hỗ trợ

Nếu có vấn đề hoặc câu hỏi, vui lòng liên hệ:
- Email: support@bookstore.com
- Issues: [GitHub Issues]

---

**Chúc bạn thành công với dự án! 🎉**
