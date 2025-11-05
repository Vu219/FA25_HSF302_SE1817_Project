package fa25.group.evtrainticket.controller;

import fa25.group.evtrainticket.service.TicketService;
import fa25.group.evtrainticket.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    /**
     * Check if a ticket is active by ticket code
     */
    @GetMapping("/check-active/{ticketCode}")
    @ResponseBody
    public ResponseEntity<?> checkTicketActive(@PathVariable String ticketCode) {
        try {
            boolean isActive = ticketService.isTicketActiveByCode(ticketCode);
            Ticket ticket = ticketService.getTicketByCode(ticketCode);

            return ResponseEntity.ok(Map.of(
                "ticketCode", ticketCode,
                "isActive", isActive,
                "status", ticket.getStatus(),
                "message", isActive ? "Ticket is active and valid for travel" : "Ticket is not active"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "ticketCode", ticketCode,
                "isActive", false,
                "message", "Ticket not found: " + e.getMessage()
            ));
        }
    }

    /**
     * Get ticket details by ticket code
     */
    @GetMapping("/details/{ticketCode}")
    @ResponseBody
    public ResponseEntity<?> getTicketDetails(@PathVariable String ticketCode) {
        try {
            Ticket ticket = ticketService.getTicketByCode(ticketCode);
            Map<String, Object> response = new HashMap<>();
            response.put("ticketId", ticket.getTicketID());
            response.put("ticketCode", ticket.getTicketCode());
            response.put("status", ticket.getStatus());
            response.put("isActive", ticketService.isTicketActiveByCode(ticketCode));
            response.put("bookingCode", ticket.getBooking().getBookingCode());
            response.put("seatNumber", ticket.getSeat().getSeatNumber());
            response.put("carriageName", "Carriage " + ticket.getSeat().getCarriage().getCarriageNumber());
            response.put("seatType", ticket.getSeat().getSeatType().getTypeName());
            response.put("price", ticket.getPrice());
            response.put("ticketType", ticket.getTicketType());
            response.put("trainName", ticket.getSchedule().getTrain().getTrainName());
            response.put("departureStation", ticket.getSchedule().getDepartureStation().getName());
            response.put("arrivalStation", ticket.getSchedule().getArrivalStation().getName());
            response.put("departureTime", ticket.getSchedule().getDepartureTime());
            response.put("arrivalTime", ticket.getSchedule().getArrivalTime());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Ticket not found: " + e.getMessage()
            ));
        }
    }

    /**
     * Check multiple tickets at once
     */
    @PostMapping("/check-multiple")
    @ResponseBody
    public ResponseEntity<?> checkMultipleTickets(@RequestBody List<String> ticketCodes) {
        try {
            boolean allValid = ticketService.areTicketsValidForTravel(ticketCodes);

            List<Map<String, Object>> ticketStatuses = ticketCodes.stream()
                .map(code -> {
                    Map<String, Object> ticketInfo = new HashMap<>();
                    try {
                        Ticket ticket = ticketService.getTicketByCode(code);
                        boolean isActive = ticketService.isTicketActiveByCode(code);
                        ticketInfo.put("ticketCode", code);
                        ticketInfo.put("status", ticket.getStatus());
                        ticketInfo.put("isActive", isActive);
                        ticketInfo.put("seatNumber", ticket.getSeat().getSeatNumber());
                    } catch (Exception e) {
                        ticketInfo.put("ticketCode", code);
                        ticketInfo.put("status", "NOT_FOUND");
                        ticketInfo.put("isActive", false);
                        ticketInfo.put("error", e.getMessage());
                    }
                    return ticketInfo;
                })
                .toList();

            return ResponseEntity.ok(Map.of(
                "allValid", allValid,
                "tickets", ticketStatuses,
                "message", allValid ? "All tickets are valid for travel" : "Some tickets are not valid"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error checking tickets: " + e.getMessage()
            ));
        }
    }

    /**
     * Mark ticket as used (for boarding)
     */
    @PutMapping("/mark-used/{ticketCode}")
    @ResponseBody
    public ResponseEntity<?> markTicketAsUsed(@PathVariable String ticketCode) {
        try {
            Ticket ticket = ticketService.markTicketAsUsed(ticketCode);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket marked as used successfully",
                "ticketCode", ticket.getTicketCode(),
                "status", ticket.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to mark ticket as used: " + e.getMessage()
            ));
        }
    }

    /**
     * Get tickets for a booking
     */
    @GetMapping("/booking/{bookingId}")
    @ResponseBody
    public ResponseEntity<?> getTicketsByBooking(@PathVariable(name = "bookingId") Integer bookingId) {
        try {
            List<Ticket> tickets = ticketService.getTicketsByBookingId(bookingId);

            List<Map<String, Object>> ticketDetails = tickets.stream()
                .map(ticket -> {
                    Map<String, Object> ticketInfo = new HashMap<>();
                    ticketInfo.put("ticketId", ticket.getTicketID());
                    ticketInfo.put("ticketCode", ticket.getTicketCode());
                    ticketInfo.put("status", ticket.getStatus());
                    ticketInfo.put("isActive", ticketService.isTicketActive(ticket.getTicketID()));
                    ticketInfo.put("seatNumber", ticket.getSeat().getSeatNumber());
                    ticketInfo.put("carriageName", "Carriage " + ticket.getSeat().getCarriage().getCarriageNumber());
                    ticketInfo.put("seatType", ticket.getSeat().getSeatType().getTypeName());
                    ticketInfo.put("price", ticket.getPrice());
                    ticketInfo.put("ticketType", ticket.getTicketType());
                    return ticketInfo;
                })
                .toList();

            return ResponseEntity.ok(Map.of(
                "bookingId", bookingId,
                "ticketCount", tickets.size(),
                "tickets", ticketDetails
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error retrieving tickets: " + e.getMessage()
            ));
        }
    }
}
