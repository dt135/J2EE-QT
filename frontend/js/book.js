/**
 * ============================================
 * BOOK MODULE
 * ============================================
 * File này xử lý các chức năng liên quan đến sách:
 * - Hiển thị danh sách sách
 * - Tìm kiếm sách
 * - Lọc sách theo giá
 * - Thêm sách vào giỏ hàng
 * 
 * API Endpoints:
 * GET    /books       - Lấy danh sách sách
 * GET    /books/:id   - Lấy chi tiết sách
 * GET    /books/search?keyword=xxx - Tìm kiếm sách
 * POST   /cart/add    - Thêm vào giỏ hàng
 */

// ============================================
// STATE MANAGEMENT
// ============================================

let allBooks = [];           // Lưu trữ tất cả sách
let allCategories = [];      // Lưu trữ danh sách thể loại
let currentFilteredBooks = []; // Lưu trữ kết quả lọc hiện tại
let currentPage = 1;         // Trang hiện tại
const booksPerPage = 12;     // Số sách mỗi trang

// ============================================
// FETCH BOOKS
// ============================================

/**
 * Lấy danh sách sách từ API
 * Hiển thị loading và xử lý lỗi
 */
async function fetchBooks() {
    try {
        showLoading(true);

        // Gọi API lấy danh sách sách
        const data = await apiGet('/books');

        // Backend trả về PageResponse: { content, page, limit, total, totalPages }
        allBooks = data.content || [];

        console.log('Fetched books:', allBooks);
        console.log('First book:', allBooks[0]);

        // Hiển thị sách
        displayBooks(allBooks);

        // Cập nhật số lượng
        updateBookCount(allBooks.length);

    } catch (error) {
        console.error('Error fetching books:', error);
        showToast('Không thể tải danh sách sách. Vui lòng thử lại!', 'error');
        displayEmptyState();
    } finally {
        showLoading(false);
    }
}

/**
 * Refresh dữ liệu và reset tất cả bộ lọc
 */
async function refreshBooks() {
    try {
        showLoading(true);

        // Reset các bộ lọc
        currentFilteredBooks = [];
        const searchInput = document.getElementById('search-input');
        const categoryFilter = document.getElementById('category-filter');
        const sortSelect = document.getElementById('sort-select');

        if (searchInput) searchInput.value = '';
        if (categoryFilter) categoryFilter.value = 'all';
        if (sortSelect) sortSelect.value = '';

        // Gọi API lấy lại dữ liệu
        const data = await apiGet('/books');
        allBooks = data.content || [];

        console.log('Refreshed books:', allBooks);

        // Hiển thị sách
        displayBooks(allBooks);

        // Cập nhật số lượng
        updateBookCount(allBooks.length);

        // Lấy lại danh sách thể loại
        await fetchCategories();

        showToast('Đã làm mới danh sách sách!', 'success');

    } catch (error) {
        console.error('Error refreshing books:', error);
        showToast('Không thể làm mới danh sách sách!', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Lấy danh sách thể loại từ API
 */
async function fetchCategories() {
    try {
        const data = await apiGet('/categories?limit=100');
        allCategories = data.content || [];

        // Cập nhật dropdown category
        updateCategoryDropdown(allCategories);
    } catch (error) {
        console.error('Error fetching categories:', error);
        showToast('Không thể tải danh sách thể loại!', 'error');
    }
}

/**
 * Cập nhật dropdown category với dữ liệu từ backend
 */
function updateCategoryDropdown(categories) {
    console.log('Categories from backend:', categories);

    const categoryFilter = document.getElementById('category-filter');
    if (!categoryFilter) return;

    // Giữ lại option đầu tiên "Tất cả thể loại"
    categoryFilter.innerHTML = '<option value="all">Tất cả thể loại</option>';

    // Thêm các category từ backend
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id || category._id; // Hỗ trợ cả id và _id
        option.textContent = category.name;
        categoryFilter.appendChild(option);
        console.log('Added category option:', category.id || category._id, category.name);
    });
}

/**
 * Hiển thị danh sách sách lên giao diện
 * @param {Array} books - Mảng các đối tượng sách
 */
function displayBooks(books) {
    const container = document.getElementById('books-container');

    if (!container) return;

    // Kiểm tra nếu không có sách
    if (!books || books.length === 0) {
        displayEmptyState();
        return;
    }

    // Tạo HTML cho từng sách
    const booksHTML = books.map(book => createBookCard(book)).join('');

    container.innerHTML = `
        <div class="row g-4">
            ${booksHTML}
        </div>
    `;

    // Gắn sự kiện cho các nút "Thêm vào giỏ"
    attachAddToCartEvents();

    // Cập nhật UI dựa trên role (ẩn các nút dành riêng cho USER nếu là ADMIN)
    updateUIByRole();
}

/**
 * Tạo HTML cho một book card
 * @param {Object} book - Đối tượng sách
 * @returns {string} HTML string
 */
function createBookCard(book) {
    // Lấy tên category từ categoryName field
    const categoryName = book.categoryName || 'Chưa phân loại';

    return `
        <div class="col-lg-3 col-md-4 col-sm-6">
            <div class="book-card fade-in">
                <div class="book-image">
                    <i class="fas fa-book"></i>
                </div>
                <div class="book-content">
                    <h5 class="book-title" title="${escapeHtml(book.title)}">${escapeHtml(book.title)}</h5>
                    <p class="book-author">
                        <i class="fas fa-user"></i>
                        ${escapeHtml(book.author)}
                    </p>
                    <span class="book-category">${escapeHtml(categoryName)}</span>
                    <p class="book-price">${formatCurrency(book.price)}</p>
                    <div class="book-actions">
                        <button class="btn btn-primary btn-add-cart" data-book-id="${book.id}" data-user-only>
                            <i class="fas fa-cart-plus"></i> Thêm vào giỏ
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
}

/**
 * Hiển thị trạng thái rỗng khi không có sách
 */
function displayEmptyState() {
    const container = document.getElementById('books-container');
    if (container) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-search"></i>
                <h3>Không tìm thấy sách</h3>
                <p>Vui lòng thử tìm kiếm với từ khóa khác hoặc điều chỉnh bộ lọc.</p>
            </div>
        `;
    }
}

/**
 * Cập nhật số lượng sách hiển thị
 * @param {number} count - Số lượng sách
 */
function updateBookCount(count) {
    const countEl = document.getElementById('book-count');
    if (countEl) {
        countEl.textContent = `Hiển thị ${count} cuốn sách`;
    }
}

// ============================================
// SEARCH & FILTER
// ============================================

/**
 * Tìm kiếm sách theo từ khóa (gọi API backend)
 * @param {string} keyword - Từ khóa tìm kiếm
 */
async function searchBooks(keyword) {
    if (!keyword.trim()) {
        // Reset về trạng thái ban đầu
        currentFilteredBooks = [];

        // Áp dụng sắp xếp hiện tại
        const sortSelect = document.getElementById('sort-select');
        if (sortSelect) {
            sortBooks(sortSelect.value);
        } else {
            displayBooks(allBooks);
            updateBookCount(allBooks.length);
        }
        return;
    }

    try {
        showLoading(true);

        // Gọi API search từ backend
        const data = await apiGet(`/books/search?keyword=${encodeURIComponent(keyword)}`);

        // Backend trả về: { keyword, count, books }
        const filteredBooks = data.books || [];
        currentFilteredBooks = filteredBooks;

        // Áp dụng sắp xếp hiện tại
        const sortSelect = document.getElementById('sort-select');
        if (sortSelect) {
            sortBooks(sortSelect.value);
        } else {
            displayBooks(currentFilteredBooks);
            updateBookCount(currentFilteredBooks.length);
        }

    } catch (error) {
        console.error('Error searching books:', error);
        showToast('Không thể tìm kiếm sách!', 'error');
        // Fallback: filter client-side
        const lowerKeyword = keyword.toLowerCase();
        const filteredBooks = allBooks.filter(book => {
            return (
                book.title.toLowerCase().includes(lowerKeyword) ||
                book.author.toLowerCase().includes(lowerKeyword)
            );
        });
        currentFilteredBooks = filteredBooks;

        // Áp dụng sắp xếp hiện tại
        const sortSelect = document.getElementById('sort-select');
        if (sortSelect) {
            sortBooks(sortSelect.value);
        } else {
            displayBooks(currentFilteredBooks);
            updateBookCount(currentFilteredBooks.length);
        }
    } finally {
        showLoading(false);
    }
}

/**
 * Lọc sách theo category
 * @param {string} categoryId - ID của category
 */
function filterByCategory(categoryId) {
    console.log('Filtering by categoryId:', categoryId);

    if (!categoryId || categoryId === 'all') {
        // Reset về trạng thái ban đầu
        currentFilteredBooks = [];
    } else {
        // Lọc sách theo category ID
        const filteredBooks = allBooks.filter(book => {
            return book.categoryId === categoryId;
        });

        console.log('Filtered books:', filteredBooks);
        currentFilteredBooks = filteredBooks;
    }

    // Áp dụng sắp xếp hiện tại (nếu có)
    const sortSelect = document.getElementById('sort-select');
    if (sortSelect) {
        sortBooks(sortSelect.value);
    } else {
        // Không có sort select: hiển thị danh sách hiện tại
        const booksToDisplay = currentFilteredBooks.length > 0 ? currentFilteredBooks : allBooks;
        displayBooks(booksToDisplay);
        updateBookCount(booksToDisplay.length);
    }
}

/**
 * Sắp xếp sách
 * @param {string} sortBy - Tiêu chí sắp xếp: 'price-asc', 'price-desc', 'name-asc', 'name-desc'
 */
function sortBooks(sortBy) {
    // Xác định danh sách cần sort: danh sách đã lọc hoặc toàn bộ danh sách
    let booksToSort = currentFilteredBooks.length > 0 ? [...currentFilteredBooks] : [...allBooks];

    // Nếu chọn "Mặc định"
    if (!sortBy) {
        if (currentFilteredBooks.length > 0) {
            // Có filter: hiển thị danh sách đã lọc không sort
            displayBooks(currentFilteredBooks);
            updateBookCount(currentFilteredBooks.length);
        } else {
            // Không có filter: hiển thị toàn bộ sách
            displayBooks(allBooks);
            updateBookCount(allBooks.length);
        }
        return;
    }

    // Thực hiện sắp xếp
    switch (sortBy) {
        case 'price-asc':
            booksToSort.sort((a, b) => a.price - b.price);
            break;
        case 'price-desc':
            booksToSort.sort((a, b) => b.price - a.price);
            break;
        case 'name-asc':
            booksToSort.sort((a, b) => a.title.localeCompare(b.title));
            break;
        case 'name-desc':
            booksToSort.sort((a, b) => b.title.localeCompare(a.title));
            break;
    }

    // Update currentFilteredBooks với kết quả đã sort
    currentFilteredBooks = booksToSort;

    displayBooks(currentFilteredBooks);
    updateBookCount(currentFilteredBooks.length);
}

// ============================================
// ADD TO CART
// ============================================

/**
 * Gắn sự kiện cho các nút "Thêm vào giỏ"
 */
function attachAddToCartEvents() {
    const buttons = document.querySelectorAll('.btn-add-cart');
    buttons.forEach(button => {
        button.addEventListener('click', function() {
            const bookId = this.getAttribute('data-book-id');
            addToCart(bookId);
        });
    });
}

/**
 * Lấy ID của book (xử lý cả id và _id)
 * @param {Object} book - Đối tượng sách
 * @returns {string} ID của sách
 */
function getBookId(book) {
    return book.id || book._id;
}

/**
 * Thêm sách vào giỏ hàng
 * @param {string} bookId - ID của sách
 */
async function addToCart(bookId) {
    // Kiểm tra đã đăng nhập chưa
    if (!isAuthenticated()) {
        showToast('Vui lòng đăng nhập để thêm vào giỏ hàng', 'warning');
        setTimeout(() => {
            navigateTo('login');
        }, 1500);
        return;
    }
    
    try {
        // Gọi API thêm vào giỏ
        await apiPost('/cart/add', {
            bookId: bookId,
            quantity: 1
        });
        
        showToast('Đã thêm sách vào giỏ hàng!', 'success');
        
        // Cập nhật số lượng giỏ hàng (nếu có UI hiển thị)
        updateCartBadge();
        
    } catch (error) {
        console.error('Error adding to cart:', error);
        showToast(error.message || 'Không thể thêm vào giỏ hàng', 'error');
    }
}

/**
 * Cập nhật badge số lượng giỏ hàng
 */
async function updateCartBadge() {
    try {
        const data = await apiGet('/cart');
        const cartCount = data.items ? data.items.length : 0;
        
        const badge = document.getElementById('cart-badge');
        if (badge) {
            badge.textContent = cartCount;
            badge.style.display = cartCount > 0 ? 'inline' : 'none';
        }
    } catch (error) {
        console.error('Error fetching cart:', error);
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
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    // Tải danh sách sách nếu đang ở trang index
    if (document.getElementById('books-container')) {
        fetchBooks();
        // Tải danh sách thể loại
        fetchCategories();
    }

    // Gắn sự kiện nút refresh
    const btnRefresh = document.getElementById('btn-refresh');
    if (btnRefresh) {
        btnRefresh.addEventListener('click', refreshBooks);
    }

    // Gắn sự kiện tìm kiếm khi gõ (debounce)
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                searchBooks(this.value);
            }, 300);
        });
    }

    // Gắn sự kiện sắp xếp
    const sortSelect = document.getElementById('sort-select');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            sortBooks(this.value);
        });
    }

    // Gắn sự kiện lọc theo category
    const categoryFilter = document.getElementById('category-filter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', function() {
            filterByCategory(this.value);
        });
    }

    // Cập nhật badge giỏ hàng nếu đã đăng nhập
    if (isAuthenticated()) {
        updateCartBadge();
    }
});
