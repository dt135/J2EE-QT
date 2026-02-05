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
        
        // Lưu dữ liệu - backend trả về { id, userId, items, totalAmount, itemCount }
        cartItems = data.items || [];
        cartTotal = data.totalAmount || 0;
        
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
    
    // Tạo HTML cho từng item
    const itemsHTML = cartItems.map(item => createCartItemHTML(item)).join('');
    
    container.innerHTML = itemsHTML;
    
    // Hiển thị tổng tiền
    if (summaryContainer) {
        summaryContainer.style.display = 'block';
        updateCartTotal();
    }
    
    // Gắn sự kiện cho các nút
    attachCartEvents();
}

/**
 * Tạo HTML cho một cart item
 * @param {Object} item - Item trong giỏ
 * @returns {string} HTML string
 */
function createCartItemHTML(item) {
    const book = item.book || item;
    const quantity = item.quantity || 1;
    const itemTotal = book.price * quantity;
    
    return `
        <div class="cart-item" data-item-id="${book._id}">
            <div class="cart-item-image">
                <i class="fas fa-book"></i>
            </div>
            <div class="cart-item-details">
                <h5 class="cart-item-title">${escapeHtml(book.title)}</h5>
                <p class="cart-item-author">${escapeHtml(book.author)}</p>
                <p class="text-muted">${escapeHtml(book.category)}</p>
            </div>
            <div class="cart-item-price">
                ${formatCurrency(book.price)}
            </div>
            <div class="quantity-control">
                <button class="btn-quantity-decrease" data-book-id="${book._id}">
                    <i class="fas fa-minus"></i>
                </button>
                <span class="quantity-value">${quantity}</span>
                <button class="btn-quantity-increase" data-book-id="${book._id}">
                    <i class="fas fa-plus"></i>
                </button>
            </div>
            <div class="cart-item-total">
                ${formatCurrency(itemTotal)}
            </div>
            <button class="cart-item-remove" data-book-id="${book._id}" title="Xóa khỏi giỏ">
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
    // Nút tăng số lượng
    document.querySelectorAll('.btn-quantity-increase').forEach(btn => {
        btn.addEventListener('click', function() {
            const bookId = this.getAttribute('data-book-id');
            updateQuantity(bookId, 1);
        });
    });
    
    // Nút giảm số lượng
    document.querySelectorAll('.btn-quantity-decrease').forEach(btn => {
        btn.addEventListener('click', function() {
            const bookId = this.getAttribute('data-book-id');
            updateQuantity(bookId, -1);
        });
    });
    
    // Nút xóa
    document.querySelectorAll('.cart-item-remove').forEach(btn => {
        btn.addEventListener('click', function() {
            const bookId = this.getAttribute('data-book-id');
            removeFromCart(bookId);
        });
    });
    
    // Nút thanh toán
    const checkoutBtn = document.getElementById('checkout-btn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', handleCheckout);
    }
}

/**
 * Cập nhật số lượng sách trong giỏ
 * @param {string} bookId - ID sách
 * @param {number} change - Thay đổi số lượng (+1 hoặc -1)
 */
async function updateQuantity(bookId, change) {
    try {
        // Tìm item hiện tại
        const item = cartItems.find(i => {
            const book = i.book || i;
            return book._id === bookId;
        });
        
        if (!item) return;
        
        const currentQuantity = item.quantity || 1;
        const newQuantity = currentQuantity + change;
        
        // Nếu số lượng <= 0, xóa khỏi giỏ
        if (newQuantity <= 0) {
            await removeFromCart(bookId);
            return;
        }
        
        showLoading(true);
        
        // Gọi API cập nhật
        await apiPut('/cart/update', {
            bookId: bookId,
            quantity: newQuantity
        });
        
        // Cập nhật local data
        item.quantity = newQuantity;
        
        // Cập nhật UI
        displayCart();
        
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
    // Xác nhận trước khi xóa
    if (!confirm('Bạn có chắc muốn xóa sách này khỏi giỏ hàng?')) {
        return;
    }
    
    try {
        showLoading(true);
        
        // Gọi API xóa - backend nhận DELETE /cart/remove với body { bookId }
        await apiCall('/cart/remove', {
            method: 'DELETE',
            body: JSON.stringify({ bookId: bookId })
        });
        
        // Cập nhật local data
        cartItems = cartItems.filter(item => {
            const book = item.book || item;
            return book._id !== bookId;
        });
        
        // Cập nhật UI
        displayCart();
        
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
