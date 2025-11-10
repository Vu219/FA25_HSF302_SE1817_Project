# EVTrainTicket Booking, Ticket, and Payment Flow

This document outlines the entire process from searching for a train to booking a ticket, making a payment, and finally using the ticket for travel. It includes code snippets from the controllers and services to explain the implementation.

## High-Level Flow

1.  **Search and Select:** The user searches for a train schedule and selects available seats.
2.  **Passenger Information:** The user enters their details (name, email, etc.).
3.  **Create Pending Booking:** The system creates a `Booking` with a `PENDING` status, temporarily reserving the seats.
4.  **Payment:** The user makes a payment.
5.  **Confirmation:** Upon successful payment, the `Booking` is `CONFIRMED`, and the `Ticket`s are `ACTIVE`.
6.  **Ticket Usage:** The user's `Ticket` is marked as `USED` at the time of travel.

## Detailed Step-by-Step Flow with Code

### 1. Search and Seat Selection

- The user selects a departure station, arrival station, and date.
- The system displays a list of available schedules.
- The user selects a schedule, and the system shows the seat layout for the train.
- The user selects one or more available seats.

### 2. Passenger Information

- The user is directed to a page to enter their information.
- This information is used to create a `User` record if they are not logged in, or to associate the booking with their existing account.

### 3. Creating a Pending Booking

- After submitting their information, a `POST` request is sent to `/api/booking/create-pending`.
- The `BookingController` handles this request.

**`BookingController.java`**
```java
@PostMapping("/api/booking/create-pending")
@ResponseBody
public ResponseEntity<?> createPendingBooking(@RequestBody BookingRequestDto bookingRequest, HttpSession session) {
    try {
        var booking = bookingService.createAnonymousPendingBooking(bookingRequest);
        var response = bookingMapper.toDto(booking);

        // Store booking code in session for payment flow
        session.setAttribute("currentBookingCode", booking.getBookingCode());

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Booking creation failed: " + e.getMessage());
    }
}
```

- The controller calls the `bookingService.createAnonymousPendingBooking()` method.

**`BookingServiceImpl.java`**
```java
@Override
@Transactional
public Booking createAnonymousPendingBooking(BookingRequestDto bookingRequest) {
    try {
        // 1. Create or find anonymous user
        User anonymousUser = createOrFindAnonymousUser(bookingRequest);

        // 2. Get schedule
        Schedule schedule = scheduleRepository.findById(bookingRequest.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + bookingRequest.getScheduleId()));

        // 3. Validate seat availability and calculate price
        List<Seat> selectedSeats = new ArrayList<>();
        double totalAmount = 0.0;
        for (Integer seatId : bookingRequest.getSelectedSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId));
            if (!seat.getIsAvailable()) {
                throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
            }
            selectedSeats.add(seat);
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
        booking = bookingRepository.save(booking);

        // 5. Create tickets and mark seats as unavailable
        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSchedule(schedule);
            ticket.setSeat(seat);
            // ... set price and other details
            ticket.setStatus("PENDING");
            tickets.add(ticket);

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
```
- This service method:
    - Creates a new `Booking` entity with a `PENDING` status.
    - For each selected seat, it creates a `Ticket` entity with a `PENDING` status, linking it to the booking, schedule, and seat.
    - It calculates the `totalAmount` for the booking.
    - It saves the `Booking` and the associated `Ticket`s to the database.
    - The `bookingCode` of the new booking is stored in the user's session.

### 4. Payment Process

- The user is redirected to the payment page, which displays the booking details.
- The user initiates the payment, which triggers a `POST` request to `/api/payment/complete-session`.

**`PaymentController.java`**
```java
@PostMapping("/complete-session")
public ResponseEntity<?> completePaymentFromSession(HttpSession session) {
    try {
        String bookingCode = (String) session.getAttribute("currentBookingCode");
        // ... error handling ...

        Booking booking = bookingService.getBookingByCode(bookingCode);
        // ... error handling ...

        // Activate all tickets for this booking
        List<Ticket> tickets = ticketService.activateTicketsForBooking(bookingCode);

        // Create Payment entity
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod("DEMO_PAYMENT");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionCode(generateTransactionCode());
        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);

        // Update booking status
        booking.setStatus("CONFIRMED");
        bookingService.updateBooking(booking);

        // Clear booking from session
        session.removeAttribute("currentBookingCode");

        // ... create and return response ...
    } catch (Exception e) {
        // ... error handling ...
    }
}
```
- The `PaymentController` handles this request by:
    1.  Retrieving the `bookingCode` from the session.
    2.  Fetching the `Booking` from the database.
    3.  Calling `ticketService.activateTicketsForBooking(bookingCode)`.
    4.  Creating a new `Payment` entity with a `COMPLETED` status.
    5.  Updating the `Booking` status to `CONFIRMED`.
    6.  Removing the `bookingCode` from the session.

**`TicketServiceImpl.java`**
```java
@Override
@Transactional
public List<Ticket> activateTicketsForBooking(String bookingCode) {
    List<Ticket> tickets = ticketRepository.findByBookingBookingCode(bookingCode);

    if (!tickets.isEmpty()) {
        // Update booking status to COMPLETED
        Booking booking = tickets.get(0).getBooking();
        booking.setStatus("COMPLETED"); // Note: In PaymentController, it's set to CONFIRMED. This might be an inconsistency.

        // Activate all tickets
        tickets.forEach(ticket -> ticket.setStatus("ACTIVE"));
        return ticketRepository.saveAll(tickets);
    }

    return tickets;
}
```
The `activateTicketsForBooking` method finds all tickets for the given booking code and updates their status from `PENDING` to `ACTIVE`.

### 5. Booking Confirmation

- After the payment is completed, the user is redirected to a confirmation page.
- This page displays the booking details, confirming that the payment was successful and the tickets are active.

### 6. Ticket Management and Usage

- The user can view their booking history and ticket details.
- The `TicketController` provides endpoints for ticket management.

**`TicketController.java`**
```java
@GetMapping("/check-active/{ticketCode}")
@ResponseBody
public ResponseEntity<?> checkTicketActive(@PathVariable String ticketCode) {
    // ... implementation ...
}

@GetMapping("/details/{ticketCode}")
@ResponseBody
public ResponseEntity<?> getTicketDetails(@PathVariable String ticketCode) {
    // ... implementation ...
}

@PutMapping("/mark-used/{ticketCode}")
@ResponseBody
public ResponseEntity<?> markTicketAsUsed(@PathVariable String ticketCode) {
    try {
        Ticket ticket = ticketService.markTicketAsUsed(ticketCode);
        // ... return success response ...
    } catch (Exception e) {
        // ... return error response ...
    }
}
```
- `markTicketAsUsed` is called by staff to update the ticket's status to `USED`.

**`TicketServiceImpl.java`**
```java
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
```

## Entity Status Lifecycle

### Booking Status

- `PENDING`: The booking has been created, but not yet paid for. Seats are reserved.
- `CONFIRMED`: The payment has been successfully completed.
- `CANCELLED`: The booking has been cancelled.
- `COMPLETED`: The travel date has passed.

### Ticket Status

- `PENDING`: The ticket has been created as part of a `PENDING` booking. It is not yet valid for travel.
- `ACTIVE`: The ticket has been paid for and is valid for travel.
- `USED`: The ticket has been used for travel (e.g., scanned at the gate).
- `CANCELLED`: The ticket has been cancelled.
- `EXPIRED`: The travel date has passed and the ticket was not used.