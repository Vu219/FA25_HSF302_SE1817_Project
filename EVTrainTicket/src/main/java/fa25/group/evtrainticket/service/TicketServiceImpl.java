package fa25.group.evtrainticket.service;

import fa25.group.evtrainticket.repository.TicketRepository;
import fa25.group.evtrainticket.entity.Booking;
import fa25.group.evtrainticket.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    @Transactional
    public List<Ticket> activateTickets(Integer bookingId) {
        List<Ticket> tickets = ticketRepository.findByBookingBookingID(bookingId);
        tickets.forEach(ticket -> ticket.setStatus("ACTIVE"));
        return ticketRepository.saveAll(tickets);
    }

    @Override
    @Transactional
    public List<Ticket> activateTicketsForBooking(String bookingCode) {
        List<Ticket> tickets = ticketRepository.findByBookingBookingCode(bookingCode);

        if (!tickets.isEmpty()) {
            // Update booking status to COMPLETED
            Booking booking = tickets.get(0).getBooking();
            booking.setStatus("COMPLETED");

            // Activate all tickets
            tickets.forEach(ticket -> ticket.setStatus("ACTIVE"));
            return ticketRepository.saveAll(tickets);
        }

        return tickets;
    }

    @Override
    public boolean isTicketActive(Integer ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> "ACTIVE".equals(ticket.getStatus()))
                .orElse(false);
    }

    @Override
    public boolean isTicketActiveByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .map(ticket -> "ACTIVE".equals(ticket.getStatus()))
                .orElse(false);
    }

    @Override
    public List<Ticket> findByBooking(Booking booking) {
        return ticketRepository.findByBooking(booking);
    }

    @Override
    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket getTicketByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode).orElse(null);
    }

    @Override
    @Transactional
    public List<Ticket> cancelTickets(Integer bookingId) {
        List<Ticket> tickets = ticketRepository.findByBookingBookingID(bookingId);
        tickets.forEach(ticket -> ticket.setStatus("CANCELLED"));
        return ticketRepository.saveAll(tickets);
    }

    @Override
    @Transactional
    public Ticket updateTicketStatus(Integer ticketId, String status) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> {
                    ticket.setStatus(status);
                    return ticketRepository.save(ticket);
                })
                .orElse(null);
    }

    @Override
    public List<Ticket> getTicketsByBookingId(Integer bookingId) {
        return ticketRepository.findByBookingBookingID(bookingId);
    }

    @Override
    public boolean areTicketsValidForTravel(List<String> ticketCodes) {
        return ticketCodes.stream()
                .allMatch(code -> ticketRepository.findByTicketCode(code)
                        .map(ticket -> "ACTIVE".equals(ticket.getStatus()))
                        .orElse(false));
    }

    @Override
    @Transactional
    public Ticket markTicketAsUsed(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .map(ticket -> {
                    ticket.setStatus("USED");
                    return ticketRepository.save(ticket);
                })
                .orElse(null);
    }
}
