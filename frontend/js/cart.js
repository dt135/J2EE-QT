/**
 * ============================================
 * CART MODULE
 * ============================================
 * File này xử lý các chức năng giỏ hàng:
 * - Hiển thị giỏ hàng
 * - Cập nhật số lượng
 * - Xóa sách khỏi giỏ
 * - Checkout
 * 
 * API Endpoints:
 * GET    /cart          - Lấy giỏ hàng
 * POST   /cart/add      - Thêm vào giỏ
 * PUT    /cart/update   - Cập nhật số lượng
 * DELETE /cart/remove   - Xóa khỏi giỏ (body: { bookId })
 * POST   /checkout      - Thanh toán
 */

// ============================================
// STATE MANAGEMENT
// ============================================

let cartItems = [];      // Lưu trữ items trong giỏ
let cartTotal = 0;       // Tổng tiền

// ============================================
// FETCH CART
// ============================================

/**
 * Lấy thông tin giỏ hàng từ API
 */
async function fetchCart() {
    try {
        showLoading(true);

        // Gọi API lấy giỏ hàng
        const data = await apiGet('/cart');

        console.log('Fetched cart data:', data);

        // Lưu dữ liệu - backend trả về { id, userId, items, totalAmount, itemCount }
        cartItems = data.items || [];
        cartTotal = data.totalAmount || 0;

        console.log('Parsed cartItems:', cartItems);
        console.log('Parsed cartTotal:', cartTotal);

        // Hiển thị giỏ hàng
        displayCart();

    } catch (error) {
        console.error('Error fetching cart:', error);
        showToast('Không thể tải giỏ hàng. Vui lòng thử lại!', 'error');
        displayEmptyCart();
    } finally {
        showLoading(false);
    }
}

/**
 * Hiển thị giỏ hàng lên giao diện
 */
function displayCart() {
    const container = document.getElementById('cart-items-container');
    const summaryContainer = document.getElementById('cart-summary');

    if (!container) return;

    // Kiểm tra nếu giỏ hàng trống
    if (!cartItems || cartItems.length === 0) {
        displayEmptyCart();
        return;
    }

    console.log('Displaying cart with items:', cartItems);

    // Tạo HTML cho từng item
    const itemsHTML = cartItems.map(item => createCartItemHTML(item)).join('');

    container.innerHTML = itemsHTML;

    console.log('Rendered HTML:', container.innerHTML);

    // Hiển thị tổng tiền
    if (summaryContainer) {
        summaryContainer.style.display = 'block';
        updateCartTotal();
    }

    // Gắn sự kiện cho các nút - dùng setTimeout để đảm bảo DOM được cập nhật
    setTimeout(() => {
        attachCartEvents();
    }, 0);
}

/**
 * Tạo HTML cho một cart item
 * @param {Object} item - Item trong giỏ
 * @returns {string} HTML string
 */
function createCartItemHTML(item) {
    // Log full item structure để debug
    console.log('Creating cart item HTML - Full item:', JSON.stringify(item, null, 2));

    const book = item.book;
    // Ưu tiên lấy bookId từ item.bookId (backend trả về), nếu không thì thử từ book object
    const bookId = item.bookId || (book ? (book.id || book._id) : null);
    const quantity = item.quantity || 1;
    const price = book ? book.price : 0;
    const itemTotal = price * quantity;

    console.log('Creating cart item HTML:', { item, book, bookId, quantity, price, itemTotal });

    // Nếu không có bookId, hiển thị lỗi
    if (!bookId) {
        console.error('bookId is missing for item:', item);
        return `
            <div class="cart-item" style="border-left: 4px solid var(--secondary-color);">
                <div class="cart-item-details" style="flex-grow: 1; padding: 1rem;">
                    <h5 class="cart-item-title text-danger">Lỗi: Thiếu thông tin sách</h5>
                    <p class="text-muted">Item: ${escapeHtml(JSON.stringify(item))}</p>
                </div>
            </div>
        `;
    }

    // Nếu không có book object, hiển thị thông tin đơn giản
    if (!book) {
        console.warn('Book object is missing for item:', item);
        return `
            <div class="cart-item" data-item-id="${bookId}">
                <div class="cart-item-image">
                    <i class="fas fa-book"></i>
                </div>
                <div class="cart-item-details">
                    <h5 class="cart-item-title">Sách không tồn tại</h5>
                    <p class="text-muted">ID: ${escapeHtml(bookId)}</p>
                </div>
                <div class="cart-item-price">
                    ${formatCurrency(price)}
                </div>
                <div class="quantity-control">
                    <button class="btn-quantity-decrease" data-book-id="${bookId}">
                        <i class="fas fa-minus"></i>
                    </button>
                    <span class="quantity-value">${quantity}</span>
                    <button class="btn-quantity-increase" data-book-id="${bookId}">
                        <i class="fas fa-plus"></i>
                    </button>
                </div>
                <div class="cart-item-total">
                    ${formatCurrency(itemTotal)}
                </div>
                <button class="cart-item-remove" data-book-id="${bookId}" title="Xóa khỏi giỏ">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;
    }

    return `
        <div class="cart-item" data-item-id="${bookId}">
            <div class="cart-item-image">
                <i class="fas fa-book"></i>
            </div>
            <div class="cart-item-details">
                <h5 class="cart-item-title">${escapeHtml(book.title)}</h5>
                <p class="cart-item-author">${escapeHtml(book.author)}</p>
                <p class="text-muted">${escapeHtml(book.categoryName || book.category)}</p>
            </div>
            <div class="cart-item-price">
                ${formatCurrency(price)}
            </div>
            <div class="quantity-control">
                <button class="btn-quantity-decrease" data-book-id="${bookId}">
                    <i class="fas fa-minus"></i>
                </button>
                <span class="quantity-value">${quantity}</span>
                <button class="btn-quantity-increase" data-book-id="${bookId}">
                    <i class="fas fa-plus"></i>
                </button>
            </div>
            <div class="cart-item-total">
                ${formatCurrency(itemTotal)}
            </div>
            <button class="cart-item-remove" data-book-id="${bookId}" title="Xóa khỏi giỏ">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;
}

/**
 * Hiển thị trạng thái giỏ hàng trống
 */
function displayEmptyCart() {
    const container = document.getElementById('cart-items-container');
    const summaryContainer = document.getElementById('cart-summary');
    
    if (container) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-shopping-cart"></i>
                <h3>Giỏ hàng trống</h3>
                <p>Bạn chưa thêm sách nào vào giỏ hàng.</p>
                <a href="./index.html" class="btn btn-primary mt-3">
                    <i class="fas fa-book"></i> Tiếp tục mua sắm
                </a>
            </div>
        `;
    }
    
    if (summaryContainer) {
        summaryContainer.style.display = 'none';
    }
}

/**
 * Cập nhật tổng tiền hiển thị
 */
function updateCartTotal() {
    // Tính lại tổng tiền
    cartTotal = cartItems.reduce((total, item) => {
        const book = item.book || item;
        const quantity = item.quantity || 1;
        return total + (book.price * quantity);
    }, 0);
    
    const totalEl = document.getElementById('cart-total-value');
    if (totalEl) {
        totalEl.textContent = formatCurrency(cartTotal);
    }
    
    const countEl = document.getElementById('cart-item-count');
    if (countEl) {
        countEl.textContent = cartItems.length;
    }
}

// ============================================
// CART OPERATIONS
// ============================================

/**
 * Gắn sự kiện cho các nút trong giỏ hàng
 */
function attachCartEvents() {
    console.log('Attaching cart events...');

    // Nút tăng số lượng
    document.querySelectorAll('.btn-quantity-increase').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            const bookId = this.getAttribute('data-book-id');
            console.log('Increase button clicked for bookId:', bookId);
            updateQuantity(bookId, 1);
        });
    });

    // Nút giảm số lượng
    document.querySelectorAll('.btn-quantity-decrease').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            const bookId = this.getAttribute('data-book-id');
            console.log('Decrease button clicked for bookId:', bookId);
            updateQuantity(bookId, -1);
        });
    });

    // Nút xóa
    document.querySelectorAll('.cart-item-remove').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            const bookId = this.getAttribute('data-book-id');
            console.log('Remove button clicked for bookId:', bookId);
            removeFromCart(bookId);
        });
    });

    // Nút thanh toán
    const checkoutBtn = document.getElementById('checkout-btn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', handleCheckout);
    }

    console.log('Cart events attached. Found',
        document.querySelectorAll('.btn-quantity-increase').length, 'increase buttons,',
        document.querySelectorAll('.btn-quantity-decrease').length, 'decrease buttons');
}

/**
 * Cập nhật số lượng sách trong giỏ
 * @param {string} bookId - ID sách
 * @param {number} change - Thay đổi số lượng (+1 hoặc -1)
 */
async function updateQuantity(bookId, change) {
    console.log('updateQuantity called with bookId:', bookId, 'change:', change);
    console.log('Current cartItems:', cartItems);

    try {
        // Tìm item hiện tại - ưu tiên item.bookId, sau đó thử book.id và book._id
        const item = cartItems.find(i => {
            return i.bookId === bookId ||
                   (i.book && i.book.id === bookId) ||
                   (i.book && i.book._id === bookId);
        });

        console.log('Found item:', item);

        if (!item) {
            console.error('Item not found for bookId:', bookId);
            showToast('Không tìm thấy sản phẩm', 'error');
            return;
        }

        const currentQuantity = item.quantity || 1;
        const newQuantity = currentQuantity + change;

        console.log('Current quantity:', currentQuantity, 'New quantity:', newQuantity);

        // Nếu số lượng <= 0, xóa khỏi giỏ
        if (newQuantity <= 0) {
            await removeFromCart(bookId);
            return;
        }

        showLoading(true);

        // Gọi API cập nhật
        const response = await apiPut('/cart/update', {
            bookId: bookId,
            quantity: newQuantity
        });

        console.log('API response:', response);

        // Lấy lại giỏ hàng từ backend để đồng bộ
        await fetchCart();

        showToast('Đã cập nhật số lượng', 'success');

    } catch (error) {
        console.error('Error updating quantity:', error);
        showToast(error.message || 'Không thể cập nhật số lượng', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Xóa sách khỏi giỏ hàng
 * @param {string} bookId - ID sách cần xóa
 */
async function removeFromCart(bookId) {
    console.log('removeFromCart called with bookId:', bookId);

    // Tìm item để xác nhận tên sách
    const item = cartItems.find(i => {
        return i.bookId === bookId ||
               (i.book && i.book.id === bookId) ||
               (i.book && i.book._id === bookId);
    });

    // Xác nhận trước khi xóa
    const bookTitle = item && item.book ? item.book.title : 'sách này';
    if (!confirm(`Bạn có chắc muốn xóa "${bookTitle}" khỏi giỏ hàng?`)) {
        return;
    }

    try {
        showLoading(true);

        // Gọi API xóa - backend nhận DELETE /cart/remove với body { bookId }
        const response = await apiCall('/cart/remove', {
            method: 'DELETE',
            body: JSON.stringify({ bookId: bookId })
        });

        console.log('Remove API response:', response);

        // Lấy lại giỏ hàng từ backend để đồng bộ
        await fetchCart();

        showToast('Đã xóa sách khỏi giỏ hàng', 'success');

    } catch (error) {
        console.error('Error removing from cart:', error);
        showToast(error.message || 'Không thể xóa sách', 'error');
    } finally {
        showLoading(false);
    }
}

// ============================================
// CHECKOUT
// ============================================

/**
 * Xử lý thanh toán
 */
async function handleCheckout() {
    // Kiểm tra giỏ hàng có trống không
    if (!cartItems || cartItems.length === 0) {
        showToast('Giỏ hàng trống!', 'warning');
        return;
    }
    
    // Xác nhận thanh toán
    if (!confirm(`Xác nhận thanh toán ${formatCurrency(cartTotal)}?`)) {
        return;
    }
    
    try {
        showLoading(true);
        
        // Gọi API checkout
        const data = await apiPost('/checkout', {
            items: cartItems,
            total: cartTotal
        });
        
        // Thanh toán thành công
        showToast('Thanh toán thành công! Cảm ơn bạn đã mua hàng.', 'success');
        
        // Xóa giỏ hàng local
        cartItems = [];
        cartTotal = 0;
        
        // Hiển thị trang thành công
        displayCheckoutSuccess(data.order);
        
    } catch (error) {
        console.error('Checkout error:', error);
        showToast(error.message || 'Thanh toán thất bại. Vui lòng thử lại.', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Hiển thị trang thanh toán thành công
 * @param {Object} order - Thông tin đơn hàng
 */
function displayCheckoutSuccess(order) {
    const container = document.getElementById('cart-container');
    if (container) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-check-circle" style="color: var(--success-color);"></i>
                <h3>Đặt hàng thành công!</h3>
                <p>Mã đơn hàng: <strong>#${order?._id || 'N/A'}</strong></p>
                <p>Tổng tiền: <strong>${formatCurrency(cartTotal)}</strong></p>
                <a href="./index.html" class="btn btn-primary mt-3">
                    <i class="fas fa-book"></i> Tiếp tục mua sắm
                </a>
            </div>
        `;
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
    // Tải giỏ hàng nếu đang ở trang cart
    if (document.getElementById('cart-items-container')) {
        // Kiểm tra đăng nhập
        if (!requireAuth()) {
            return;
        }
        
        fetchCart();
    }
});
