// ================================
// FILE: /js/booking.js
// ================================

let selectedSchedule = null;
let selectedSeats = [];
let seatLayout = [];
let totalAmount = 0;
const MAX_SEATS_PER_BOOKING = 8;

/**
 * Khởi tạo khi tải trang
 */
document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const scheduleId = urlParams.get('scheduleId');

    if (scheduleId) {
        loadScheduleInfo(scheduleId);
        document.getElementById('booking-content').style.display = 'block';
    } else {
        showError('Không có chuyến tàu nào được chọn. Vui lòng quay lại trang chủ và tìm kiếm.');
    }

    // Gắn sự kiện cho nút tiếp theo
    document.getElementById('next-btn').addEventListener('click', handleNextStep);
});

/**
 * Hiển thị lỗi
 */
function showError(message) {
    document.getElementById('booking-content').style.display = 'none';
    document.getElementById('booking-summary').style.display = 'none';
    document.getElementById('schedule-info-card').style.display = 'none';
    document.getElementById('booking-error-message').textContent = message;
    document.getElementById('booking-error').style.display = 'flex';
}

/**
 * Tải thông tin chuyến tàu
 */
function loadScheduleInfo(scheduleId) {
    fetch(`/api/schedules/${scheduleId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Không tìm thấy chuyến tàu. Mã lỗi: ' + response.status);
            }
            return response.json();
        })
        .then(schedule => {
            selectedSchedule = {
                scheduleId: schedule.scheduleId,
                trainName: schedule.trainName,
                departureStation: schedule.departureStation,
                arrivalStation: schedule.arrivalStation,
                departureTime: new Date(schedule.departureTime).toLocaleString('vi-VN'),
                arrivalTime: new Date(schedule.arrivalTime).toLocaleString('vi-VN'),
                basePrice: schedule.basePrice
            };
            updateScheduleDisplay();
            loadSeatLayout(scheduleId);
        })
        .catch(error => {
            console.error('Error loading schedule:', error);
            showError(error.message);
        });
}

/**
 * Cập nhật thông tin chuyến tàu lên UI
 */
function updateScheduleDisplay() {
    if (selectedSchedule) {
        document.getElementById('train-name').textContent = selectedSchedule.trainName;
        document.getElementById('departure-station').textContent = selectedSchedule.departureStation;
        document.getElementById('arrival-station').textContent = selectedSchedule.arrivalStation;
        document.getElementById('departure-time').textContent = selectedSchedule.departureTime;
        document.getElementById('arrival-time').textContent = selectedSchedule.arrivalTime;
        document.getElementById('base-price').textContent = selectedSchedule.basePrice.toLocaleString('vi-VN');

        // Cập nhật summary
        document.getElementById('summary-train-name').textContent = selectedSchedule.trainName;
        document.getElementById('summary-route').textContent = `${selectedSchedule.departureStation} → ${selectedSchedule.arrivalStation}`;
    }
}

/**
 * Tải sơ đồ ghế ngồi
 */
function loadSeatLayout(scheduleId) {
    fetch(`/api/schedules/${scheduleId}/seats`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Lỗi tải sơ đồ ghế: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            seatLayout = data;
            renderSeatLayout();
        })
        .catch(error => {
            console.error('Error loading seats:', error);
            document.getElementById('carriages-container').innerHTML =
                `<div class="alert alert-danger">${error.message}</div>`;
        });
}

/**
 * Vẽ sơ đồ ghế lên UI
 */
function renderSeatLayout() {
    const container = document.getElementById('carriages-container');
    container.innerHTML = '';

    if (!seatLayout || seatLayout.length === 0) {
        container.innerHTML = '<div class="alert alert-warning">Không có thông tin ghế ngồi cho chuyến tàu này.</div>';
        return;
    }

    seatLayout.forEach(carriage => {
        const carriageDiv = document.createElement('div');
        carriageDiv.className = 'carriage-section';

        carriageDiv.innerHTML = `
            <h6>
                <i class="fas fa-subway"></i> 
                ${carriage.carriageTypeName} - Toa ${carriage.carriageNumber}
                <small style="font-size: 0.8em; color: #666; margin-left: 10px;">
                    (${carriage.seats.filter(s => s.isAvailable === true).length}/${carriage.seats.length} ghế trống)
                </small>
            </h6>
        `;

        const seatMap = document.createElement('div');
        seatMap.className = 'seat-map';

        // Sort seats by row and column for proper display
        carriage.seats.sort((a, b) => {
            const rowDiff = a.rowNumber - b.rowNumber;
            return rowDiff !== 0 ? rowDiff : (a.columnNum || 0) - (b.columnNum || 0);
        });

        // Create and append seats
        carriage.seats.forEach(seat => {
            const seatDiv = document.createElement('div');
            const isAvailable = seat.isAvailable === true;

            seatDiv.className = `seat ${isAvailable ? 'available' : 'booked'}`;
            if (selectedSeats.some(s => s.seatID === seat.seatID)) {
                seatDiv.classList.remove('available');
                seatDiv.classList.add('selected');
            }

            seatDiv.textContent = seat.seatNumber;
            seatDiv.dataset.seatId = seat.seatID;
            seatDiv.dataset.price = seat.price;
            seatDiv.dataset.seatType = seat.seatTypeName;
            seatDiv.dataset.isAvailable = seat.isAvailable;
            seatDiv.title = isAvailable
                ? `${seat.seatTypeName} - ${parseFloat(seat.price).toLocaleString('vi-VN')} VNĐ - Có sẵn`
                : `${seat.seatNumber} - Đã đặt`;

            if (isAvailable) {
                seatDiv.addEventListener('click', () => toggleSeat(seat));
            } else {
                seatDiv.style.cursor = 'not-allowed';
            }

            seatMap.appendChild(seatDiv);
        });

        carriageDiv.appendChild(seatMap);
        container.appendChild(carriageDiv);
    });
}

/**
 * Xử lý chọn/bỏ chọn ghế
 */
function toggleSeat(seat) {
    const seatDiv = document.querySelector(`[data-seat-id="${seat.seatID}"]`);
    const index = selectedSeats.findIndex(s => s.seatID === seat.seatID);

    if (index > -1) {
        // Bỏ chọn ghế
        selectedSeats.splice(index, 1);
        seatDiv.classList.remove('selected');
        seatDiv.classList.add('available');
    } else {
        // Chọn ghế mới
        if (selectedSeats.length >= MAX_SEATS_PER_BOOKING) {
            alert(`Bạn chỉ có thể chọn tối đa ${MAX_SEATS_PER_BOOKING} ghế.`);
            return;
        }
        selectedSeats.push(seat);
        seatDiv.classList.remove('available');
        seatDiv.classList.add('selected');
    }
    updateBookingSummary();
}

/**
 * Cập nhật tóm tắt đặt vé
 */
function updateBookingSummary() {
    const summarySeatsList = document.getElementById('summary-seats-list');
    const totalAmountEl = document.getElementById('total-amount');
    const nextBtn = document.getElementById('next-btn');

    if (selectedSeats.length === 0) {
        summarySeatsList.innerHTML = '<p class="text-muted" style="text-align: center; padding: 20px;">Chưa chọn ghế nào</p>';
        totalAmountEl.textContent = '0 VNĐ';
        nextBtn.disabled = true;
    } else {
        let content = '';
        let totalAmount = 0;

        selectedSeats.forEach(seat => {
            const seatPrice = parseFloat(seat.price);
            totalAmount += seatPrice;

            content += `
                <div class="seat-summary-item">
                    <div class="seat-info">
                        <span class="seat-number">${seat.seatNumber}</span>
                        <span class="seat-type">${seat.seatTypeName}</span>
                    </div>
                    <span class="seat-price">${seatPrice.toLocaleString('vi-VN')} VNĐ</span>
                </div>
            `;
        });

        summarySeatsList.innerHTML = content;
        totalAmountEl.textContent = totalAmount.toLocaleString('vi-VN') + ' VNĐ';
        nextBtn.disabled = false;
    }
}

/**
 * Xử lý nút "Tiếp theo"
 */
function handleNextStep() {
    if (selectedSeats.length === 0) {
        alert('Vui lòng chọn ít nhất một ghế.');
        return;
    }

    // Chuyển đến trang thông tin hành khách
    const seatIds = selectedSeats.map(seat => seat.seatID).join(',');
    window.location.href = `/booking/passenger-info?scheduleId=${selectedSchedule.scheduleId}&seatIds=${seatIds}`;
}

/**
 * Kiểm tra tính hợp lệ của các ghế đã chọn
 */
function validateSeatSelection() {
    if (selectedSeats.length === 0) {
        alert('Vui lòng chọn ít nhất một ghế.');
        return false;
    }
    return true;
}