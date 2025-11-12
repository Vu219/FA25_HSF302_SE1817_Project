package fa25.group.evtrainticket.mapper;

import fa25.group.evtrainticket.dto.BookingResponseDto;
import fa25.group.evtrainticket.entity.Booking;
import fa25.group.evtrainticket.entity.Payment;
import fa25.group.evtrainticket.entity.Schedule;
import fa25.group.evtrainticket.entity.Ticket;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {
    public BookingResponseDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponseDto dto = new BookingResponseDto();
        dto.setBookingId(booking.getBookingID());
        dto.setBookingCode(booking.getBookingCode());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStatus(booking.getStatus());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setNotes(booking.getNotes());

        // Add user information
        if (booking.getUser() != null) {
            dto.setUserFullName(booking.getUser().getFullName());
            dto.setUserEmail(booking.getUser().getEmail());
        }

        // Map tickets to TicketDto
        if (booking.getTickets() != null) {
            dto.setTickets(booking.getTickets().stream()
                .map(this::toTicketDto)
                .collect(Collectors.toList()));
        }

        // Map payments to PaymentDto
        if (booking.getPayments() != null) {
            dto.setPayments(booking.getPayments().stream()
                .map(this::toPaymentDto)
                .collect(Collectors.toList()));
        }

        return dto;
    }

    public List<BookingResponseDto> toDtoList(List<Booking> bookings) {
        if (bookings == null) {
            return null;
        }
        return bookings.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private BookingResponseDto.TicketDto toTicketDto(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        BookingResponseDto.TicketDto dto = new BookingResponseDto.TicketDto();
        dto.setTicketId(ticket.getTicketID());
        dto.setTicketCode(ticket.getTicketCode());
        dto.setStatus(ticket.getStatus());
        dto.setPrice(ticket.getPrice());

        // --- [QUAN TRỌNG] THÊM 2 DÒNG NÀY ---
        dto.setPassengerName(ticket.getPassengerName()); // Map tên hành khách
        dto.setTicketType(ticket.getTicketType());       // Map loại vé
        // ------------------------------------

        // Add seat information
        if (ticket.getSeat() != null) {
            dto.setSeatNumber(ticket.getSeat().getSeatNumber());

            if (ticket.getSeat().getCarriage() != null) {
                dto.setCarriageName("Toa " + ticket.getSeat().getCarriage().getCarriageNumber());
            }

            if (ticket.getSeat().getSeatType() != null) {
                dto.setSeatType(ticket.getSeat().getSeatType().getTypeName());
            }
        }

        if (ticket.getSchedule() != null) {
            dto.setSchedule(toScheduleDto(ticket.getSchedule()));
        }

        return dto;
    }
    private BookingResponseDto.PaymentDto toPaymentDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        BookingResponseDto.PaymentDto dto = new BookingResponseDto.PaymentDto();
        dto.setPaymentId(payment.getPaymentID());
        dto.setTransactionCode(payment.getTransactionCode());
        dto.setStatus(payment.getStatus());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setNotes(payment.getNotes());

        return dto;
    }

    private BookingResponseDto.ScheduleDto toScheduleDto(Schedule schedule) {
        if (schedule == null) {
            return null;
        }

        BookingResponseDto.ScheduleDto dto = new BookingResponseDto.ScheduleDto();
        dto.setScheduleId(schedule.getScheduleID());
        dto.setDepartureTime(schedule.getDepartureTime());
        dto.setArrivalTime(schedule.getArrivalTime());
        if (schedule.getDepartureStation() != null) {
            dto.setOrigin(schedule.getDepartureStation().getName());
        }
        if (schedule.getArrivalStation() != null) {
            dto.setDestination(schedule.getArrivalStation().getName());
        }

        // Add train name
        if (schedule.getTrain() != null) {
            dto.setTrainName(schedule.getTrain().getTrainName());
        }

        return dto;
    }
}
