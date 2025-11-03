package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.entity.Booking;
import fa25.group.evtrainticket.entity.Ticket;
import java.util.List;

public interface TicketService {
    /**
     * Activate tickets for a booking (after payment confirmation)
     * @param bookingId The booking ID
     * @return List of activated tickets
     */
    List<Ticket> activateTickets(Integer bookingId);

    /**
     * Activate tickets for a booking using booking code
     * @param bookingCode The booking code
     * @return List of activated tickets
     */
    List<Ticket> activateTicketsForBooking(String bookingCode);

    /**
     * Check if a ticket is active
     * @param ticketId The ticket ID
     * @return true if ticket is active, false otherwise
     */
    boolean isTicketActive(Integer ticketId);

    /**
     * Check if a ticket is active by ticket code
     * @param ticketCode The ticket code
     * @return true if ticket is active, false otherwise
     */
    boolean isTicketActiveByCode(String ticketCode);

    /**
     * Find tickets by booking
     * @param booking The booking
     * @return List of tickets
     */
    List<Ticket> findByBooking(Booking booking);

    /**
     * Save a ticket
     * @param ticket The ticket to save
     * @return The saved ticket
     */
    Ticket save(Ticket ticket);

    /**
     * Get ticket by ticket code
     * @param ticketCode The ticket code
     * @return Ticket object
     */
    Ticket getTicketByCode(String ticketCode);

    /**
     * Cancel tickets for a booking
     * @param bookingId The booking ID
     * @return List of cancelled tickets
     */
    List<Ticket> cancelTickets(Integer bookingId);

    /**
     * Update ticket status
     * @param ticketId The ticket ID
     * @param status The new status
     * @return Updated ticket
     */
    Ticket updateTicketStatus(Integer ticketId, String status);

    /**
     * Get tickets by booking ID
     * @param bookingId The booking ID
     * @return List of tickets
     */
    List<Ticket> getTicketsByBookingId(Integer bookingId);

    /**
     * Check if tickets are valid for travel (active and not expired)
     * @param ticketCodes List of ticket codes to check
     * @return true if all tickets are valid
     */
    boolean areTicketsValidForTravel(List<String> ticketCodes);

    /**
     * Mark ticket as used (for boarding)
     * @param ticketCode The ticket code
     * @return Updated ticket
     */
    Ticket markTicketAsUsed(String ticketCode);
}
