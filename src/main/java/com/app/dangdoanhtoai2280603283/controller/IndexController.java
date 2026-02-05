package com.app.dangdoanhtoai2280603283.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Index Controller - Trang chu
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public String index() {
        return """
            ==================================================
              BOOK MANAGEMENT API
              Dang Doanh Toai - 2280603283
            ==================================================
            
            Available Endpoints:
            
            Public (No authentication required):
            - GET  /api/books              - Get all books (paginated)
            - GET  /api/books/{id}         - Get book details by ID
            - POST /auth/register          - Register new user
            - POST /auth/login             - Login and get JWT token
            
            Authenticated (JWT token required):
            - GET  /cart                   - Get user's cart
            - POST /cart                   - Add item to cart
            - POST /checkout               - Place order
            - GET  /invoices               - Get user's invoices
            
            Admin Only (ADMIN role required):
            - DELETE /api/books/{id}       - Delete book
            - POST /api/migrate-books      - Migrate books to new format
            - POST /books                  - Create book
            - PUT /books/{id}              - Update book
            - POST /categories             - Create category
            - PUT /categories/{id}         - Update category
            - DELETE /categories/{id}      - Delete category
            - GET /users                   - Get all users
            - GET /invoices/all            - Get all invoices
            - DELETE /invoices/{id}        - Delete invoice
            
            ==================================================
            """;
    }
}
