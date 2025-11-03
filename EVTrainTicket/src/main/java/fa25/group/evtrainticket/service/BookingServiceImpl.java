package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.repository.*;
import fa25.group.evtrainticket.dto.BookingRequestDto;
import fa25.group.evtrainticket.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    user.setPassword(""); // Anonymous users don't need passwords
                    user.setRole("GUEST");
                    user.setCreateAt(LocalDateTime.now());
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
    @Transactional
    public Booking cancelBooking(Integer bookingId) {
        // Implementation details...
        return null;
    }

    @Override
    public boolean validateSeatAvailability(Integer scheduleId, List<Integer> seatIds) {
        // Implementation details...
        return false;
    }

    @Override
    public double calculateBookingPrice(BookingRequestDto bookingRequest) {
        // Implementation details...
        return 0;
    }
}
