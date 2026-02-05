/**
 * ============================================
 * AUTHENTICATION MODULE
 * ============================================
 * File này xử lý các chức năng xác thực:
 * - Đăng ký (Register)
 * - Đăng nhập (Login)
 * - Đăng xuất (Logout)
 * - Quản lý JWT token
 * - Kiểm tra quyền (Role-based Access Control)
 *
 * API Endpoints:
 * POST /auth/register - Đăng ký tài khoản mới
 * POST /auth/login    - Đăng nhập
 * POST /auth/logout   - Đăng xuất (optional)
 * GET  /auth/me       - Lấy thông tin user hiện tại
 */

// ============================================
// REGISTER FUNCTION
// ============================================

/**
 * Xử lý đăng ký tài khoản mới
 * @param {Event} event - Form submit event
 */
async function handleRegister(event) {
    event.preventDefault();
    
    // Lấy dữ liệu từ form
    const formData = {
        username: document.getElementById('username').value.trim(),
        email: document.getElementById('email').value.trim(),
        password: document.getElementById('password').value,
        confirmPassword: document.getElementById('confirmPassword').value
    };
    
    // Validate
    if (!validateRegisterForm(formData)) {
        return;
    }
    
    try {
        // Gọi API đăng ký
        const data = await apiPost('/auth/register', {
            username: formData.username,
            email: formData.email,
            password: formData.password
        });
        
        // Đăng ký thành công
        showToast('Đăng ký thành công! Chuyển đến trang đăng nhập...', 'success');
        
        // Chuyển đến trang login sau 1.5 giây
        setTimeout(() => {
            navigateTo('login');
        }, 1500);
        
    } catch (error) {
        showAlert('register-alert', error.message || 'Đăng ký thất bại. Vui lòng thử lại.', 'danger');
    }
}

/**
 * Validate form đăng ký
 * @param {Object} data - Dữ liệu form
 * @returns {boolean} true nếu hợp lệ
 */
function validateRegisterForm(data) {
    // Kiểm tra username
    if (data.username.length < 3) {
        showAlert('register-alert', 'Tên đăng nhập phải có ít nhất 3 ký tự', 'danger');
        return false;
    }
    
    // Kiểm tra email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(data.email)) {
        showAlert('register-alert', 'Email không hợp lệ', 'danger');
        return false;
    }
    
    // Kiểm tra password
    if (data.password.length < 6) {
        showAlert('register-alert', 'Mật khẩu phải có ít nhất 6 ký tự', 'danger');
        return false;
    }
    
    // Kiểm tra confirm password
    if (data.password !== data.confirmPassword) {
        showAlert('register-alert', 'Mật khẩu xác nhận không khớp', 'danger');
        return false;
    }
    
    return true;
}

// ============================================
// LOGIN FUNCTION
// ============================================

/**
 * Xử lý đăng nhập
 * @param {Event} event - Form submit event
 */
async function handleLogin(event) {
    event.preventDefault();
    
    // Lấy dữ liệu từ form
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    
    // Validate
    if (!email || !password) {
        showAlert('login-alert', 'Vui lòng nhập email và mật khẩu', 'danger');
        return;
    }
    
    try {
        // Gọi API đăng nhập
        const data = await apiPost('/auth/login', {
            username: email,
            password: password
        });
        
        // Backend trả về AuthResponse: { id, username, email, role, token }
        // Lưu token và thông tin user
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
            id: data.id,
            username: data.username,
            email: data.email,
            role: data.role
        }));
        
        // Đăng nhập thành công
        showToast('Đăng nhập thành công!', 'success');
        
        // Chuyển hướng dựa trên role
        setTimeout(() => {
            if (data.role === 'ADMIN') {
                navigateTo('admin');
            } else {
                navigateTo('index');
            }
        }, 1000);
        
    } catch (error) {
        showAlert('login-alert', error.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại.', 'danger');
    }
}

// ============================================
// LOGOUT FUNCTION
// ============================================

/**
 * Xử lý đăng xuất
 */
async function handleLogout() {
    try {
        // Optional: Gọi API logout để invalidate token ở server
        // await apiPost('/auth/logout', {});
        
        // Xóa token và user data khỏi localStorage
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        
        showToast('Đã đăng xuất thành công!', 'success');
        
        // Chuyển về trang login
        setTimeout(() => {
            navigateTo('login');
        }, 1000);
        
    } catch (error) {
        console.error('Logout error:', error);
        // Vẫn xóa token ngay cả khi API lỗi
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigateTo('login');
    }
}

// ============================================
// GET CURRENT USER
// ============================================

/**
 * Lấy thông tin user hiện tại từ server
 * Dùng để verify token còn hợp lệ không
 */
async function fetchCurrentUser() {
    try {
        // Backend trả về User object trực tiếp (sau unwrap)
        const user = await apiGet('/auth/me');

        // Cập nhật thông tin user mới nhất
        localStorage.setItem('user', JSON.stringify(user));

        return user;
    } catch (error) {
        // Token hết hạn hoặc không hợp lệ
        console.error('Fetch user error:', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        return null;
    }
}

// ============================================
// ROLE-BASED ACCESS CONTROL
// ============================================

/**
 * Kiểm tra user hiện tại có phải USER không
 * @returns {boolean} true nếu là USER
 */
function isUser() {
    const user = getUser();
    return user && user.role === 'USER';
}

/**
 * Lấy role của user hiện tại
 * @returns {string|null} Role của user hoặc null
 */
function getCurrentUserRole() {
    const user = getUser();
    return user ? user.role : null;
}

/**
 * Điều hướng dựa trên role
 */
function navigateBasedOnRole() {
    const role = getCurrentUserRole();
    if (role === 'ADMIN') {
        navigateTo('admin');
    } else if (role === 'USER') {
        navigateTo('index');
    } else {
        navigateTo('login');
    }
}

/**
 * Ẩn/hiện UI dựa trên role
 * - Ẩn các element dành riêng cho ADMIN nếu user không phải ADMIN
 * - Ẩn các element dành riêng cho USER nếu user không phải USER
 */
function updateUIByRole() {
    const role = getCurrentUserRole();

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

// ============================================
// OAUTH2 GOOGLE LOGIN HANDLING
// ============================================

/**
 * Xử lý callback từ Google OAuth2
 * Sau khi đăng nhập thành công, Google sẽ redirect về frontend với:
 * - token: JWT token
 * - userId: User ID
 * - username: Tên người dùng
 * - email: Email người dùng
 * - role: Role (USER hoặc ADMIN)
 */
function handleOAuth2Callback() {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const userId = urlParams.get('userId');
    const username = urlParams.get('username');
    const email = urlParams.get('email');
    const role = urlParams.get('role');

    if (token && userId && username && email && role) {
        // Lưu token và thông tin user
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify({
            id: userId,
            username: username,
            email: email,
            role: role
        }));

        // Xóa các query parameters khỏi URL
        window.history.replaceState({}, document.title, window.location.pathname);

        // Đăng nhập thành công
        showToast('Đăng nhập bằng Google thành công!', 'success');

        // Chuyển hướng dựa trên role
        setTimeout(() => {
            if (role === 'ADMIN') {
                navigateTo('admin');
            } else {
                navigateTo('index');
            }
        }, 1000);

        return true;
    }

    return false;
}

// ============================================
// INITIALIZATION
// ============================================

// Khởi tạo khi DOM ready
document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra OAuth2 callback trước tiên
    handleOAuth2Callback();
    // Gắn sự kiện cho form đăng ký
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
    
    // Gắn sự kiện cho form đăng nhập
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    // Gắn sự kiện cho nút đăng xuất
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
    
    // Nếu đã đăng nhập, fetch thông tin user mới nhất
    if (isAuthenticated()) {
        fetchCurrentUser();
    }
});
