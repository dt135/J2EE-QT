# BOOK MANAGEMENT API - Spring Boot

API Quan ly sach - Xay dung voi Spring Boot, MongoDB va JWT

**Tac gia:** Dang Doan Toai - 2280603283

## Tong quan

Du an Web Quan ly Sach day du, ap dung cac chuc nang tu BAI 3 den BAI 8:

| Bai | Chuc nang | Mo ta |
|-----|-----------|-------|
| 3 | Book Management | CRUD sach, tim kiem, loc theo gia |
| 4 | Category | CRUD danh muc |
| 5 | Cart & Checkout | Gio hang, thanh toan, hoa don |
| 6 | Authentication | Dang ky, dang nhap, JWT |
| 7 | Authorization | Phan quyen ADMIN/USER |
| 8 | REST API & Security | CORS, Bearer Token |

## Cong nghe su dung

- Java 21
- Spring Boot 3.2.0
- Spring Security
- Spring Data MongoDB
- JWT (jjwt 0.12.3)
- Lombok
- Maven

## Cau truc du an

```
src/main/java/com/app/dangdoanhtoai2280603283/
├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── MongoConfig.java
├── controller/
│   ├── AuthController.java
│   ├── BookController.java
│   ├── CategoryController.java
│   ├── CartController.java
│   ├── InvoiceController.java
│   ├── ApiController.java
│   └── HealthController.java
├── dto/
│   ├── ApiResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── AuthResponse.java
│   ├── BookRequest.java
│   ├── CategoryRequest.java
│   ├── CartRequest.java
│   ├── CartResponse.java
│   ├── CheckoutResponse.java
│   └── PageResponse.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── GlobalExceptionHandler.java
├── model/
│   ├── User.java
│   ├── Role.java
│   ├── Category.java
│   ├── Book.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Invoice.java
│   └── Item.java
├── repository/
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── BookRepository.java
│   ├── CartRepository.java
│   ├── InvoiceRepository.java
│   └── ItemRepository.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/
│   ├── AuthService.java
│   ├── BookService.java
│   ├── CategoryService.java
│   ├── CartService.java
│   └── InvoiceService.java
└── Dangdoanhtoai2280603283Application.java
```

## Cai dat va Chay du an

### Yeu cau

- Java 21+
- Maven 3.6+
- MongoDB 5.0+

### Buoc 1: Cai dat MongoDB

Dam bao MongoDB dang chay tai `localhost:27017`

### Buoc 2: Chay du an

```bash
# Build du an
mvn clean install

# Chay ung dung
mvn spring-boot:run
```

Server se chay tai: `http://localhost:8080`

## API Endpoints

### Authentication (BAI 6)

| Method | Endpoint | Mo ta | Access |
|--------|----------|-------|--------|
| POST | /auth/register | Dang ky | Public |
| POST | /auth/login | Dang nhap | Public |
| GET | /auth/me | Thong tin user | Private |

### Category (BAI 4)

| Method | Endpoint | Mo ta | Access |
|--------|----------|-------|--------|
| GET | /categories | Lay tat ca | Public |
| GET | /categories/:id | Chi tiet | Public |
| POST | /categories | Tao moi | ADMIN |
| PUT | /categories/:id | Cap nhat | ADMIN |
| DELETE | /categories/:id | Xoa | ADMIN |

### Book (BAI 3)

| Method | Endpoint | Mo ta | Access |
|--------|----------|-------|--------|
| GET | /books | Lay tat ca | Public |
| GET | /books/search?keyword= | Tim kiem | Public |
| GET | /books/filter?price=&limit= | Loc gia | Public |
| GET | /books/:id | Chi tiet | Public |
| POST | /books | Them moi | ADMIN |
| PUT | /books/:id | Cap nhat | ADMIN |
| DELETE | /books/:id | Xoa | ADMIN |

### Cart (BAI 5)

| Method | Endpoint | Mo ta | Access |
|--------|----------|-------|--------|
| GET | /cart | Lay gio hang | Private |
| POST | /cart/add | Them sach | Private |
| PUT | /cart/update | Cap nhat SL | Private |
| DELETE | /cart/remove | Xoa sach | Private |
| DELETE | /cart/clear | Xoa gio | Private |

### Checkout & Invoice (BAI 5)

| Method | Endpoint | Mo ta | Access |
|--------|----------|-------|--------|
| POST | /checkout | Thanh toan | Private |
| GET | /invoices | Lich su | Private |
| GET | /invoices/:id | Chi tiet | Private |
| GET | /invoices/all | Tat ca | ADMIN |

### REST API (BAI 8)

| Method | Endpoint | Mo ta | Access |
|--------|----------|-------|--------|
| GET | /api/books | Lay sach | Public |
| GET | /api/books/:id | Chi tiet | Public |
| DELETE | /api/books/:id | Xoa | ADMIN |

## Vi du Request/Response

### Dang ky
```http
POST /auth/register
Content-Type: application/json

{
  "username": "user1",
  "email": "user1@example.com",
  "password": "123456"
}
```

### Dang nhap
```http
POST /auth/login
Content-Type: application/json

{
  "username": "user1",
  "password": "123456"
}
```

Response:
```json
{
  "success": true,
  "message": "Dang nhap thanh cong",
  "data": {
    "id": "...",
    "username": "user1",
    "email": "user1@example.com",
    "role": "USER",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### Them sach (ADMIN)
```http
POST /books
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "JavaScript Co Ban",
  "author": "Nguyen Van A",
  "price": 150000,
  "categoryId": "<category_id>",
  "description": "Sach day JavaScript"
}
```

### Tim kiem sach
```http
GET /books/search?keyword=javascript
```

### Loc sach theo gia
```http
GET /books/filter?price=100000&limit=5
```

## Phan quyen

| Role | Quyen |
|------|-------|
| ADMIN | CRUD Book, Category, xem tat ca hoa don |
| USER | Xem sach, them gio hang, mua sach |

## License

ISC
