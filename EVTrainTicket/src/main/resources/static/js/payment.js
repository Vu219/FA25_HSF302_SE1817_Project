// Payment Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const paymentBtn = document.getElementById('paymentBtn');
    if (paymentBtn) {
        paymentBtn.addEventListener('click', completePayment);
    }
});

async function completePayment() {
    const button = document.getElementById('paymentBtn');
    const resultDiv = document.getElementById('result');

    button.disabled = true;
    button.innerHTML = '⏳ Đang xử lý thanh toán...';

    resultDiv.style.display = 'block';
    resultDiv.className = 'result';
    resultDiv.innerHTML = '<div class="loading">Đang xử lý thanh toán của bạn...</div>';

    try {
        const response = await fetch('/api/payment/complete-session', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();

        if (response.ok && result.success) {
            resultDiv.className = 'result success';
            resultDiv.innerHTML = `
                <h3>✅ Thanh toán thành công!</h3>
                <p><strong>${result.message}</strong></p>
                <div class="info-row">
                    <span class="info-label">Mã vé:</span>
                    <span class="info-value">${result.bookingCode}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Trạng thái:</span>
                    <span class="info-value">${result.bookingStatus}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Vé đã kích hoạt:</span>
                    <span class="info-value">${result.ticketCount}</span>
                </div>
                <div class="ticket-list">
                    <h4>🎫 Vé của bạn:</h4>
                    ${result.tickets.map(ticket => `
                        <div class="ticket-item">
                            <strong>Mã vé:</strong> ${ticket.ticketCode}<br>
                            <strong>Ghế:</strong> ${ticket.seatNumber}<br>
                            <strong>Trạng thái:</strong> ${ticket.status}<br>
                            <strong>Giá:</strong> ${ticket.price.toLocaleString('vi-VN')} VNĐ
                        </div>
                    `).join('')}
                </div>
                <p><em>🎉 Vé của bạn đã được kích hoạt. Chúc bạn chuyến đi vui vẻ!</em></p>
                <button class="home-btn" style="margin-top: 15px; padding: 10px 20px; background-color: var(--secondary-color); color: white; border: none; border-radius: 4px; cursor: pointer;">
                    Đặt chuyến khác
                </button>
            `;

            requestAnimationFrame(() => {
                const homeBtn = resultDiv.querySelector('.home-btn');
                if (homeBtn) {
                    homeBtn.addEventListener('click', function() {
                        window.location.href = '/home';
                    });
                }
            });

            button.style.display = 'none';
        } else {
            resultDiv.className = 'result error';
            resultDiv.innerHTML = `
                <h3>❌ Thanh toán thất bại</h3>
                <p>${result.error}</p>
                <button class="retry-btn" style="margin-top: 10px; padding: 8px 16px; background-color: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer;">
                    Thử lại
                </button>
            `;

            requestAnimationFrame(() => {
                const retryBtn = resultDiv.querySelector('.retry-btn');
                if (retryBtn) {
                    retryBtn.addEventListener('click', retryPayment);
                }
            });

            button.disabled = false;
            button.innerHTML = '💳 Hoàn tất thanh toán';
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.innerHTML = `
            <h3>❌ Lỗi</h3>
            <p>Đã xảy ra lỗi khi xử lý: ${error.message}</p>
            <button class="retry-btn" style="margin-top: 10px; padding: 8px 16px; background-color: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer;">
                Thử lại
            </button>
        `;

        requestAnimationFrame(() => {
            const retryBtn = resultDiv.querySelector('.retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', retryPayment);
            }
        });

        button.disabled = false;
        button.innerHTML = '💳 Hoàn tất thanh toán';
    }
}

function retryPayment() {
    const resultDiv = document.getElementById('result');
    resultDiv.style.display = 'none';
}

