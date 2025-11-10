package fa25.group.evtrainticket.service.Impl;

import fa25.group.evtrainticket.repository.*;
import fa25.group.evtrainticket.dto.BookingRequestDto;
import fa25.group.evtrainticket.entity.*;
import fa25.group.evtrainticket.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import fa25.group.evtrainticket.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketService ticketService;

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

    @Override
    @Transactional
    public Booking createAnonymousBooking(BookingRequestDto bookingRequest) {
        // Implementation details...
        return null;
    }

    @Override
    @Transactional
    public Booking createAnonymousPendingBooking(BookingRequestDto bookingRequest) {
        try {
            // 1. Create or find anonymous user
            User anonymousUser = createOrFindAnonymousUser(bookingRequest);

            // 2. Get schedule
            Schedule schedule = scheduleRepository.findById(bookingRequest.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + bookingRequest.getScheduleId()));

            // 3. Validate seat availability
            List<Seat> selectedSeats = new ArrayList<>();
            double totalAmount = 0.0;

            for (Integer seatId : bookingRequest.getSelectedSeatIds()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId));

                if (!seat.getIsAvailable()) {
                    throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
                }

                selectedSeats.add(seat);

                // Calculate price (base price * seat type multiplier * carriage type multiplier)
                double seatPrice = schedule.getBasePrice().doubleValue() *
                        seat.getSeatType().getPriceMultiplier().doubleValue() *
                        seat.getCarriage().getCarriageType().getPriceMultiplier().doubleValue();
                totalAmount += seatPrice;
            }

            // 4. Create booking
            Booking booking = new Booking();
            booking.setBookingCode(generateBookingCode());
            booking.setUser(anonymousUser);
            booking.setBookingDate(LocalDateTime.now());
            booking.setTotalAmount(totalAmount);
            booking.setStatus("PENDING");
            booking.setNotes(bookingRequest.getNotes());

            // Save booking first to get the ID
            booking = bookingRepository.save(booking);

            // 5. Create tickets and mark seats as unavailable
            List<Ticket> tickets = new ArrayList<>();
            for (Seat seat : selectedSeats) {
                Ticket ticket = new Ticket();
                ticket.setBooking(booking);
                ticket.setSchedule(schedule);
                ticket.setSeat(seat);
                ticket.setPrice(schedule.getBasePrice().doubleValue() *
                        seat.getSeatType().getPriceMultiplier().doubleValue() *
                        seat.getCarriage().getCarriageType().getPriceMultiplier().doubleValue());
                ticket.setTicketType(bookingRequest.getTicketType() != null ? bookingRequest.getTicketType() : "ONE_WAY");
                ticket.setTicketCode(generateTicketCode());
                ticket.setStatus("PENDING");

                tickets.add(ticket);

                // Mark seat as unavailable
                seat.setIsAvailable(false);
                seatRepository.save(seat);
            }

            // Save all tickets
            ticketRepository.saveAll(tickets);
            booking.setTickets(tickets);

            return booking;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create pending booking: " + e.getMessage(), e);
        }
    }

    private User createOrFindAnonymousUser(BookingRequestDto bookingRequest) {
        // Try to find existing user by email
        return userRepository.findByEmail(bookingRequest.getUserEmail())
                .orElseGet(() -> {
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
            System.out.println("Processing payment for booking: " + bookingCode);
            System.out.println("Payment method: " + paymentMethod);

            // Update booking status
            booking.setStatus("PROCESSING");

            // Create payment record
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalAmount());
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setStatus("PENDING");

            // Generate payment code
            String paymentCode = "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            payment.setTransactionCode(paymentCode);

            System.out.println("Created payment with code: " + paymentCode);
            paymentRepository.save(payment);

            return save(booking);
        }
        return null;
    }

    @Override
    @Transactional
    public Booking confirmPayment(String bookingCode) {
        Booking booking = findByBookingCode(bookingCode);
        if (booking != null) {
            System.out.println("Confirming payment for booking: " + bookingCode);

            // Update booking status
            booking.setStatus("CONFIRMED"); // Or COMPLETED, depending on your status flow

            // Update all tickets to ACTIVE status
            List<Ticket> tickets = ticketService.activateTickets(booking.getBookingID());

            // Update payment status to COMPLETED
            List<Payment> payments = paymentRepository.findByBookingBookingID(booking.getBookingID());
            for (Payment payment : payments) {
                payment.setStatus("COMPLETED");
                System.out.println("Completed payment: " + payment.getTransactionCode());
            }
            paymentRepository.saveAll(payments);

            System.out.println("✅ Payment confirmed, all tickets activated");

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
        // Implementation details...
        return null;
    }

    @Override
    public boolean validateSeatAvailability(Integer scheduleId, List<Integer> seatIds) {
        try {
            System.out.println("=== VALIDATING SEAT AVAILABILITY ===");
            System.out.println("scheduleId: " + scheduleId);
            System.out.println("seatIds to validate: " + seatIds);

            if (seatIds == null || seatIds.isEmpty()) {
                System.out.println("No seats to validate");
                return false;
            }

            // Get all tickets for this schedule that are booked or confirmed
            List<Ticket> bookedTickets = ticketRepository.findByScheduleScheduleIDAndBookingStatusIn(
                scheduleId,
                Arrays.asList("BOOKED", "CONFIRMED", "PAID")
            );

            // Get set of booked seat IDs
            Set<Integer> bookedSeatIds = bookedTickets.stream()
                .map(ticket -> ticket.getSeat().getSeatID())
                .collect(Collectors.toSet());

            System.out.println("Booked seat IDs: " + bookedSeatIds);
            System.out.println("Requested seat IDs: " + seatIds);

            // Check if any requested seats are already booked
            for (Integer seatId : seatIds) {
                if (bookedSeatIds.contains(seatId)) {
                    System.out.println("Seat " + seatId + " is already booked!");
                    return false;
                }
            }

            // Additional check: verify seats exist and belong to this schedule's train
            List<Seat> seats = seatRepository.findAllById(seatIds);
            if (seats.size() != seatIds.size()) {
                System.out.println("Some seat IDs don't exist. Found " + seats.size() + " out of " + seatIds.size());
                return false;
            }

            // Verify all seats belong to the correct train for this schedule
            Optional<Schedule> schedule = scheduleRepository.findById(scheduleId);
            if (schedule.isEmpty()) {
                System.out.println("Schedule not found: " + scheduleId);
                return false;
            }

            Integer trainId = schedule.get().getTrain().getTrainID();
            for (Seat seat : seats) {
                if (!seat.getCarriage().getTrain().getTrainID().equals(trainId)) {
                    System.out.println("Seat " + seat.getSeatID() + " doesn't belong to train " + trainId);
                    return false;
                }

                // Check if seat is marked as available
                if (!seat.getIsAvailable()) {
                    System.out.println("Seat " + seat.getSeatID() + " is marked as unavailable");
                    return false;
                }
            }

            System.out.println("✅ All seats are available!");
            return true;

        } catch (Exception e) {
            System.out.println("ERROR in seat validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public double calculateBookingPrice(BookingRequestDto bookingRequest) {
        // Implementation details...
        return 0;
    }
}