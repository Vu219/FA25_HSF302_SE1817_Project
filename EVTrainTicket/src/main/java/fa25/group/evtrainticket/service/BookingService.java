package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.dto.BookingRequestDto;
import fa25.group.evtrainticket.dto.BookingResponseDto;
import fa25.group.evtrainticket.entity.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    /**
     * Find booking by booking code
     * @param bookingCode The booking code
     * @return The booking or null if not found
     */
    Booking findByBookingCode(String bookingCode);

    /**
     * Save a booking
     * @param booking The booking to save
     * @return The saved booking
     */
    Booking save(Booking booking);

    /**
     * Update a booking
     * @param booking The booking to update
     * @return The updated booking
     */
    Booking updateBooking(Booking booking);

    Booking createAnonymousPendingBooking(BookingRequestDto bookingRequest);

    Booking processPayment(String bookingCode, String paymentMethod);

    Booking confirmPayment(String bookingCode);

    Booking getBookingByCode(String bookingCode);

    List<Booking> getBookingsByEmail(String email);

    List<Booking> getBookingsByUserId(Integer userId);

    Booking cancelBooking(Integer bookingId);

    boolean validateSeatAvailability(Integer scheduleId, List<Integer> seatIds);

    double calculateBookingPrice(BookingRequestDto bookingRequest);
    List<BookingResponseDto> getUserBookingsWithFilter(Integer userId, String status, LocalDate fromDate, LocalDate toDate);
}
