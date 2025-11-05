let growthChart = null;

document.addEventListener('DOMContentLoaded', function() {
    loadTotalStats();
    loadWeeklyStats();
    document.getElementById('toggleUsers').addEventListener('change', updateChartVisibility);
    document.getElementById('toggleTickets').addEventListener('change', updateChartVisibility);
    document.getElementById('toggleRevenue').addEventListener('change', updateChartVisibility);
});

async function loadTotalStats() {
    try {
        const response = await fetch('/admin/dashboard/total-stats');
        const stats = await response.json();

        renderStatsCards(stats);
    } catch (error) {
        console.error('Error loading total stats:', error);
        alert('Có lỗi xảy ra khi tải dữ liệu thống kê');
    }
}

async function loadWeeklyStats() {
    const startMonth = document.getElementById('startMonth').value;
    const endMonth = document.getElementById('endMonth').value;

    try {
        const response = await fetch(`/admin/dashboard/weekly-stats?startMonth=${startMonth}&endMonth=${endMonth}`);
        const data = await response.json();

        renderChart(data.weeklyStats);
        document.getElementById('chartDateRange').textContent = data.monthRange;

    } catch (error) {
        console.error('Error loading weekly stats:', error);
        alert('Có lỗi xảy ra khi tải dữ liệu biểu đồ');
    }
}

function renderStatsCards(stats) {
    const statsGrid = document.getElementById('statsGrid');
    statsGrid.innerHTML = '';

    const icons = {
        'Tổng người dùng': { icon: '👥', class: 'users' },
        'Tổng số vé': { icon: '🎫', class: 'tickets' },
        'Doanh thu': { icon: '💰', class: 'revenue' }
    };

    stats.forEach(stat => {
        const iconInfo = icons[stat.title] || { icon: '📊', class: 'default' };
        const isPositive = stat.growing;

        const card = document.createElement('div');
        card.className = 'stat-card';
        card.innerHTML = `
            <div class="stat-icon ${iconInfo.class}">
                ${iconInfo.icon}
            </div>
            <div class="stat-info">
                <div class="stat-title">${stat.title}</div>
                <div class="stat-value">${stat.value}</div>
                <div class="stat-change ${isPositive ? 'positive' : 'negative'}">
                    ${isPositive ? '↗' : '↘'} ${stat.change}
                </div>
            </div>
        `;
        statsGrid.appendChild(card);
    });
}

function renderChart(weeklyStats) {
    const ctx = document.getElementById('growthChart').getContext('2d');

    const labels = weeklyStats.map((week, index) => `Tuần ${index + 1}`);
    const weekRanges = weeklyStats.map(week => week.weekRange);

    const usersData = weeklyStats.map(week => {
        const userStat = week.stats.find(s => s.title === 'Tổng số người dùng');
        return parseFloat(userStat.change.replace(/[+%]/g, ''));
    });

    const ticketsData = weeklyStats.map(week => {
        const ticketStat = week.stats.find(s => s.title === 'Tổng số vé');
        return parseFloat(ticketStat.change.replace(/[+%]/g, ''));
    });

    const revenueData = weeklyStats.map(week => {
        const revenueStat = week.stats.find(s => s.title === 'Doanh thu tuần');
        return parseFloat(revenueStat.change.replace(/[+%]/g, ''));
    });

    if (growthChart) {
        growthChart.destroy();
    }

    growthChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Tổng người dùng',
                    data: usersData,
                    borderColor: '#4CAF50',
                    backgroundColor: 'rgba(76, 175, 80, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true,
                    pointRadius: 5,
                    pointHoverRadius: 7
                },
                {
                    label: 'Tổng số vé',
                    data: ticketsData,
                    borderColor: '#2196F3',
                    backgroundColor: 'rgba(33, 150, 243, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true,
                    pointRadius: 5,
                    pointHoverRadius: 7
                },
                {
                    label: 'Doanh thu',
                    data: revenueData,
                    borderColor: '#FF9800',
                    backgroundColor: 'rgba(255, 152, 0, 0.1)',
                    borderWidth: 3,
                    tension: 0.4,
                    fill: true,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    borderColor: '#ccc',
                    borderWidth: 1,
                    callbacks: {
                        title: function(context) {
                            return weekRanges[context[0].dataIndex];
                        },
                        label: function(context) {
                            return context.dataset.label + ': ' + context.parsed.y.toFixed(0) + '%';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return value + '%';
                        }
                    },
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    }
                }
            },
            onClick: (event, elements) => {
                if (elements.length > 0) {
                    const index = elements[0].index;
                    showWeekDetails(weeklyStats[index]);
                }
            }
        }
    });
}

function showWeekDetails(weekData) {
    const detailsDiv = document.getElementById('weekDetails');

    let detailsHTML = `<strong>${weekData.weekRange}</strong><br>`;
    weekData.stats.forEach(stat => {
        detailsHTML += `${stat.title}: ${stat.value} (${stat.change})<br>`;
    });

    detailsDiv.innerHTML = detailsHTML;
}

function updateChartVisibility() {
    const showUsers = document.getElementById('toggleUsers').checked;
    const showTickets = document.getElementById('toggleTickets').checked;
    const showRevenue = document.getElementById('toggleRevenue').checked;

    if (growthChart) {
        growthChart.data.datasets[0].hidden = !showUsers;
        growthChart.data.datasets[1].hidden = !showTickets;
        growthChart.data.datasets[2].hidden = !showRevenue;
        growthChart.update();
    }
}