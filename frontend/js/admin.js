/**
 * ============================================
 * ADMIN MODULE
 * ============================================
 * File này xử lý các chức năng quản lý (dành cho Admin):
 * - Hiển thị danh sách sách
 * - Thêm sách mới
 * - Sửa thông tin sách
 * - Xóa sách
 * - Quản lý category
 * - Quản lý người dùng
 * 
 * API Endpoints:
 * GET    /books          - Lấy danh sách sách
 * POST   /books          - Thêm sách mới
 * PUT    /books/:id      - Cập nhật sách
 * DELETE /books/:id      - Xóa sách
 * GET    /categories     - Lấy danh sách category
 * GET    /users          - Lấy danh sách user
 * PUT    /users/:id/role - Cập nhật role user
 * PUT    /users/:id/status - Khoá/Mở khoá user
 * DELETE /users/:id      - Xóa user
 */

// ============================================
// STATE MANAGEMENT
// ============================================

let adminBooks = [];       // Danh sách sách trong admin
let editingBookId = null;  // ID sách đang chỉnh sửa
let editingCategoryId = null; // ID category đang chỉnh sửa
let categories = [];       // Danh sách category
let users = [];            // Danh sách người dùng
let currentTab = 'books';  // Tab hiện tại: books, categories, users

// ============================================
// INITIALIZATION
// ============================================

/**
 * Khởi tạo trang admin
 */
async function initAdmin() {
    // Kiểm tra quyền admin
    if (!requireAdmin()) {
        return;
    }
    
    // Tải dữ liệu
    await Promise.all([
        fetchAdminBooks(),
        fetchCategories(),
        fetchUsers()
    ]);
    
    // Cập nhật thống kê
    await updateStats();
    
    // Hiển thị tab mặc định
    switchTab('books');
}

// ============================================
// FETCH DATA
// ============================================

/**
 * Lấy danh sách sách cho admin
 */
async function fetchAdminBooks() {
    try {
        showLoading(true);
        
        const data = await apiGet('/books');
        // Backend trả về PageResponse: { content, page, limit, total, totalPages }
        adminBooks = data.content || [];
        
        displayAdminBooks();
        
    } catch (error) {
        console.error('Error fetching books:', error);
        showToast('Không thể tải danh sách sách', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Lấy danh sách category
 */
async function fetchCategories() {
    try {
        const data = await apiGet('/categories');
        categories = data.content || data.categories || data || [];
        
        // Cập nhật dropdown category
        updateCategoryDropdowns();
        
        // Hiển thị danh sách category trong bảng
        displayCategoryList();
        
    } catch (error) {
        console.error('Error fetching categories:', error);
        // Sử dụng category mặc định nếu API lỗi
        categories = ['Fiction', 'Non-Fiction', 'Science', 'Technology', 'History'];
        updateCategoryDropdowns();
        displayCategoryList();
    }
}

/**
 * Lấy danh sách user
 */
async function fetchUsers() {
    try {
        const data = await apiGet('/users');
        users = data.content || data || [];
        
        displayUsers();
        
    } catch (error) {
        console.error('Error fetching users:', error);
        showToast('Không thể tải danh sách người dùng', 'error');
    }
}

// ============================================
// DISPLAY FUNCTIONS
// ============================================

/**
 * Hiển thị danh sách sách trong bảng admin
 */
function displayAdminBooks() {
    const tbody = document.getElementById('admin-books-table-body');
    if (!tbody) return;
    
    if (adminBooks.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <p class="text-muted mb-0">Chưa có sách nào</p>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = adminBooks.map((book, index) => `
        <tr>
            <td>${index + 1}</td>
            <td>
                <strong>${escapeHtml(book.title)}</strong>
            </td>
            <td>${escapeHtml(book.author)}</td>
            <td>
                <span class="badge bg-secondary">${escapeHtml(book.categoryName || 'Chưa phân loại')}</span>
            </td>
            <td>${formatCurrency(book.price)}</td>
            <td>
                <button class="btn btn-sm btn-primary btn-action" onclick="editBook('${book._id}')">
                    <i class="fas fa-edit"></i> Sửa
                </button>
                <button class="btn btn-sm btn-danger btn-action" onclick="deleteBook('${book._id}')">
                    <i class="fas fa-trash"></i> Xóa
                </button>
            </td>
        </tr>
    `).join('');
}

/**
 * Cập nhật dropdown category
 */
function updateCategoryDropdowns() {
    const selects = document.querySelectorAll('.category-select');
    const options = categories.map(cat => {
        // cat có thể là object {id, name} hoặc string (dữ liệu cũ)
        const catId = cat.id || cat;
        const catName = cat.name || cat;
        return `<option value="${escapeHtml(catId)}">${escapeHtml(catName)}</option>`;
    }).join('');

    selects.forEach(select => {
        const currentValue = select.value;
        select.innerHTML = '<option value="">Chọn thể loại</option>' + options;
        if (currentValue) {
            select.value = currentValue;
        }
    });
}

/**
 * Cập nhật thống kê
 */
async function updateStats() {
    const stats = {
        books: adminBooks.length,
        categories: categories.length,
        users: users.length,
        // Có thể thêm các stats khác từ API
    };
    
    // Cập nhật UI
    Object.keys(stats).forEach(key => {
        const el = document.getElementById(`stat-${key}`);
        if (el) {
            el.textContent = stats[key];
        }
    });
    
    // Lấy thống kê từ API
    try {
        const userStats = await apiGet('/users/stats');
        if (userStats) {
            const adminCountEl = document.getElementById('stat-admins');
            const activeUsersEl = document.getElementById('stat-active-users');
            
            if (adminCountEl) {
                adminCountEl.textContent = userStats.adminCount || 0;
            }
            if (activeUsersEl) {
                activeUsersEl.textContent = userStats.activeCount || 0;
            }
        }
    } catch (error) {
        console.error('Error fetching user stats:', error);
    }
}

// ============================================
// CRUD OPERATIONS
// ============================================

/**
 * Mở modal thêm sách mới
 */
function openAddBookModal() {
    editingBookId = null;
    document.getElementById('book-form-title').textContent = 'Thêm sách mới';
    document.getElementById('book-form').reset();
    
    const modal = new bootstrap.Modal(document.getElementById('book-modal'));
    modal.show();
}

/**
 * Mở modal sửa sách
 * @param {string} bookId - ID sách cần sửa
 */
async function editBook(bookId) {
    const book = adminBooks.find(b => b._id === bookId);
    if (!book) {
        showToast('Không tìm thấy sách', 'error');
        return;
    }

    editingBookId = bookId;
    document.getElementById('book-form-title').textContent = 'Chỉnh sửa sách';

    // Điền thông tin vào form
    document.getElementById('book-title').value = book.title;
    document.getElementById('book-author').value = book.author;
    document.getElementById('book-category').value = book.categoryId || '';
    document.getElementById('book-price').value = book.price;
    document.getElementById('book-description').value = book.description || '';

    const modal = new bootstrap.Modal(document.getElementById('book-modal'));
    modal.show();
}

/**
 * Xử lý submit form (Thêm/Sửa sách)
 * @param {Event} event - Form submit event
 */
async function handleBookFormSubmit(event) {
    event.preventDefault();
    
    // Lấy dữ liệu từ form
    const bookData = {
        title: document.getElementById('book-title').value.trim(),
        author: document.getElementById('book-author').value.trim(),
        categoryId: document.getElementById('book-category').value,
        price: parseFloat(document.getElementById('book-price').value),
        description: document.getElementById('book-description').value.trim()
    };

    // Validate
    if (!validateBookForm(bookData)) {
        return;
    }
    
    try {
        showLoading(true);
        
        if (editingBookId) {
            // Cập nhật sách
            await apiPut(`/books/${editingBookId}`, bookData);
            showToast('Cập nhật sách thành công!', 'success');
        } else {
            // Thêm sách mới
            await apiPost('/books', bookData);
            showToast('Thêm sách mới thành công!', 'success');
        }
        
        // Đóng modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('book-modal'));
        modal.hide();
        
        // Tải lại danh sách
        await fetchAdminBooks();
        updateStats();
        
    } catch (error) {
        console.error('Error saving book:', error);
        showToast(error.message || 'Không thể lưu sách', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Validate form sách
 * @param {Object} data - Dữ liệu sách
 * @returns {boolean} true nếu hợp lệ
 */
function validateBookForm(data) {
    if (!data.title || data.title.length < 2) {
        showToast('Tiêu đề sách phải có ít nhất 2 ký tự', 'error');
        return false;
    }

    if (!data.author || data.author.length < 2) {
        showToast('Tên tác giả phải có ít nhất 2 ký tự', 'error');
        return false;
    }

    if (!data.categoryId) {
        showToast('Vui lòng chọn thể loại', 'error');
        return false;
    }

    if (!data.price || data.price <= 0) {
        showToast('Giá sách phải lớn hơn 0', 'error');
        return false;
    }

    return true;
}

/**
 * Xóa sách
 * @param {string} bookId - ID sách cần xóa
 */
async function deleteBook(bookId) {
    const book = adminBooks.find(b => b._id === bookId);
    if (!book) return;
    
    // Xác nhận xóa
    if (!confirm(`Bạn có chắc muốn xóa sách "${book.title}"?`)) {
        return;
    }
    
    try {
        showLoading(true);
        
        // Gọi API xóa
        await apiDelete(`/books/${bookId}`);
        
        showToast('Xóa sách thành công!', 'success');
        
        // Tải lại danh sách
        await fetchAdminBooks();
        updateStats();
        
    } catch (error) {
        console.error('Error deleting book:', error);
        showToast(error.message || 'Không thể xóa sách', 'error');
    } finally {
        showLoading(false);
    }
}

// ============================================
// CATEGORY MANAGEMENT
// ============================================

/**
 * Mở modal quản lý category
 */
function openCategoryModal() {
    editingCategoryId = null;
    
    // Reset form
    const input = document.getElementById('new-category');
    if (input) {
        input.value = '';
    }
    
    // Reset title và button
    const formTitle = document.querySelector('#category-modal .modal-title');
    const addBtn = document.getElementById('btn-add-category');
    if (formTitle) {
        formTitle.textContent = 'Quản lý thể loại';
    }
    if (addBtn) {
        addBtn.innerHTML = '<i class="fas fa-plus"></i> Thêm';
        addBtn.onclick = saveCategory;
    }
    
    displayCategoryList();
    const modal = new bootstrap.Modal(document.getElementById('category-modal'));
    modal.show();
}

/**
 * Hiển thị danh sách category
 */
function displayCategoryList() {
    const container = document.getElementById('category-list');
    const tableBody = document.getElementById('admin-categories-table-body');
    
    // Hiển thị trong modal (nếu có)
    if (container) {
        if (categories.length === 0) {
            container.innerHTML = '<p class="text-muted">Chưa có category nào</p>';
            return;
        }
        
        container.innerHTML = categories.map(cat => `
            <div class="d-flex justify-content-between align-items-center mb-2 p-2 border rounded">
                <span>${escapeHtml(cat.name || cat)}</span>
                <button class="btn btn-sm btn-danger" onclick="deleteCategory('${cat.id || cat}')">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `).join('');
    }
    
    // Hiển thị trong bảng tab categories
    if (tableBody) {
        if (categories.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="text-center py-4">
                        <p class="text-muted mb-0">Chưa có category nào</p>
                    </td>
                </tr>
            `;
            return;
        }
        
        tableBody.innerHTML = categories.map((cat, index) => {
            // Đếm số sách thuộc category này
            const bookCount = adminBooks.filter(b => b.categoryId === cat.id || b.category === cat.name).length;
            
            return `
                <tr>
                    <td>${index + 1}</td>
                    <td>
                        <strong>${escapeHtml(cat.name)}</strong>
                    </td>
                    <td>
                        <span class="badge bg-secondary">${bookCount} sách</span>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-warning btn-action" onclick="editCategory('${cat.id}')" title="Sửa">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-danger btn-action" onclick="deleteCategoryById('${cat.id}')" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    }
}

/**
 * Lưu category (thêm mới hoặc sửa)
 */
async function saveCategory() {
    const input = document.getElementById('new-category');
    const categoryName = input.value.trim();
    
    if (!categoryName) {
        showToast('Vui lòng nhập tên category', 'error');
        return;
    }
    
    // Kiểm tra trùng tên (trừ khi đang sửa chính nó)
    const exists = categories.some(cat => {
        if (editingCategoryId && cat.id === editingCategoryId) {
            return cat.name === categoryName;
        }
        return cat.name === categoryName || cat === categoryName;
    });
    
    if (exists) {
        showToast('Category này đã tồn tại', 'error');
        return;
    }
    
    try {
        showLoading(true);
        
        if (editingCategoryId) {
            // Sửa category
            await apiPut(`/categories/${editingCategoryId}`, { name: categoryName });
            showToast('Cập nhật category thành công!', 'success');
        } else {
            // Thêm mới category
            await apiPost('/categories', { name: categoryName });
            showToast('Thêm category thành công!', 'success');
        }
        
        // Reset
        editingCategoryId = null;
        
        // Tải lại danh sách
        await fetchCategories();
        
        input.value = '';
        
        // Đóng modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('category-modal'));
        modal?.hide();
        
    } catch (error) {
        showToast(error.message || 'Không thể lưu category', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Xóa category
 * @param {string} category - Tên category
 */
async function deleteCategory(category) {
    if (!confirm(`Bạn có chắc muốn xóa category "${category}"?`)) {
        return;
    }
    
    try {
        // Kiểm tra xem có sách nào thuộc category này không
        const booksInCategory = adminBooks.filter(b => b.categoryId === category || b.category === category);
        
        if (booksInCategory.length > 0) {
            if (!confirm(`Category "${category}" có ${booksInCategory.length} sách. Bạn có chắc muốn xóa?`)) {
                return;
            }
        }
        
        // Gọi API xóa category
        await apiDelete(`/categories/${category}`);
        
        // Xóa khỏi local
        categories = categories.filter(c => c !== category);
        
        // Cập nhật UI
        displayCategoryList();
        updateCategoryDropdowns();
        
        showToast('Xóa category thành công!', 'success');
        
    } catch (error) {
        showToast(error.message || 'Không thể xóa category', 'error');
    }
}

/**
 * Xóa category theo ID
 * @param {string} categoryId - ID của category
 */
async function deleteCategoryById(categoryId) {
    const category = categories.find(c => c.id === categoryId);
    if (!category) return;
    
    if (!confirm(`Bạn có chắc muốn xóa category "${category.name}"?`)) {
        return;
    }
    
    try {
        // Kiểm tra xem có sách nào thuộc category này không
        const booksInCategory = adminBooks.filter(b => b.categoryId === categoryId || b.category === category.name);
        
        if (booksInCategory.length > 0) {
            if (!confirm(`Category "${category.name}" có ${booksInCategory.length} sách. Bạn có chắc muốn xóa?`)) {
                return;
            }
        }
        
        // Gọi API xóa category
        await apiDelete(`/categories/${categoryId}`);
        
        // Xóa khỏi local
        categories = categories.filter(c => c.id !== categoryId);
        
        // Cập nhật UI
        displayCategoryList();
        updateCategoryDropdowns();
        
        showToast('Xóa category thành công!', 'success');
        
    } catch (error) {
        showToast(error.message || 'Không thể xóa category', 'error');
    }
}

/**
 * Edit category
 * @param {string} categoryId - ID của category
 */
async function editCategory(categoryId) {
    const category = categories.find(c => c.id === categoryId);
    if (!category) {
        showToast('Không tìm thấy category', 'error');
        return;
    }
    
    editingCategoryId = categoryId;
    
    // Mở modal category
    openCategoryModal();
    
    // Điền thông tin vào form
    const input = document.getElementById('new-category');
    if (input) {
        input.value = category.name;
    }
    
    // Đổi placeholder và nút button để hiển thị đang edit
    const formTitle = document.querySelector('#category-modal .modal-title');
    const addBtn = document.getElementById('btn-add-category');
    if (formTitle) {
        formTitle.textContent = 'Chỉnh sửa danh mục';
    }
    if (addBtn) {
        addBtn.innerHTML = '<i class="fas fa-save"></i> Lưu thay đổi';
        addBtn.onclick = saveCategory;
    }
}

/**
 * Lưu category (thêm mới hoặc sửa)
 */
async function saveCategory() {
    const input = document.getElementById('new-category');
    const categoryName = input.value.trim();
    
    if (!categoryName) {
        showToast('Vui lòng nhập tên category', 'error');
        return;
    }
    
    try {
        showLoading(true);
        
        if (editingCategoryId) {
            // Sửa category
            await apiPut(`/categories/${editingCategoryId}`, { name: categoryName });
            showToast('Cập nhật category thành công!', 'success');
        } else {
            // Thêm mới category
            await apiPost('/categories', { name: categoryName });
            showToast('Thêm category thành công!', 'success');
        }
        
        // Reset
        editingCategoryId = null;
        
        // Tải lại danh sách
        await fetchCategories();
        
        input.value = '';
        
        // Đóng modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('category-modal'));
        modal?.hide();
        
    } catch (error) {
        showToast(error.message || 'Không thể lưu category', 'error');
    } finally {
        showLoading(false);
    }
}

// ============================================
// USER MANAGEMENT
// ============================================

/**
 * Hiển thị danh sách user trong bảng
 */
function displayUsers() {
    const tbody = document.getElementById('admin-users-table-body');
    if (!tbody) return;
    
    if (users.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <p class="text-muted mb-0">Chưa có người dùng nào</p>
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = users.map((user, index) => `
        <tr>
            <td>${index + 1}</td>
            <td>
                <strong>${escapeHtml(user.username)}</strong>
            </td>
            <td>${escapeHtml(user.email)}</td>
            <td>
                <span class="badge ${user.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'}">
                    ${escapeHtml(user.role)}
                </span>
            </td>
            <td>
                <span class="badge ${user.enabled ? 'bg-success' : 'bg-secondary'}">
                    ${user.enabled ? 'Hoạt động' : 'Đã khoá'}
                </span>
            </td>
            <td>
                <button class="btn btn-sm btn-warning btn-action" onclick="toggleUserRole('${user.id}')" title="Đổi role">
                    <i class="fas fa-user-shield"></i>
                </button>
                <button class="btn btn-sm ${user.enabled ? 'btn-secondary' : 'btn-success'} btn-action" 
                        onclick="toggleUserStatus('${user.id}')" 
                        title="${user.enabled ? 'Khoá' : 'Mở khoá'}">
                    <i class="fas ${user.enabled ? 'fa-lock' : 'fa-unlock'}"></i>
                </button>
                <button class="btn btn-sm btn-danger btn-action" onclick="deleteUser('${user.id}')" title="Xóa">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

/**
 * Đổi role của user
 * @param {string} userId - ID của user
 */
async function toggleUserRole(userId) {
    const user = users.find(u => u.id === userId);
    if (!user) return;
    
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    
    if (!confirm(`Bạn có chắc muốn đổi role của user "${user.username}" từ ${user.role} sang ${newRole}?`)) {
        return;
    }
    
    try {
        showLoading(true);
        
        await apiPut(`/users/${userId}/role`, { role: newRole });
        
        showToast(`Đã đổi role thành ${newRole}!`, 'success');
        
        // Tải lại danh sách
        await fetchUsers();
        await updateStats();
        
    } catch (error) {
        console.error('Error updating user role:', error);
        showToast(error.message || 'Không thể cập nhật role', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Khoá/Mở khoá user
 * @param {string} userId - ID của user
 */
async function toggleUserStatus(userId) {
    const user = users.find(u => u.id === userId);
    if (!user) return;
    
    const newStatus = !user.enabled;
    const action = newStatus ? 'mở khoá' : 'khoá';
    
    if (!confirm(`Bạn có chắc muốn ${action} user "${user.username}"?`)) {
        return;
    }
    
    try {
        showLoading(true);
        
        await apiPut(`/users/${userId}/status`, { enabled: newStatus });
        
        showToast(`Đã ${action} user thành công!`, 'success');
        
        // Tải lại danh sách
        await fetchUsers();
        await updateStats();
        
    } catch (error) {
        console.error('Error toggling user status:', error);
        showToast(error.message || 'Không thể thay đổi trạng thái user', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Xóa user
 * @param {string} userId - ID của user
 */
async function deleteUser(userId) {
    const user = users.find(u => u.id === userId);
    if (!user) return;
    
    // Kiểm tra nếu user muốn xóa chính mình
    const currentUser = getUser();
    if (currentUser && currentUser.id === userId) {
        showToast('Bạn không thể xóa chính mình!', 'error');
        return;
    }
    
    if (!confirm(`Bạn có chắc muốn xóa user "${user.username}"? Hành động này không thể hoàn tác!`)) {
        return;
    }
    
    try {
        showLoading(true);
        
        await apiDelete(`/users/${userId}`);
        
        showToast('Xóa user thành công!', 'success');
        
        // Tải lại danh sách
        await fetchUsers();
        await updateStats();
        
    } catch (error) {
        console.error('Error deleting user:', error);
        showToast(error.message || 'Không thể xóa user', 'error');
    } finally {
        showLoading(false);
    }
}

// ============================================
// TAB NAVIGATION
// ============================================

/**
 * Chuyển tab
 * @param {string} tabName - Tên tab
 */
function switchTab(tabName) {
    currentTab = tabName;
    
    // Ẩn tất cả các section
    document.querySelectorAll('.admin-tab').forEach(el => {
        el.style.display = 'none';
    });
    
    // Hiển thị tab được chọn
    const tabElement = document.getElementById(`admin-${tabName}-tab`);
    if (tabElement) {
        tabElement.style.display = 'block';
    }
    
    // Cập nhật active state cho nav tabs
    document.querySelectorAll('.nav-tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    const activeBtn = document.querySelector(`[data-tab="${tabName}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }
    
    // Tải lại dữ liệu cho tab hiện tại
    if (tabName === 'users') {
        fetchUsers();
    } else if (tabName === 'categories') {
        displayCategoryList();
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

/**
 * Escape HTML để tránh XSS
 * @param {string} text - Chuỗi cần escape
 * @returns {string} Chuỗi đã escape
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ============================================
// EVENT LISTENERS
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    // Kiểm tra nếu đang ở trang admin
    if (document.getElementById('admin-books-table')) {
        initAdmin();
    }
    
    // Nút thêm sách (đã nằm trong tab Books rồi)
    const addBookBtn = document.getElementById('btn-add-book');
    if (addBookBtn) {
        addBookBtn.addEventListener('click', openAddBookModal);
    }
    
    // Form sách
    const bookForm = document.getElementById('book-form');
    if (bookForm) {
        bookForm.addEventListener('submit', handleBookFormSubmit);
    }
    
    // Nút quản lý category chính (trong tab categories)
    const manageCatBtnMain = document.getElementById('btn-manage-categories-main');
    if (manageCatBtnMain) {
        manageCatBtnMain.addEventListener('click', openCategoryModal);
    }
    
    // Nút thêm/sửa category - được gán trong openCategoryModal() và editCategory()
    
    // Tab navigation
    const tabButtons = document.querySelectorAll('.nav-tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const tabName = this.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
});
