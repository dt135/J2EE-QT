/**
 * ============================================
 * API CONFIGURATION
 * ============================================
 * File này chứa cấu hình cơ bản cho API calls
 * - Base URL của backend
 * - Hàm tiện ích để gọi API
 * - Xử lý request/response chung
 */

// ============================================
// API BASE CONFIGURATION
// ============================================

// Thay đổi URL này thành URL của backend server
// Nếu chạy local: http://localhost:8081 hoặc http://localhost:3000
const API_BASE_URL = 'http://localhost:8082';

// ============================================
// UTILITY FUNCTIONS
// ============================================

/**
 * Lấy JWT token từ localStorage
 * @returns {string|null} JWT token hoặc null nếu chưa đăng nhập
 */
function getToken() {
    return localStorage.getItem('token');
}

/**
 * Lấy thông tin user từ localStorage
 * @returns {Object|null} Thông tin user hoặc null
 */
function getUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
}

/**
 * Kiểm tra user đã đăng nhập chưa
 * @returns {boolean} true nếu đã đăng nhập
 */
function isAuthenticated() {
    return !!getToken();
}

/**
 * Kiểm tra user có phải admin không
 * @returns {boolean} true nếu là admin
 */
function isAdmin() {
    const user = getUser();
    return user && user.role === 'ADMIN';
}

/**
 * Ẩn/hiện UI dựa trên role
 * - Ẩn các element dành riêng cho ADMIN nếu user không phải ADMIN
 * - Ẩn các element dành riêng cho USER nếu user không phải USER
 */
function updateUIByRole() {
    const user = getUser();
    const role = user ? user.role : null;

    // Ẩn/hiện các element dành riêng cho ADMIN
    const adminOnlyElements = document.querySelectorAll('[data-admin-only]');
    adminOnlyElements.forEach(element => {
        if (role === 'ADMIN') {
            element.style.display = ''; // Hiện
        } else {
            element.style.display = 'none'; // Ẩn
        }
    });

    // Ẩn/hiện các element dành riêng cho USER
    const userOnlyElements = document.querySelectorAll('[data-user-only]');
    userOnlyElements.forEach(element => {
        if (role === 'USER') {
            element.style.display = ''; // Hiện
        } else {
            element.style.display = 'none'; // Ẩn
        }
    });
}

/**
 * Hiển thị loading spinner
 * @param {boolean} show - true để hiện, false để ẩn
 */
function showLoading(show = true) {
    let loadingEl = document.getElementById('loading-overlay');
    
    if (show) {
        if (!loadingEl) {
            loadingEl = document.createElement('div');
            loadingEl.id = 'loading-overlay';
            loadingEl.className = 'loading-overlay';
            loadingEl.innerHTML = '<div class="spinner"></div>';
            document.body.appendChild(loadingEl);
        }
        loadingEl.style.display = 'flex';
    } else if (loadingEl) {
        loadingEl.style.display = 'none';
    }
}

/**
 * Hiển thị toast notification
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại: 'success', 'error', 'warning'
 * @param {number} duration - Thời gian hiển thị (ms)
 */
function showToast(message, type = 'success', duration = 3000) {
    // Tạo toast container nếu chưa có
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    // Tạo toast element
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    // Icon theo loại
    let icon = '✓';
    if (type === 'error') icon = '✕';
    if (type === 'warning') icon = '!';
    
    toast.innerHTML = `
        <span style="font-weight: bold; font-size: 1.2rem;">${icon}</span>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);

    // Xóa toast sau duration
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

/**
 * Hiển thị alert message
 * @param {string} elementId - ID của element chứa alert
 * @param {string} message - Nội dung
 * @param {string} type - Loại: 'success', 'danger', 'warning', 'info'
 */
function showAlert(elementId, message, type = 'danger') {
    const alertEl = document.getElementById(elementId);
    if (alertEl) {
        alertEl.className = `alert alert-${type}`;
        alertEl.textContent = message;
        alertEl.style.display = 'block';
        
        // Tự động ẩn sau 5 giây
        setTimeout(() => {
            alertEl.style.display = 'none';
        }, 5000);
    }
}

// ============================================
// API CALL FUNCTIONS
// ============================================

/**
 * Hàm gọi API chung
 * @param {string} endpoint - API endpoint (không bao gồm base URL)
 * @param {Object} options - Fetch options
 * @returns {Promise<Object>} Response data
 */
async function apiCall(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    // Default headers
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    // Thêm Authorization header nếu có token
    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        showLoading(true);
        
        const response = await fetch(url, config);
        
        // Parse JSON response
        let data;
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        // Kiểm tra lỗi HTTP
        if (!response.ok) {
            throw new Error(data.message || data || `HTTP Error: ${response.status}`);
        }

        // Backend trả về ApiResponse format: { success, message, data }
        // Auto-unwrap để trả về data field cho dễ sử dụng
        if (data && typeof data === 'object' && 'success' in data && 'data' in data) {
            if (!data.success) {
                throw new Error(data.message || 'Request failed');
            }
            return data.data;
        }

        return data;
    } catch (error) {
        console.error('API Call Error:', error);
        throw error;
    } finally {
        showLoading(false);
    }
}

/**
 * GET request
 * @param {string} endpoint - API endpoint
 * @returns {Promise<Object>}
 */
async function apiGet(endpoint) {
    return apiCall(endpoint, {
        method: 'GET'
    });
}

/**
 * POST request
 * @param {string} endpoint - API endpoint
 * @param {Object} body - Request body
 * @returns {Promise<Object>}
 */
async function apiPost(endpoint, body) {
    return apiCall(endpoint, {
        method: 'POST',
        body: JSON.stringify(body)
    });
}

/**
 * PUT request
 * @param {string} endpoint - API endpoint
 * @param {Object} body - Request body
 * @returns {Promise<Object>}
 */
async function apiPut(endpoint, body) {
    return apiCall(endpoint, {
        method: 'PUT',
        body: JSON.stringify(body)
    });
}

/**
 * DELETE request
 * @param {string} endpoint - API endpoint
 * @returns {Promise<Object>}
 */
async function apiDelete(endpoint) {
    return apiCall(endpoint, {
        method: 'DELETE'
    });
}

// ============================================
// NAVIGATION UTILITIES
// ============================================

/**
 * Chuyển hướng đến trang khác
 * @param {string} page - Tên trang (vd: 'index', 'login', 'admin')
 */
function navigateTo(page) {
    window.location.href = `./${page}.html`;
}

/**
 * Cập nhật navbar dựa trên trạng thái đăng nhập
 */
function updateNavbar() {
    const user = getUser();
    const token = getToken();
    const role = user ? user.role : null;

    // Tìm các element trong navbar
    const loginLink = document.getElementById('nav-login');
    const registerLink = document.getElementById('nav-register');
    const userNameDropdown = document.getElementById('nav-username');
    const usernameDisplay = document.getElementById('username-display');
    const adminLink = document.getElementById('nav-admin');
    const cartLink = document.getElementById('nav-cart');
    const ordersLink = document.getElementById('nav-orders');

    if (token && user) {
        // Đã đăng nhập
        if (loginLink) loginLink.style.display = 'none';
        if (registerLink) registerLink.style.display = 'none';

        // Ẩn/hiện giỏ hàng, đơn hàng và admin dựa trên role
        if (role === 'ADMIN') {
            // ADMIN: ẩn giỏ hàng, ẩn đơn hàng, hiện quản lý
            if (cartLink) cartLink.style.display = 'none';
            if (ordersLink) ordersLink.style.display = 'none';
            if (adminLink) adminLink.style.display = 'block';
        } else {
            // USER: hiện giỏ hàng, hiện đơn hàng, ẩn quản lý
            if (cartLink) cartLink.style.display = 'block';
            if (ordersLink) ordersLink.style.display = 'block';
            if (adminLink) adminLink.style.display = 'none';
        }

        // Hiển thị dropdown username và cập nhật tên
        if (userNameDropdown) {
            userNameDropdown.style.display = 'block';
        }
        if (usernameDisplay) {
            usernameDisplay.textContent = user.username || user.email;
        }
    } else {
        // Chưa đăng nhập
        if (loginLink) loginLink.style.display = 'block';
        if (registerLink) registerLink.style.display = 'block';
        if (userNameDropdown) userNameDropdown.style.display = 'none';
        if (adminLink) adminLink.style.display = 'none';
        if (cartLink) cartLink.style.display = 'none';
        if (ordersLink) ordersLink.style.display = 'none';
    }
}

/**
 * Bảo vệ route - chỉ cho phép user đã đăng nhập
 * Chuyển về trang login nếu chưa đăng nhập
 */
function requireAuth() {
    if (!isAuthenticated()) {
        showToast('Vui lòng đăng nhập để tiếp tục', 'warning');
        navigateTo('login');
        return false;
    }
    return true;
}

/**
 * Bảo vệ route - chỉ cho phép admin
 * Chuyển về trang chủ nếu không phải admin
 */
function requireAdmin() {
    if (!isAuthenticated()) {
        showToast('Vui lòng đăng nhập để tiếp tục', 'warning');
        navigateTo('login');
        return false;
    }
    
    if (!isAdmin()) {
        showToast('Bạn không có quyền truy cập trang này', 'error');
        navigateTo('index');
        return false;
    }
    return true;
}

// ============================================
// FORMAT UTILITIES
// ============================================

/**
 * Format số tiền VND
 * @param {number} amount - Số tiền
 * @returns {string} Chuỗi đã format
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

/**
 * Format ngày tháng
 * @param {string} dateString - Chuỗi ngày
 * @returns {string} Ngày đã format
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

// ============================================
// INITIALIZATION
// ============================================

// Update navbar khi trang load
document.addEventListener('DOMContentLoaded', function() {
    updateNavbar();
    updateUIByRole();
});
