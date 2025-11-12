document.addEventListener('DOMContentLoaded', function() {
    const paymentBtn = document.getElementById('paymentBtn');
    if (paymentBtn) {
        paymentBtn.addEventListener('click', completePayment);
    }
});

async function completePayment() {
    // 1. Lấy bookingCode từ biến toàn cục (đã khai báo bên HTML)
    const bookingCode = window.CURRENT_BOOKING_CODE;

    if (!bookingCode) {
        alert("Lỗi: Không tìm thấy mã đơn hàng. Vui lòng quay lại trang chủ.");
        return;
    }

    const button = document.getElementById('paymentBtn');
    const resultDiv = document.getElementById('result');

    // 2. Hiệu ứng Loading
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý thanh toán...';

    if (resultDiv) {
        resultDiv.style.display = 'block';
        resultDiv.className = 'result';
        resultDiv.innerHTML = '<div class="loading">Đang kết nối tới cổng thanh toán...</div>';
    }

    try {
        // 3. GỌI API MỚI (Sửa endpoint tại đây)
        // Endpoint: /api/payment/complete/{code}?paymentMethod=...
        const response = await fetch(`/api/payment/complete/${bookingCode}?paymentMethod=QR_PAY_DEMO`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();

        if (response.ok && result.success) {
            // --- THANH TOÁN THÀNH CÔNG ---
            if (resultDiv) {
                resultDiv.className = 'result success';
                resultDiv.innerHTML = `
                    <h3>✅ Thanh toán thành công!</h3>
                    <p><strong>${result.message}</strong></p>
                    <div class="info-row">
                        <span class="info-label">Mã vé:</span>
                        <span class="info-value">${result.bookingCode}</span>
                    </div>
                    <div class="ticket-list" style="margin-top:15px;">
                        <p><em>🎉 Vé của bạn đã được kích hoạt. Hệ thống sẽ chuyển hướng sau 3 giây...</em></p>
                    </div>
                    <button class="home-btn" id="redirectBtn" style="margin-top: 15px; padding: 10px 20px; background-color: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer;">
                        Xem vé ngay
                    </button>
                `;

                // Gắn sự kiện cho nút mới tạo
                setTimeout(() => {
                    const redirectBtn = document.getElementById('redirectBtn');
                    if(redirectBtn) {
                        redirectBtn.addEventListener('click', () => window.location.href = '/booking/history');
                    }
                }, 100);
            }

            button.style.display = 'none';

            // Tự động chuyển hướng sau 3 giây
            setTimeout(() => {
                window.location.href = '/booking/history';
            }, 3000);

        } else {
            // --- LỖI TỪ SERVER TRẢ VỀ ---
            throw new Error(result.error || 'Giao dịch thất bại');
        }

    } catch (error) {
        // --- LỖI KẾT NỐI / CODING ---
        console.error(error);
        if (resultDiv) {
            resultDiv.className = 'result error';
            resultDiv.innerHTML = `
                <h3>❌ Thanh toán thất bại</h3>
                <p>${error.message}</p>
                <button id="retryBtn" style="margin-top: 10px; padding: 8px 16px; background-color: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer;">
                    Thử lại
                </button>
            `;

            setTimeout(() => {
                const retryBtn = document.getElementById('retryBtn');
                if(retryBtn) retryBtn.addEventListener('click', retryPayment);
            }, 100);
        }

        button.disabled = false;
        button.innerHTML = '💳 Hoàn tất thanh toán';
    }
}

function retryPayment() {
    const resultDiv = document.getElementById('result');
    if (resultDiv) resultDiv.style.display = 'none';
    // Reset nút bấm nếu cần
    const button = document.getElementById('paymentBtn');
    if (button) {
        button.disabled = false;
        button.innerHTML = '💳 Hoàn tất thanh toán';
    }
}