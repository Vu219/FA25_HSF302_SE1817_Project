// ==========================================
// 1. KHỞI TẠO DỮ LIỆU & BIẾN TOÀN CỤC
// ==========================================

// Lấy dữ liệu từ window (được định nghĩa bên file HTML)
const scheduleId = (typeof window.SERVER_SCHEDULE_ID !== 'undefined') ? window.SERVER_SCHEDULE_ID : 0;
const seatIdsParam = (typeof window.SERVER_SEAT_IDS_STR !== 'undefined') ? window.SERVER_SEAT_IDS_STR : '';

// Xử lý chuỗi ghế thành mảng số
let seatIdsArray = [];
if (seatIdsParam && seatIdsParam.trim() !== '') {
    seatIdsArray = seatIdsParam.split(',')
        .map(id => id.trim())
        .filter(id => id !== '')
        .map(id => parseInt(id))
        .filter(id => !isNaN(id));
}

// Đếm số ghế thực tế
const selectedSeatsCount = seatIdsArray.length;

console.log('DEBUG JS START:', { scheduleId, seatIdsParam, seatIdsArray, selectedSeatsCount });

// Biến lưu giá ghế
const seatPrices = {};

// Hệ số giảm giá
const discountRates = {
    'ADULT': 1.0,
    'CHILD': 0.8,
    'ELDERLY': 0.8
};

// ==========================================
// 2. CÁC HÀM HỖ TRỢ (HELPER)
// ==========================================

function goBack() {
    if (scheduleId > 0) {
        window.location.href = `/booking?scheduleId=${scheduleId}`;
    } else {
        window.history.back();
    }
}

function showError(message) {
    const errorDiv = document.getElementById('error-message');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        errorDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });

        const successDiv = document.getElementById('success-message');
        if (successDiv) successDiv.style.display = 'none';
    } else {
        alert(message);
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function getTicketTypeLabel(ticketType) {
    const labels = { 'ADULT': 'Người lớn', 'CHILD': 'Trẻ em', 'ELDERLY': 'Người cao tuổi' };
    return labels[ticketType] || ticketType;
}

// Hàm ẩn/hiện ô nhập CCCD cho Trẻ em
function toggleIdCardInput(selectElement) {
    const ticketType = selectElement.value;
    let idCardInput;

    if (selectElement.id === 'contactTicketType') {
        idCardInput = document.getElementById('identityCard');
    } else {
        const index = selectElement.id.split('_')[1];
        idCardInput = document.getElementById(`passengerIdCard_${index}`);
    }

    if (idCardInput) {
        if (ticketType === 'CHILD') {
            idCardInput.value = '';
            idCardInput.disabled = true;
            idCardInput.placeholder = 'Trẻ em không cần nhập';
            idCardInput.style.backgroundColor = '#e9ecef';
            idCardInput.style.border = '1px solid #ced4da';
        } else {
            idCardInput.disabled = false;
            idCardInput.placeholder = '001234567890';
            idCardInput.style.backgroundColor = '#fff';
        }
    }
}

// ==========================================
// 3. HÀM TÍNH TOÁN GIÁ
// ==========================================

function calculateAndDisplayPrices() {
    if (Object.keys(seatPrices).length === 0) return;

    let totalOriginal = 0;
    let totalFinal = 0;
    const priceDetails = [];

    const ticketTypeSelects = document.querySelectorAll('.passenger-tickettype');

    ticketTypeSelects.forEach((select, index) => {
        if (index >= seatIdsArray.length) return;

        const seatId = seatIdsArray[index];
        const ticketType = select.value;
        const discountRate = discountRates[ticketType] || 1;
        const seatPrice = seatPrices[seatId] || 0;
        const finalPrice = seatPrice * discountRate;

        totalOriginal += seatPrice;
        totalFinal += finalPrice;

        let passengerName;
        if (seatIdsArray.length === 1) {
            passengerName = 'Người liên hệ';
        } else {
            passengerName = index === 0 ? 'Người liên hệ' : `Hành khách ${index}`;
        }

        priceDetails.push({
            passenger: passengerName,
            seatPrice: seatPrice,
            ticketType: ticketType,
            discount: (1 - discountRate) * 100,
            finalPrice: finalPrice
        });
    });

    displayTotalPrice(totalOriginal, totalFinal, priceDetails);
}

function displayTotalPrice(originalTotal, finalTotal, details) {
    let priceContainer = document.getElementById('price-summary-container');

    if (!priceContainer) {
        priceContainer = document.createElement('div');
        priceContainer.id = 'price-summary-container';
        priceContainer.className = 'price-display';
        priceContainer.style.cssText = 'margin-top: 20px; padding: 15px; background: #e7f3ff; border: 1px solid #b3d9ff; border-radius: 4px;';

        const actionButtons = document.querySelector('.action-buttons');
        if (actionButtons) actionButtons.parentNode.insertBefore(priceContainer, actionButtons);
    }

    const discountAmount = originalTotal - finalTotal;

    priceContainer.innerHTML = `
        <h4 style="margin-top: 0; color: #333;"><i class="fas fa-receipt"></i> Tổng thanh toán</h4>
        <div style="font-size: 14px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                <span>Tổng giá gốc:</span> <span>${formatCurrency(originalTotal)}</span>
            </div>
            ${discountAmount > 0 ? `<div style="display: flex; justify-content: space-between; margin-bottom: 5px; color: #28a745;"><span>Giảm giá:</span><span>-${formatCurrency(discountAmount)}</span></div>` : ''}
            <div style="display: flex; justify-content: space-between; border-top: 1px solid #ccc; padding-top: 8px; font-weight: bold; font-size: 16px; color: #e74c3c;">
                <span>Tổng thanh toán:</span> <span>${formatCurrency(finalTotal)}</span>
            </div>
        </div>
        ${details.length > 0 ? `<div style="margin-top: 15px; padding-top: 10px; border-top: 1px dashed #ccc;"><strong style="font-size: 13px;">Chi tiết:</strong>${details.map(d => `<div style="display: flex; justify-content: space-between; margin-top: 5px; font-size: 13px;"><span>${d.passenger} (${getTicketTypeLabel(d.ticketType)})</span><span>${formatCurrency(d.finalPrice)}</span></div>`).join('')}</div>` : ''}
    `;
}

// ==========================================
// 4. LOAD DỮ LIỆU TỪ API
// ==========================================

async function loadSeatPrices() {
    try {
        if (seatIdsArray.length === 0) {
            console.warn("Không có seatIds để load giá.");
            return;
        }
        console.log('Calling API seat-prices...');
        const response = await fetch(`/api/booking/seat-prices?seatIds=${seatIdsParam}&scheduleId=${scheduleId}`);

        if (response.ok) {
            const prices = await response.json();
            console.log('API Prices:', prices);
            Object.assign(seatPrices, prices);
            calculateAndDisplayPrices();
        } else {
            console.warn('API failed, dùng giá mặc định.');
            useFallbackPrices();
        }
    } catch (error) {
        console.error('Error loading prices:', error);
        useFallbackPrices();
    }
}

function useFallbackPrices() {
    seatIdsArray.forEach(seatId => {
        seatPrices[seatId] = 150000;
    });
    calculateAndDisplayPrices();
}

// ==========================================
// 5. VALIDATION (QUAN TRỌNG ĐÃ SỬA LỖI BIẾN)
// ==========================================

function validatePassengerForm() {
    console.log("--- Bắt đầu Validate ---");

    // 1. CHECK NGƯỜI LIÊN HỆ
    const contactName = document.getElementById('fullName').value.trim();
    const contactEmail = document.getElementById('email').value.trim();
    const contactPhone = document.getElementById('phone').value.trim();

    // --- SỬA LỖI REFERENCE ERROR TẠI ĐÂY ---
    // Đảm bảo biến tên là contactType khớp với logic if bên dưới
    const contactType = document.getElementById('contactTicketType').value;
    const contactId = document.getElementById('identityCard').value.trim();

    console.log(`Người liên hệ: Type=${contactType}, ID=${contactId}`);

    if (!contactName) { showError('Vui lòng nhập họ tên người liên hệ'); return false; }
    if (!contactEmail) { showError('Vui lòng nhập email'); return false; }
    if (!contactPhone) { showError('Vui lòng nhập số điện thoại'); return false; }

    // Logic: Nếu KHÔNG PHẢI TRẺ EM thì bắt buộc nhập CCCD
    if (contactType !== 'CHILD' && !contactId) {
        showError('Vui lòng nhập CMND/CCCD cho người liên hệ');
        document.getElementById('identityCard').focus();
        return false;
    }

    // 2. CHECK HÀNH KHÁCH ĐI CÙNG
    if (seatIdsArray.length > 1) {
        for (let i = 1; i < seatIdsArray.length; i++) {
            const nameInput = document.getElementById(`passengerFullName_${i}`);
            const typeInput = document.getElementById(`passengerTicketType_${i}`);
            const idInput = document.getElementById(`passengerIdCard_${i}`);

            if (!nameInput || !typeInput) continue;

            if (!nameInput.value.trim()) {
                showError(`Thiếu tên hành khách thứ ${i + 1}`);
                nameInput.focus();
                return false;
            }

            const typeValue = typeInput.value;
            const idValue = idInput ? idInput.value.trim() : '';

            console.log(`Khách ${i}: Type=${typeValue}, ID=${idValue}`);

            // Logic: Nếu KHÔNG PHẢI TRẺ EM thì bắt buộc nhập CCCD
            if (typeValue !== 'CHILD' && !idValue) {
                showError(`Hành khách thứ ${i + 1} cần nhập CMND (hoặc chọn vé Trẻ em)`);
                if(idInput) idInput.focus();
                return false;
            }
        }
    }

    return true;
}

// ==========================================
// 6. XỬ LÝ SUBMIT FORM
// ==========================================

const form = document.getElementById('passengerForm');
if (form) {
    form.addEventListener('submit', async function (e) {
        e.preventDefault(); // Chặn reload trang

        // --- GỌI HÀM VALIDATE ---
        if (!validatePassengerForm()) {
            return; // Dừng nếu validate sai
        }

        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';

        try {
            const formData = new FormData(e.target);
            const passengers = [];

            // 1. Người liên hệ
            passengers.push({
                fullName: document.getElementById('fullName').value.trim(),
                idCard: document.getElementById('identityCard').value.trim(), // Có thể rỗng nếu là CHILD
                ticketType: document.getElementById('contactTicketType').value
            });

            // 2. Hành khách đi cùng
            for (let i = 1; i < seatIdsArray.length; i++) {
                const fullNameInput = document.getElementById(`passengerFullName_${i}`);
                const idCardInput = document.getElementById(`passengerIdCard_${i}`);
                const ticketTypeInput = document.getElementById(`passengerTicketType_${i}`);

                if (fullNameInput && ticketTypeInput) {
                    passengers.push({
                        fullName: fullNameInput.value.trim(),
                        idCard: idCardInput ? idCardInput.value.trim() : '',
                        ticketType: ticketTypeInput.value
                    });
                }
            }

            const bookingRequest = {
                scheduleId: scheduleId,
                selectedSeatIds: seatIdsArray,
                userFullName: passengers[0].fullName,
                userEmail: formData.get('email'),
                userPhone: formData.get('phone'),
                passengers: passengers,
                notes: formData.get('notes') || null
            };

            console.log('Submitting:', bookingRequest);

            // Gọi API đặt vé
            // LƯU Ý: Đảm bảo endpoint này đúng với Controller của bạn
            const response = await fetch('/api/booking/create-pending', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bookingRequest)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Đặt vé thất bại');
            }

            const booking = await response.json();
            // Chuyển hướng thanh toán
            window.location.href = `/payment?bookingCode=${booking.bookingCode}`;

        } catch (error) {
            console.error('Booking error:', error);
            showError(error.message || 'Có lỗi xảy ra. Vui lòng thử lại.');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-check"></i> Xác nhận và thanh toán';
        }
    });
}

// ==========================================
// 7. INITIALIZATION
// ==========================================

document.addEventListener('DOMContentLoaded', function () {
    // Gắn sự kiện cho các ô chọn loại vé
    const ticketSelects = document.querySelectorAll('.passenger-tickettype');
    ticketSelects.forEach(select => {
        select.addEventListener('change', function() {
            toggleIdCardInput(this);
            calculateAndDisplayPrices();
        });
        // Chạy lần đầu
        toggleIdCardInput(select);
    });

    loadSeatPrices();
});