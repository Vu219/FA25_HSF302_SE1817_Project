// ================================
// FILE: /js/booking.js
// ================================

document.addEventListener('DOMContentLoaded', function() {
    const checkboxes = document.querySelectorAll('.seat-checkbox');
    const nextBtn = document.getElementById('next-btn');
    const totalAmountEl = document.getElementById('total-amount');
    const summarySeatsList = document.getElementById('summary-seats-list');
    const selectedCountEl = document.getElementById('selected-count');

    // 1. Calculate Totals & Update UI
    function updateSummary() {
        let selectedSeats = [];
        let totalAmount = 0;

        // Find all checked boxes
        const checkedBoxes = document.querySelectorAll('.seat-checkbox:checked');

        checkedBoxes.forEach(box => {
            // Read data from Thymeleaf data-attributes
            const price = parseFloat(box.getAttribute('data-price'));
            const seatNumber = box.getAttribute('data-seat-number');
            const seatType = box.getAttribute('data-seat-type');
            const carriage = box.getAttribute('data-carriage');

            selectedSeats.push({
                number: seatNumber,
                type: seatType,
                price: price,
                carriage: carriage
            });
            totalAmount += price;
        });

        // Update Right Sidebar (Summary)
        if (selectedSeats.length === 0) {
            summarySeatsList.innerHTML = '<p class="text-muted" style="text-align: center; padding: 20px;">Chưa chọn ghế nào</p>';
        } else {
            summarySeatsList.innerHTML = '';
            selectedSeats.forEach(seat => {
                const item = document.createElement('div');
                item.className = 'seat-summary-item';
                item.innerHTML = `
                    <div class="seat-info">
                        <span class="seat-number">Ghế ${seat.number}</span>
                        <span class="seat-type">${seat.type} - Toa ${seat.carriage}</span>
                    </div>
                    <span class="seat-price">${seat.price.toLocaleString('vi-VN')} VNĐ</span>
                `;
                summarySeatsList.appendChild(item);
            });
        }

        // Update Total Price Text
        totalAmountEl.textContent = totalAmount.toLocaleString('vi-VN') + ' VNĐ';
        if(selectedCountEl) selectedCountEl.textContent = selectedSeats.length;

        // Enable/Disable Next Button
        if (nextBtn) {
            nextBtn.disabled = selectedSeats.length === 0;
        }
    }

    // 2. Add Click Events
    checkboxes.forEach(box => {
        box.addEventListener('change', function() {
            // Find the label associated with this checkbox
            const label = document.querySelector(`label[for="${this.id}"]`);

            if (label) {
                if (this.checked) {
                    label.classList.remove('available');
                    label.classList.add('selected');
                } else {
                    label.classList.remove('selected');
                    label.classList.add('available');
                }
            }
            updateSummary();
        });

        // Initialize state (in case browser cached the checked state)
        if (box.checked) {
            const label = document.querySelector(`label[for="${box.id}"]`);
            if (label) {
                label.classList.remove('available');
                label.classList.add('selected');
            }
        }
    });

    // Run once on load
    updateSummary();
});