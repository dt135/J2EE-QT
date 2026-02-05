/**
 * ============================================
 * ORDER MODULE
 * ============================================
 * File này xử lý các chức năng đơn hàng:
 * - Hiển thị lịch sử đơn hàng
 * - Xem chi tiết đơn hàng
 * - Phân trang
 *
 * API Endpoints:
 * GET    /orders/history     - Lấy danh sách đơn hàng
 * GET    /orders/:id         - Xem chi tiết đơn hàng
 */

// ============================================
// STATE MANAGEMENT
// ============================================

let currentPage = 0;
let itemsPerPage = 10;
let totalPages = 0;
let orders = [];

// ============================================
// FETCH ORDERS
// ============================================

/**
 * Lấy danh sách đơn hàng từ API
 * @param {number} page - Số trang
 * @param {number} limit - Số lượng mỗi trang
 */
async function fetchOrders(page = 0, limit = 10) {
    try {
        showLoading(true);

        // Gọi API lấy danh sách đơn hàng
        const data = await apiGet(`/orders/history?page=${page}&limit=${limit}`);

        console.log('Fetched orders data:', data);

        // Lưu dữ liệu
        orders = data.orders || [];
        currentPage = data.page || 0;
        itemsPerPage = data.limit || 10;
        totalPages = data.totalPages || 0;

        // Hiển thị danh sách đơn hàng
        displayOrders();

        // Hiển thị phân trang
        displayPagination();

    } catch (error) {
        console.error('Error fetching orders:', error);
        showToast('Không thể tải danh sách đơn hàng. Vui lòng thử lại!', 'error');
        displayEmptyOrders();
    } finally {
        showLoading(false);
    }
}

/**
 * Hiển thị danh sách đơn hàng
 */
function displayOrders() {
    const container = document.getElementById('orders-container');

    if (!container) return;

    // Kiểm tra nếu không có đơn hàng
    if (!orders || orders.length === 0) {
        displayEmptyOrders();
        return;
    }

    // Tạo HTML cho từng đơn hàng
    const ordersHTML = orders.map(order => createOrderHTML(order)).join('');

    container.innerHTML = ordersHTML;

    // Gắn sự kiện cho các nút xem chi tiết
    attachOrderEvents();
}

/**
 * Tạo HTML cho một đơn hàng
 * @param {Object} order - Đơn hàng
 * @returns {string} HTML string
 */
function createOrderHTML(order) {
    const orderNumber = order.orderNumber || `#${order.orderId}`;
    const createdAt = formatDate(order.createdAt);
    const totalAmount = formatCurrency(order.totalAmount);
    const itemCount = order.itemCount || 0;
    const status = order.status || 'PENDING';

    // Xác định màu badge và text hiển thị
    const statusConfig = {
        'PENDING': { class: 'bg-warning', text: 'Chờ xử lý' },
        'CONFIRMED': { class: 'bg-info', text: 'Đã xác nhận' },
        'COMPLETED': { class: 'bg-success', text: 'Hoàn thành' },
        'CANCELLED': { class: 'bg-danger', text: 'Đã hủy' }
    };

    const statusInfo = statusConfig[status] || statusConfig['PENDING'];

    // Kiểm tra có thể đánh dấu "Đã nhận hàng" không
    // Chỉ cho phép khi trạng thái là PENDING hoặc CONFIRMED
    const canMarkReceived = status === 'PENDING' || status === 'CONFIRMED';

    // Tạo nút hành động
    let actionButtons = '';
    if (canMarkReceived) {
        actionButtons = `
            <button class="btn btn-received" data-order-id="${order.orderId}">
                <i class="fas fa-check-circle"></i> Đã nhận được hàng
            </button>
        `;
    }
    actionButtons += `
        <button class="btn btn-view-detail" data-order-id="${order.orderId}">
            <i class="fas fa-eye"></i> Xem chi tiết
        </button>
    `;

    return `
        <div class="order-card">
            <div class="order-header">
                <div class="order-info">
                    <h5 class="order-number">${escapeHtml(orderNumber)}</h5>
                    <p class="order-date">
                        <i class="far fa-calendar-alt"></i> ${escapeHtml(createdAt)}
                    </p>
                </div>
                <div class="order-summary">
                    <div class="order-status">
                        <span class="badge ${statusInfo.class}">${escapeHtml(statusInfo.text)}</span>
                    </div>
                    <div class="order-amount">
                        <span class="amount-label">Tổng:</span>
                        <span class="amount-value">${escapeHtml(totalAmount)}</span>
                    </div>
                </div>
            </div>
            <div class="order-body">
                <div class="order-items-preview">
                    <i class="fas fa-book"></i>
                    <span>${itemCount} sản phẩm</span>
                </div>
            </div>
            <div class="order-footer">
                ${actionButtons}
            </div>
        </div>
    `;
}

/**
 * Hiển thị trạng thái không có đơn hàng
 */
function displayEmptyOrders() {
    const container = document.getElementById('orders-container');
    const paginationContainer = document.getElementById('pagination-container');

    if (container) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-shopping-bag"></i>
                <h3>Chưa có đơn hàng nào</h3>
                <p>Bạn chưa thực hiện đơn hàng nào. Hãy mua sách ngay nhé!</p>
                <a href="./index.html" class="btn btn-primary mt-3">
                    <i class="fas fa-book"></i> Mua sắm ngay
                </a>
            </div>
        `;
    }

    if (paginationContainer) {
        paginationContainer.style.display = 'none';
    }
}

/**
 * Hiển thị phân trang
 */
function displayPagination() {
    const paginationContainer = document.getElementById('pagination-container');
    const pagination = document.getElementById('pagination');

    if (!pagination || totalPages <= 1) {
        if (paginationContainer) {
            paginationContainer.style.display = 'none';
        }
        return;
    }

    // Hiển thị container phân trang
    if (paginationContainer) {
        paginationContainer.style.display = 'block';
    }

    let paginationHTML = '';

    // Nút Previous
    paginationHTML += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">
                <i class="fas fa-chevron-left"></i>
            </a>
        </li>
    `;

    // Các trang
    for (let i = 0; i < totalPages; i++) {
        const isActive = i === currentPage ? 'active' : '';
        paginationHTML += `
            <li class="page-item ${isActive}">
                <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
            </li>
        `;
    }

    // Nút Next
    paginationHTML += `
        <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">
                <i class="fas fa-chevron-right"></i>
            </a>
        </li>
    `;

    pagination.innerHTML = paginationHTML;

    // Gắn sự kiện cho các nút phân trang
    document.querySelectorAll('#pagination .page-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = parseInt(this.getAttribute('data-page'));
            if (page >= 0 && page < totalPages && page !== currentPage) {
                fetchOrders(page, itemsPerPage);
            }
        });
    });
}

// ============================================
// ORDER DETAIL
// ============================================

/**
 * Gắn sự kiện cho các nút trong danh sách đơn hàng
 */
function attachOrderEvents() {
    // Nút xem chi tiết
    document.querySelectorAll('.btn-view-detail').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const orderId = this.getAttribute('data-order-id');
            showOrderDetail(orderId);
        });
    });

    // Nút "Đã nhận được hàng"
    document.querySelectorAll('.btn-received').forEach(btn => {
        btn.addEventListener('click', async function(e) {
            e.preventDefault();
            const orderId = this.getAttribute('data-order-id');
            await markOrderAsReceived(orderId, this);
        });
    });
}

/**
 * Đánh dấu đơn hàng là đã nhận được
 * @param {string} orderId - ID đơn hàng
 * @param {HTMLElement} btnElement - Nút click
 */
async function markOrderAsReceived(orderId, btnElement) {
    if (!confirm('Bạn đã nhận được hàng? Đơn hàng sẽ được đánh dấu hoàn thành.')) {
        return;
    }

    try {
        // Disable nút
        const originalText = btnElement.innerHTML;
        btnElement.disabled = true;
        btnElement.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';

        // Gọi API
        await apiPut(`/orders/${orderId}/received`, {});

        showToast('Đã xác nhận nhận hàng thành công! Đơn hàng đã hoàn thành.', 'success');

        // Kiểm tra nút có trong modal không
        const isModalButton = btnElement.classList.contains('btn-modal-received');

        // Tải lại danh sách đơn hàng
        await fetchOrders(currentPage, itemsPerPage);

        // Nếu là nút trong modal, đóng modal
        if (isModalButton) {
            const modal = bootstrap.Modal.getInstance(document.getElementById('orderDetailModal'));
            modal?.hide();
        }

    } catch (error) {
        console.error('Error marking order as received:', error);
        showToast(error.message || 'Không thể xác nhận nhận hàng', 'error');
    } finally {
        // Enable nút lại (nếu cần)
        btnElement.disabled = false;
        btnElement.innerHTML = originalText;
    }
}

/**
 * Hiển thị chi tiết đơn hàng
 * @param {string} orderId - ID đơn hàng
 */
async function showOrderDetail(orderId) {
    try {
        showLoading(true);

        // Gọi API lấy chi tiết đơn hàng
        const order = await apiGet(`/orders/${orderId}`);

        console.log('Fetched order detail:', order);

        // Hiển thị chi tiết
        displayOrderDetail(order);

        // Hiển thị modal
        const modal = new bootstrap.Modal(document.getElementById('orderDetailModal'));
        modal.show();

    } catch (error) {
        console.error('Error fetching order detail:', error);
        showToast(error.message || 'Không thể tải chi tiết đơn hàng', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Hiển thị chi tiết đơn hàng trong modal
 * @param {Object} order - Chi tiết đơn hàng
 */
function displayOrderDetail(order) {
    const content = document.getElementById('order-detail-content');
    const modalFooter = document.querySelector('#orderDetailModal .modal-footer');

    if (!content) return;

    const orderNumber = order.orderNumber || `#${order.orderId}`;
    const createdAt = formatDate(order.createdAt);
    const totalAmount = formatCurrency(order.totalAmount);
    const items = order.items || [];
    const status = order.status || 'PENDING';

    // Xác định màu badge và text hiển thị
    const statusConfig = {
        'PENDING': { class: 'bg-warning', text: 'Chờ xử lý' },
        'CONFIRMED': { class: 'bg-info', text: 'Đã xác nhận' },
        'COMPLETED': { class: 'bg-success', text: 'Hoàn thành' },
        'CANCELLED': { class: 'bg-danger', text: 'Đã hủy' }
    };

    const statusInfo = statusConfig[status] || statusConfig['PENDING'];

    // Tạo HTML cho các sản phẩm
    const itemsHTML = items.map(item => `
        <div class="order-detail-item">
            <div class="item-info">
                <h6 class="item-title">${escapeHtml(item.bookTitle)}</h6>
                <p class="item-author">${escapeHtml(item.bookAuthor)}</p>
            </div>
            <div class="item-details">
                <div class="item-quantity">${item.quantity} x</div>
                <div class="item-price">${formatCurrency(item.price)}</div>
                <div class="item-subtotal">${formatCurrency(item.subtotal)}</div>
            </div>
        </div>
    `).join('');

    content.innerHTML = `
        <div class="order-detail-header">
            <div class="row">
                <div class="col-md-6">
                    <h6 class="text-muted mb-1">Mã đơn hàng</h6>
                    <h5 class="mb-2">${escapeHtml(orderNumber)}</h5>
                    <p class="text-muted mb-0">
                        <i class="far fa-calendar-alt"></i> ${escapeHtml(createdAt)}
                    </p>
                </div>
                <div class="col-md-6 text-md-end">
                    <h6 class="text-muted mb-1">Trạng thái</h6>
                    <span class="badge ${statusInfo.class}">${escapeHtml(statusInfo.text)}</span>
                    <h5 class="mt-2 mb-0">${escapeHtml(totalAmount)}</h5>
                </div>
            </div>
        </div>

        <hr class="my-3">

        <div class="order-detail-items">
            <h6 class="mb-3">Sản phẩm (${items.length})</h6>
            ${itemsHTML}
        </div>

        <hr class="my-3">

        <div class="order-detail-total">
            <div class="row">
                <div class="col-md-9 text-md-end">
                    <h5 class="mb-0">Tổng cộng:</h5>
                </div>
                <div class="col-md-3 text-end">
                    <h4 class="mb-0 text-primary">${escapeHtml(totalAmount)}</h4>
                </div>
            </div>
        </div>
    `;

    // Cập nhật modal footer
    if (modalFooter) {
        // Kiểm tra có thể đánh dấu "Đã nhận hàng" không
        const canMarkReceived = status === 'PENDING' || status === 'CONFIRMED';

        let footerHTML = `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>`;

        if (canMarkReceived) {
            footerHTML = `
                <button type="button" class="btn btn-success btn-modal-received" data-order-id="${order.orderId}">
                    <i class="fas fa-check-circle"></i> Đã nhận được hàng
                </button>
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            `;
        }

        modalFooter.innerHTML = footerHTML;

        // Gắn sự kiện cho nút "Đã nhận hàng" trong modal
        const btnReceived = modalFooter.querySelector('.btn-modal-received');
        if (btnReceived) {
            btnReceived.addEventListener('click', async function() {
                const orderId = this.getAttribute('data-order-id');
                await markOrderAsReceived(orderId, this);
            });
        }
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
    // Tải đơn hàng nếu đang ở trang lịch sử đơn hàng
    if (document.getElementById('orders-container')) {
        // Kiểm tra đăng nhập
        if (!requireAuth()) {
            return;
        }

        // Kiểm tra role - chỉ USER mới được xem
        const user = getUser();
        if (user && user.role === 'ADMIN') {
            showToast('ADMIN không có chức năng xem đơn hàng', 'warning');
            navigateTo('index');
            return;
        }

        // Tải danh sách đơn hàng
        fetchOrders(currentPage, itemsPerPage);
    }
});
