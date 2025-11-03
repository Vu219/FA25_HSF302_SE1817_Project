# Payment Flow and Ticket Status Management - Implementation Guide

## Overview
I've implemented a proper payment flow and ticket status management system for your EVTrainTicket application. Here's what has been added:

## 🚀 New Features Implemented

### 1. Payment Status Management
- **PENDING**: Payment initiated but not confirmed
- **COMPLETED**: Payment successfully processed
- **FAILED**: Payment failed
- **CANCELLED**: Payment was cancelled
- **REFUNDED**: Payment was refunded

### 2. Ticket Status Management
- **PENDING**: Ticket created but payment not confirmed
- **ACTIVE**: Ticket is valid for travel (payment confirmed)
- **USED**: Ticket has been used for boarding
- **CANCELLED**: Ticket was cancelled
- **EXPIRED**: Ticket has expired

### 3. New Services Added
- `PaymentService` - Handles all payment operations
- `TicketService` - Manages ticket status and validation
- `PaymentServiceImpl` - Implementation of payment logic
- `TicketServiceImpl` - Implementation of ticket logic

### 4. New Controllers Added
- `PaymentController` - REST API for payment operations
- `TicketController` - REST API for ticket management

## 📋 Updated Booking Flow

### Traditional Flow (Direct Confirmation)
```
POST /api/booking/create
→ Booking: CONFIRMED, Payment: COMPLETED, Tickets: ACTIVE
```

### New Proper Flow (Pending → Paid)
```
1. POST /api/booking/create-pending
   → Booking: PENDING, Tickets: PENDING

2. POST /api/booking/process-payment?bookingCode=BK123&paymentMethod=DEMO_PAYMENT
   → Payment: PENDING → COMPLETED, Booking: CONFIRMED, Tickets: ACTIVE
```

## 🔍 API Endpoints

### Booking Endpoints
- `POST /api/booking/create-pending` - Create booking with pending status
- `POST /api/booking/process-payment` - Process payment for booking
- `POST /api/booking/confirm-payment` - Confirm payment by transaction code

### Ticket Management Endpoints
- `GET /api/ticket/check-active/{ticketCode}` - Check if ticket is active
- `GET /api/ticket/details/{ticketCode}` - Get detailed ticket information
- `POST /api/ticket/check-multiple` - Check multiple tickets at once
- `PUT /api/ticket/mark-used/{ticketCode}` - Mark ticket as used (for boarding)
- `GET /api/ticket/booking/{bookingId}` - Get all tickets for a booking

### Payment Management Endpoints
- `GET /api/payment/transaction/{transactionCode}` - Get payment by transaction code
- `GET /api/payment/booking/{bookingId}` - Get payments for a booking
- `POST /api/payment/process` - Process payment for booking ID
- `POST /api/payment/confirm/{transactionCode}` - Confirm payment
- `PUT /api/payment/cancel/{paymentId}` - Cancel/refund payment
- `GET /api/payment/status/{transactionCode}` - Check payment status

## 🧪 Testing the Implementation

### Test Case 1: Check Ticket Status
```bash
# Check if a ticket is active
GET /api/ticket/check-active/TK12345678
Response: {
  "ticketCode": "TK12345678",
  "isActive": true,
  "status": "ACTIVE",
  "message": "Ticket is active and valid for travel"
}
```

### Test Case 2: Create Pending Booking and Process Payment
```bash
# Step 1: Create pending booking
POST /api/booking/create-pending
{
  "scheduleId": 1,
  "selectedSeatIds": [1, 2],
  "ticketType": "ONE_WAY",
  "userFullName": "John Doe",
  "userEmail": "john@example.com",
  "userPhone": "1234567890",
  "notes": "Test booking"
}

# Step 2: Process payment
POST /api/booking/process-payment?bookingCode=BK123456789&paymentMethod=DEMO_PAYMENT

# Step 3: Check ticket status
GET /api/ticket/check-active/TK12345678
```

### Test Case 3: Mark Ticket as Used
```bash
# For boarding system
PUT /api/ticket/mark-used/TK12345678
Response: {
  "success": true,
  "message": "Ticket marked as used successfully",
  "ticketCode": "TK12345678",
  "status": "USED"
}
```

## 💡 Ways to Check if Tickets are Active

### Method 1: Single Ticket Check
```javascript
// Check single ticket
fetch('/api/ticket/check-active/TK12345678')
  .then(response => response.json())
  .then(data => {
    if (data.isActive) {
      console.log('Ticket is valid for travel');
    } else {
      console.log('Ticket is not active: ' + data.message);
    }
  });
```

### Method 2: Multiple Tickets Check
```javascript
// Check multiple tickets at once
fetch('/api/ticket/check-multiple', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(['TK12345678', 'TK87654321'])
})
.then(response => response.json())
.then(data => {
  if (data.allValid) {
    console.log('All tickets are valid for travel');
  } else {
    console.log('Some tickets are invalid');
    data.tickets.forEach(ticket => {
      console.log(`${ticket.ticketCode}: ${ticket.status}`);
    });
  }
});
```

### Method 3: Get Detailed Ticket Information
```javascript
// Get full ticket details
fetch('/api/ticket/details/TK12345678')
  .then(response => response.json())
  .then(ticket => {
    console.log('Ticket Details:', {
      code: ticket.ticketCode,
      status: ticket.status,
      isActive: ticket.isActive,
      train: ticket.trainName,
      seat: ticket.seatNumber,
      departure: ticket.departureTime
    });
  });
```

## 🔄 Status Transition Rules

### Ticket Status Transitions
- `PENDING` → `ACTIVE` (when payment confirmed)
- `PENDING` → `CANCELLED` (when payment cancelled)
- `ACTIVE` → `USED` (when passenger boards)
- `ACTIVE` → `CANCELLED` (when booking cancelled)
- `ACTIVE` → `EXPIRED` (when ticket expires)

### Payment Status Transitions
- `PENDING` → `COMPLETED` (successful payment)
- `PENDING` → `FAILED` (payment failure)
- `PENDING` → `CANCELLED` (payment cancelled)
- `COMPLETED` → `REFUNDED` (when refund processed)

## 🛠️ Integration with Frontend

### For Booking Page
```javascript
// Create pending booking instead of direct booking
function createBooking(bookingData) {
  return fetch('/api/booking/create-pending', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(bookingData)
  });
}

// Process payment after booking creation
function processPayment(bookingCode, paymentMethod = 'DEMO_PAYMENT') {
  return fetch(`/api/booking/process-payment?bookingCode=${bookingCode}&paymentMethod=${paymentMethod}`, {
    method: 'POST'
  });
}
```

### For Ticket Validation (e.g., at station)
```javascript
// Validate ticket before allowing boarding
function validateTicketForBoarding(ticketCode) {
  return fetch(`/api/ticket/check-active/${ticketCode}`)
    .then(response => response.json())
    .then(data => {
      if (data.isActive) {
        // Allow boarding, optionally mark as used
        return fetch(`/api/ticket/mark-used/${ticketCode}`, { method: 'PUT' });
      } else {
        throw new Error(`Ticket is not valid: ${data.message}`);
      }
    });
}
```

## 📝 Key Benefits

1. **Proper Payment Flow**: Bookings are created as PENDING and only confirmed after payment
2. **Ticket Validation**: Easy way to check if tickets are valid for travel
3. **Status Tracking**: Clear status progression for both payments and tickets
4. **Boarding Management**: Ability to mark tickets as used during boarding
5. **Audit Trail**: Complete tracking of payment and ticket status changes
6. **Flexible Integration**: Easy to integrate with external payment gateways later

## 🔮 Future Enhancements

1. **Payment Gateway Integration**: Replace DEMO_PAYMENT with real payment providers
2. **Ticket Expiration**: Automatic ticket expiration based on travel date
3. **Refund Processing**: Automated refund handling
4. **Email Notifications**: Send status updates to customers
5. **QR Code Generation**: Generate QR codes for tickets
6. **Mobile Boarding Pass**: Digital boarding pass functionality

The implementation is now complete and ready for testing!
