// ========== KHAI BÁO BIẾN TOÀN CỤC ==========
const scheduleId = /*[[${schedule != null ? schedule.scheduleID : 0}]]*/ 0;
const seatIdsParam = /*[[${seatIds != null ? #strings.listJoin(seatIds, ',') : ''}]]*/ '';
const selectedSeatsCount = /*[[${selectedSeatsCount ?: 0}]]*/ 0;

// Chuyển seatIdsParam thành mảng
const seatIdsArray = seatIdsParam.split(',').map(id => parseInt(id.trim()));

// Object lưu giá ghế - sẽ được cập nhật từ backend
const seatPrices = {};

// Hệ số giảm giá theo loại vé
const discountRates = {
    'ADULT': 1.0,    // 100%
    'CHILD': 0.8,    // 80% (giảm 20%)
    'ELDERLY': 0.8   // 80% (giảm 20%)
};

// ========== HÀM ĐIỀU HƯỚNG ==========
function goBack() {
    window.location.href = `/booking?scheduleId=${scheduleId}`;
}

function showError(message) {
    const errorDiv = document.getElementById('error-message');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    document.getElementById('success-message').style.display = 'none';
    window.scrollTo(0, 0);
}

// ========== HÀM FORMAT TIỀN TỆ ==========
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// ========== HÀM LẤY TÊN LOẠI VÉ ==========
function getTicketTypeLabel(ticketType) {
    const labels = {
        'ADULT': 'Người lớn',
        'CHILD': 'Trẻ em',
        'ELDERLY': 'Người cao tuổi'
    };
    return labels[ticketType] || ticketType;
}

// ========== HÀM TÍNH TOÁN VÀ HIỂN THỊ GIÁ ==========
function calculateAndDisplayPrices() {
    // Nếu chưa có dữ liệu giá, không hiển thị
    if (Object.keys(seatPrices).length === 0) return;

    let totalOriginal = 0;
    let totalFinal = 0;
    const priceDetails = [];

    // Lấy tất cả các select loại vé
    const ticketTypeSelects = document.querySelectorAll('.passenger-tickettype');

    console.log('Calculating prices with:', {
        seatPrices: seatPrices,
        ticketSelectsCount: ticketTypeSelects.length,
        seatIdsCount: seatIdsArray.length
    });

    ticketTypeSelects.forEach((select, index) => {
        if (index >= seatIdsArray.length) {
            console.warn(`Không đủ ghế cho select thứ ${index}`);
            return;
        }

        const seatId = seatIdsArray[index];
        const ticketType = select.value;
        const discountRate = discountRates[ticketType];

        const seatPrice = seatPrices[seatId] || 0;
        const finalPrice = seatPrice * discountRate;

        totalOriginal += seatPrice;
        totalFinal += finalPrice;

        // SỬA: Hiển thị đúng tên hành khách
        let passengerName;
        if (selectedSeatsCount === 1) {
            passengerName = 'Người liên hệ';
        } else {
            passengerName = index === 0 ? 'Người liên hệ' : `Hành khách ${index}`;
        }

        priceDetails.push({
            passenger: passengerName,
            seatPrice: seatPrice,
            ticketType: ticketType,
            discount: (1 - discountRate) * 100,
            finalPrice: finalPrice,
            seatId: seatId
        });

        console.log(`Vé ${index}:`, {
            passenger: passengerName,
            seatId: seatId,
            seatPrice: seatPrice,
            ticketType: ticketType,
            discountRate: discountRate,
            finalPrice: finalPrice
        });
    });

    console.log('Final Calculation:', {
        totalOriginal: totalOriginal,
        totalFinal: totalFinal,
        discountAmount: totalOriginal - totalFinal
    });

    // Hiển thị tổng giá
    displayTotalPrice(totalOriginal, totalFinal, priceDetails);
}

// ========== HÀM HIỂN THỊ TỔNG GIÁ ==========
function displayTotalPrice(originalTotal, finalTotal, details) {
    let priceContainer = document.getElementById('price-summary-container');

    if (!priceContainer) {
        priceContainer = document.createElement('div');
        priceContainer.id = 'price-summary-container';
        priceContainer.className = 'price-display';
        priceContainer.style.marginTop = '20px';
        priceContainer.style.padding = '15px';
        priceContainer.style.background = '#e7f3ff';
        priceContainer.style.border = '1px solid #b3d9ff';
        priceContainer.style.borderRadius = '4px';

        // Chèn vào trước nút submit
        const actionButtons = document.querySelector('.action-buttons');
        actionButtons.parentNode.insertBefore(priceContainer, actionButtons);
    }

    const discountAmount = originalTotal - finalTotal;
    const discountPercent = originalTotal > 0 ? (discountAmount / originalTotal * 100) : 0;

    priceContainer.innerHTML = `
        <h4 style="margin-top: 0; color: #333;"><i class="fas fa-receipt"></i> Tổng thanh toán</h4>
        <div style="font-size: 14px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                <span>Tổng giá gốc:</span>
                <span>${formatCurrency(originalTotal)}</span>
            </div>
            ${discountAmount > 0 ? `
            <div style="display: flex; justify-content: space-between; margin-bottom: 5px; color: #28a745;">
                <span>Giảm giá (${discountPercent.toFixed(1)}%):</span>
                <span>-${formatCurrency(discountAmount)}</span>
            </div>
            ` : ''}
            <div style="display: flex; justify-content: space-between; border-top: 1px solid #ccc; padding-top: 8px; font-weight: bold; font-size: 16px; color: #e74c3c;">
                <span>Tổng thanh toán:</span>
                <span>${formatCurrency(finalTotal)}</span>
            </div>
        </div>
        ${generatePriceDetailsHTML(details)}
    `;
}

// ========== HÀM TẠO HTML CHI TIẾT GIÁ ==========
function generatePriceDetailsHTML(details) {
    if (details.length === 0) return '';

    return `
        <div style="margin-top: 15px; padding-top: 10px; border-top: 1px dashed #ccc;">
            <strong style="font-size: 13px;">Chi tiết từng vé:</strong>
            ${details.map((detail, index) => `
                <div style="display: flex; justify-content: space-between; margin-top: 8px; font-size: 13px; color: #555;">
                    <div>
                        <span>${detail.passenger}</span>
                        <span style="color: #666; font-size: 12px;"> (${getTicketTypeLabel(detail.ticketType)})</span>
                    </div>
                    <div style="text-align: right;">
                        <div style="color: #999; text-decoration: line-through; font-size: 11px;">
                            ${formatCurrency(detail.seatPrice)}
                        </div>
                        <div>
                            ${formatCurrency(detail.finalPrice)}
                            ${detail.discount > 0 ?
        `<span style="background: #28a745; color: white; padding: 1px 6px; border-radius: 10px; font-size: 10px; margin-left: 5px;">-${detail.discount}%</span>`
        : ''}
                        </div>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}

// ========== HÀM LẤY GIÁ GHẾ TỪ BACKEND ==========
async function loadSeatPrices() {
    try {
        if (seatIdsArray.length === 0) return;

        console.log('Calling seat-prices API with:', {
            seatIds: seatIdsParam,
            scheduleId: scheduleId
        });

        const response = await fetch(`/api/booking/seat-prices?seatIds=${seatIdsParam}&scheduleId=${scheduleId}`);
        if (response.ok) {
            const prices = await response.json();
            console.log('API returned seat prices:', prices);
            Object.assign(seatPrices, prices);
            calculateAndDisplayPrices();
        } else {
            console.warn('Seat prices API failed, using fallback');
            useFallbackPrices();
        }
    } catch (error) {
        console.error('Error loading seat prices:', error);
        useFallbackPrices();
    }
}

// Hàm fallback dùng giá mặc định
function useFallbackPrices() {
    seatIdsArray.forEach((seatId, index) => {
        seatPrices[seatId] = 150000 + (index * 10000);
    });
    calculateAndDisplayPrices();
}

// ========== HÀM VALIDATE FORM ==========
function validatePassengerForm() {
    // Validate thông tin liên hệ
    const contactFullName = document.getElementById('fullName').value.trim();
    const contactEmail = document.getElementById('email').value.trim();
    const contactPhone = document.getElementById('phone').value.trim();
    const contactIdCard = document.getElementById('identityCard').value.trim();
    const contactTicketType = document.getElementById('contactTicketType').value;

    if (!contactFullName) {
        showError('Vui lòng nhập họ tên người liên hệ');
        document.getElementById('fullName').focus();
        return false;
    }
    if (!contactEmail) {
        showError('Vui lòng nhập email người liên hệ');
        document.getElementById('email').focus();
        return false;
    }
    if (!contactPhone) {
        showError('Vui lòng nhập số điện thoại người liên hệ');
        document.getElementById('phone').focus();
        return false;
    }
    if (!contactIdCard) {
        showError('Vui lòng nhập CMND/CCCD người liên hệ');
        document.getElementById('identityCard').focus();
        return false;
    }
    if (!contactTicketType) {
        showError('Vui lòng chọn loại vé cho người liên hệ');
        document.getElementById('contactTicketType').focus();
        return false;
    }

    // Validate hành khách đi cùng
    for (let i = 1; i <= selectedSeatsCount - 1; i++) {
        const fullNameInput = document.getElementById(`passengerFullName_${i}`);
        const idCardInput = document.getElementById(`passengerIdCard_${i}`);
        const ticketTypeInput = document.getElementById(`passengerTicketType_${i}`);

        if (fullNameInput && !fullNameInput.value.trim()) {
            showError(`Vui lòng nhập họ tên cho hành khách đi cùng thứ ${i}`);
            fullNameInput.focus();
            return false;
        }
        if (idCardInput && !idCardInput.value.trim()) {
            showError(`Vui lòng nhập CMND/CCCD cho hành khách đi cùng thứ ${i}`);
            idCardInput.focus();
            return false;
        }
        if (ticketTypeInput && !ticketTypeInput.value) {
            showError(`Vui lòng chọn loại vé cho hành khách đi cùng thứ ${i}`);
            ticketTypeInput.focus();
            return false;
        }
    }

    return true;
}

// ========== SỰ KIỆN SUBMIT FORM ==========
document.getElementById('passengerForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    if (!validatePassengerForm()) {
        return;
    }

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';

    try {
        const formData = new FormData(e.target);
        const passengers = [];

        // 1. Lấy HÀNH KHÁCH 1 (Người liên hệ)
        const contactFullName = document.getElementById('fullName').value.trim();
        const contactIdCard = document.getElementById('identityCard').value.trim();
        const contactTicketType = document.getElementById('contactTicketType').value;

        passengers.push({
            fullName: contactFullName,
            idCard: contactIdCard,
            ticketType: contactTicketType
        });

        // 2. Lấy HÀNH KHÁCH 2...N (Người đi cùng)
        for (let i = 1; i <= selectedSeatsCount - 1; i++) {
            const fullNameInput = document.getElementById(`passengerFullName_${i}`);
            const idCardInput = document.getElementById(`passengerIdCard_${i}`);
            const ticketTypeInput = document.getElementById(`passengerTicketType_${i}`);

            if (!fullNameInput || !ticketTypeInput) {
                console.warn(`Không tìm thấy trường nhập liệu cho hành khách ${i}`);
                continue;
            }

            const fullName = fullNameInput.value.trim();
            const idCard = idCardInput ? idCardInput.value.trim() : '';
            const ticketType = ticketTypeInput.value;

            if (fullName && ticketType) {
                passengers.push({
                    fullName: fullName,
                    idCard: idCard,
                    ticketType: ticketType
                });
            }
        }

        // Kiểm tra số lượng hành khách có khớp với số ghế không
        if (passengers.length !== seatIdsArray.length) {
            throw new Error(`Số lượng thông tin hành khách (${passengers.length}) không khớp với số ghế đã chọn (${seatIdsArray.length}). Vui lòng kiểm tra lại.`);
        }

        // 3. Xây dựng request
        const bookingRequest = {
            scheduleId: parseInt(formData.get('scheduleId')),
            selectedSeatIds: seatIdsArray,
            userFullName: contactFullName,
            userEmail: formData.get('email'),
            userPhone: formData.get('phone'),
            passengers: passengers,
            notes: formData.get('notes') || null
        };

        console.log('Booking Request:', bookingRequest);

        // 4. Gửi request tạo booking
        const response = await fetch('/api/booking/create-pending', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(bookingRequest)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Đặt vé thất bại');
        }

        const booking = await response.json();

        // 5. Chuyển hướng đến trang thanh toán
        window.location.href = `/payment?bookingCode=${booking.bookingCode}`;

    } catch (error) {
        console.error('Booking error:', error);
        showError(error.message || 'Có lỗi xảy ra khi đặt vé. Vui lòng thử lại.');
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-check"></i> Xác nhận và thanh toán';
    }
});

// ========== SỰ KIỆN INPUT VALIDATION ==========
document.getElementById('phone').addEventListener('input', function (e) {
    this.value = this.value.replace(/[^0-9]/g, '');
});

document.getElementById('identityCard').addEventListener('input', function (e) {
    this.value = this.value.replace(/[^0-9]/g, '');
});

// ========== KHỞI TẠO KHI TRANG LOAD ==========
document.addEventListener('DOMContentLoaded', function () {
    // Gắn sự kiện change cho tất cả select loại vé
    document.querySelectorAll('.passenger-tickettype').forEach(select => {
        select.addEventListener('change', calculateAndDisplayPrices);
    });

    // Load giá ghế khi trang ready
    loadSeatPrices();

    // Debug info
    console.log('Seat IDs:', seatIdsArray);
    console.log('Selected Seats Count:', selectedSeatsCount);
    console.log('Schedule ID:', scheduleId);

    setTimeout(() => {
        const ticketSelects = document.querySelectorAll('.passenger-tickettype');
        console.log('Found ticket selects:', ticketSelects.length);
        console.log('Seat prices loaded:', Object.keys(seatPrices).length);

        // Debug: Kiểm tra từng select có ID đúng không
        ticketSelects.forEach((select, index) => {
            console.log(`Select ${index}:`, select.id, 'Value:', select.value);
        });
    }, 1000);
});

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
        loadSeatPrices();
    });
} else {
    loadSeatPrices();
}