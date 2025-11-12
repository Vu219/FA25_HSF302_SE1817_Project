package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.dto.BookingResponseDto;
import fa25.group.evtrainticket.dto.PassengerDto;
import fa25.group.evtrainticket.dto.TicketType;
import fa25.group.evtrainticket.mapper.BookingMapper;
import fa25.group.evtrainticket.repository.*;
import fa25.group.evtrainticket.dto.BookingRequestDto;
import fa25.group.evtrainticket.entity.*;
import fa25.group.evtrainticket.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import fa25.group.evtrainticket.utils.TicketUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingMapper bookingMapper;


    @Override
    public Booking findByBookingCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode).orElse(null);
    }

    @Override
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    private String generateUniqueTicketCode() {
        String code;
        boolean isDuplicate;
        do {
            // Sinh mã ngẫu nhiên (VD: A8B9C2)
            code = TicketUtils.generateRandomCode();

            // Kiểm tra trong DB xem có chưa (gọi Repository)
            isDuplicate = ticketRepository.existsByTicketCode(code);

        } while (isDuplicate); // Nếu trùng thì quay lại sinh mã khác

        return code;
    }

    @Override
    @Transactional
    public Booking createAnonymousPendingBooking(BookingRequestDto bookingRequest) {
        try {
            // 1. Validate seats trước
            if (!validateSeatAvailability(bookingRequest.getScheduleId(), bookingRequest.getSelectedSeatIds())) {
                throw new RuntimeException("Một hoặc nhiều ghế không khả dụng hoặc không thuộc lịch trình này");
            }

            // 2. Tính giá sử dụng method riêng
            double totalAmount = calculateBookingPrice(bookingRequest);

            // 3. Tạo hoặc tìm người dùng liên hệ
            User anonymousUser = createOrFindAnonymousUser(bookingRequest);

            // 4. Lấy thông tin lịch trình
            Schedule schedule = scheduleRepository.findById(bookingRequest.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch trình với ID: " + bookingRequest.getScheduleId()));

            // 5. Validate số lượng ghế và hành khách
            List<Integer> seatIds = bookingRequest.getSelectedSeatIds();
            List<PassengerDto> passengers = bookingRequest.getPassengers();

            if (seatIds == null || passengers == null) {
                throw new RuntimeException("Thiếu thông tin ghế hoặc hành khách.");
            }
            if (seatIds.size() != passengers.size()) {
                throw new RuntimeException("Số lượng ghế (" + seatIds.size() + ") không khớp với số lượng hành khách (" + passengers.size() + ")");
            }

            // 6. Tạo Booking với tổng tiền ĐÃ TÍNH
            Booking booking = new Booking();
            booking.setBookingCode(generateBookingCode());
            booking.setUser(anonymousUser);
            booking.setBookingDate(LocalDateTime.now());
            booking.setTotalAmount(totalAmount); // Sử dụng giá đã tính
            booking.setStatus("PENDING");
            booking.setNotes(bookingRequest.getNotes());

            booking = bookingRepository.save(booking);

            // 7. Lấy danh sách ghế và tạo vé
            List<Seat> selectedSeats = new ArrayList<>();
            List<Ticket> tickets = new ArrayList<>();

            for (int i = 0; i < seatIds.size(); i++) {
                Integer seatId = seatIds.get(i);
                PassengerDto passenger = passengers.get(i);

                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế với ID: " + seatId));
                selectedSeats.add(seat);

                // 1. Parse loại vé an toàn
                TicketType ticketType;
                try {
                    // Chuyển về chữ in hoa để tránh lỗi case-sensitive (child vs CHILD)
                    ticketType = TicketType.valueOf(passenger.getTicketType().toUpperCase());
                } catch (Exception e) {
                    ticketType = TicketType.ADULT; // Mặc định là người lớn nếu lỗi
                }

                // 2. Tính giá vé
                double baseSeatPrice = schedule.getBasePrice().doubleValue() *
                        seat.getSeatType().getPriceMultiplier().doubleValue() *
                        seat.getCarriage().getCarriageType().getPriceMultiplier().doubleValue();

                double finalTicketPrice = baseSeatPrice * ticketType.getPriceMultiplier();

                // 3. Tạo Ticket Object
                Ticket ticket = new Ticket();
                ticket.setBooking(booking);
                ticket.setSchedule(schedule);
                ticket.setSeat(seat);
                ticket.setPassengerName(passenger.getFullName());
                ticket.setPrice(finalTicketPrice);
                ticket.setTicketType(ticketType.name());
                ticket.setTicketCode(generateUniqueTicketCode()); // Hàm random không trùng
                ticket.setStatus("PENDING");

                // 4. XỬ LÝ CCCD (LOGIC ĐÃ TỐI ƯU)
                String idCard = passenger.getIdCard();

                // Sử dụng Enum ticketType để so sánh chuẩn xác hơn
                if (ticketType == TicketType.CHILD) {
                    // Nếu là TRẺ EM:
                    // Kiểm tra nếu null hoặc rỗng thì gán null (nếu DB cho phép) hoặc chuỗi mặc định
                    if (idCard == null || idCard.trim().isEmpty()) {
                        ticket.setPassengerIDCard(null); // Lưu ý: DB phải allow NULL. Nếu không thì điền "TRE_EM"
                    } else {
                        ticket.setPassengerIDCard(idCard); // Nếu họ vẫn nhập thì cứ lưu
                    }
                } else {
                    // Nếu là NGƯỜI LỚN / NGƯỜI GIÀ: Bắt buộc phải có ID
                    if (idCard == null || idCard.trim().isEmpty()) {
                        throw new RuntimeException("Vui lòng nhập số CMND/CCCD cho hành khách: " + passenger.getFullName());
                    }
                    ticket.setPassengerIDCard(idCard);
                }

                tickets.add(ticket);

                // 5. Khóa ghế
                seat.setIsAvailable(false);
                seatRepository.save(seat);
            }

            ticketRepository.saveAll(tickets);
            booking.setTickets(tickets);

            return booking;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create pending booking: " + e.getMessage(), e);
        }
    }

    private User createOrFindAnonymousUser(BookingRequestDto bookingRequest) {
        // Try to find existing user by email
        return userRepository.findByEmail(bookingRequest.getUserEmail()).orElseGet(() -> {
            // Create new anonymous user
            User user = new User();
            user.setFullName(bookingRequest.getUserFullName());
            user.setEmail(bookingRequest.getUserEmail());
            user.setPhone(bookingRequest.getUserPhone());

            // ===============================================
            // FIX: Đặt mật khẩu placeholder hợp lệ (thay vì "")
            // để qua mặt validation @Size(min=6)
            // ===============================================
            user.setPassword("guest_user");

            user.setRole("GUEST");
            user.setCreatedAt(LocalDateTime.now());
            user.setStatus("ACTIVE");
            return userRepository.save(user);
        });
    }

    private String generateBookingCode() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = bookingRepository.count() + 1;
        return String.format("BK%s%03d", dateStr, count);
    }

    private String generateTicketCode() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = ticketRepository.count() + 1;
        return String.format("TK%s%03d", dateStr, count);
    }

    @Override
    @Transactional
    public Booking processPayment(String bookingCode, String paymentMethod) {
        Booking booking = findByBookingCode(bookingCode);
        if (booking != null) {
            booking.setStatus("PROCESSING");
            return save(booking);
        }
        return null;
    }

    @Override
    @Transactional
    public Booking confirmPayment(String bookingCode) {
        Booking booking = findByBookingCode(bookingCode);
        if (booking != null) {
            booking.setStatus("COMPLETED");
            return save(booking);
        }
        return null;
    }

    @Override
    public Booking getBookingByCode(String bookingCode) {
        return findByBookingCode(bookingCode);
    }

    @Override
    public List<Booking> getBookingsByEmail(String email) {
        return bookingRepository.findByUserEmail(email);
    }

    @Override
    public List<Booking> getBookingsByUserId(Integer userId) {
        return bookingRepository.findByUserUserIDOrderByBookingDateDesc(userId);
    }

    @Override
    @Transactional
    public Booking cancelBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + bookingId));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking này đã bị hủy trước đó.");
        }

        booking.setStatus("CANCELLED");
        List<Ticket> tickets = ticketRepository.findByBookingBookingID(bookingId);

        for (Ticket ticket : tickets) {
            ticket.setStatus("CANCELLED");

        }

        return bookingRepository.save(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookingsWithFilter(Integer userId, String status, LocalDate fromDate, LocalDate toDate) {
        // 1. Convert LocalDate (from HTML) to LocalDateTime (for Database)
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (fromDate != null) {
            startDateTime = fromDate.atStartOfDay(); // 00:00:00
        }

        if (toDate != null) {
            endDateTime = toDate.atTime(23, 59, 59); // 23:59:59
        }

        // 2. Call the Repository
        List<Booking> bookings = bookingRepository.findFilteredBookings(userId, status, startDateTime, endDateTime);

        // 3. Convert to DTOs using your Mapper
        // Your BookingMapper has a method 'toDtoList' that returns List<BookingResponseDto>
        return bookingMapper.toDtoList(bookings);
    }

    @Override
    public boolean validateSeatAvailability(Integer scheduleId, List<Integer> seatIds) {
        try {
            for (Integer seatId : seatIds) {
                Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new RuntimeException("Không tìm thấy ghế: " + seatId));

                if (!seat.getIsAvailable()) {
                    return false; // Ghế không khả dụng
                }

                // Kiểm tra xem ghế có thuộc về lịch trình này không
                // 2. Check ghế có thuộc về lịch trình này không - SỬA LẠI
                boolean belongsToSchedule = seat.getCarriage().getTrain().getSchedules().stream()
                        .anyMatch(s -> s.getScheduleID().equals(scheduleId));

                if (!belongsToSchedule) {
                    return false; // Ghế không thuộc lịch trình
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double calculateBookingPrice(BookingRequestDto bookingRequest) {
        try {
            Schedule schedule = scheduleRepository.findById(bookingRequest.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            List<Integer> seatIds = bookingRequest.getSelectedSeatIds();
            List<PassengerDto> passengers = bookingRequest.getPassengers();

            if (seatIds == null || passengers == null || seatIds.size() != passengers.size()) {
                throw new RuntimeException("Invalid seat or passenger data");
            }

            double totalAmount = 0.0;

            for (int i = 0; i < seatIds.size(); i++) {
                Integer seatId = seatIds.get(i);
                PassengerDto passenger = passengers.get(i);

                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế: " + seatId));

                // Tính giá gốc
                double baseSeatPrice = schedule.getBasePrice().doubleValue() *
                        seat.getSeatType().getPriceMultiplier().doubleValue() *
                        seat.getCarriage().getCarriageType().getPriceMultiplier().doubleValue();

                // Áp dụng giảm giá theo loại vé
                TicketType ticketType;
                try {
                    ticketType = TicketType.valueOf(passenger.getTicketType());
                } catch (Exception e) {
                    ticketType = TicketType.ADULT;
                }

                double finalPrice = baseSeatPrice * ticketType.getPriceMultiplier();
                totalAmount += finalPrice;
            }

            return totalAmount;

        } catch (Exception e) {
            throw new RuntimeException("Không thể tính toán giá đặt vé: " + e.getMessage());
        }
    }
}