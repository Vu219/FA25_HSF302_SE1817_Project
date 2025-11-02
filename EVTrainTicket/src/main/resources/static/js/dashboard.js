document.addEventListener("DOMContentLoaded", function () {
    const ctx = document.getElementById("growthChart");

    new Chart(ctx, {
        type: "line",
        data: {
            labels: ["Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4"],
            datasets: [
                {
                    label: "Tổng khách hàng",
                    data: [100, 70, 50, 90],
                    borderColor: "#0e3c7e",
                    fill: false,
                    tension: 0.3
                },
                {
                    label: "Tổng vé đặt",
                    data: [90, 60, 40, 80],
                    borderColor: "#1e90ff",
                    fill: false,
                    tension: 0.3
                },
                {
                    label: "Doanh thu",
                    data: [110, 80, 60, 100],
                    borderColor: "#ff9933",
                    fill: false,
                    tension: 0.3
                }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: "bottom" }
            },
            scales: {
                y: { beginAtZero: true }
            }
        }
    });
});

    function openAddModal() {
    document.getElementById("stationForm").reset();
    document.getElementById("stationModalLabel").innerText = "Thêm nhà ga";
    document.getElementById("stationId").value = "";
}

    function openEditModal(id, name, location) {
    document.getElementById("stationModalLabel").innerText = "Cập nhật nhà ga";
    document.getElementById("stationId").value = id;
    document.getElementById("stationName").value = name;
    document.getElementById("stationLocation").value = location;
    new bootstrap.Modal(document.getElementById('stationModal')).show();
}

    function confirmDelete(id) {
    if (confirm("Bạn có chắc muốn xóa nhà ga này không?")) {
    fetch(`/manager/stations/delete/${id}`, { method: "DELETE" })
    .then(res => {
    if (res.ok) location.reload();
    else alert("Không thể xóa!");
});
}
}


