# 🚀 Simplified Payment System - Postman Testing Guide

## ✅ Status: API Implementation Complete
The payment system has been fully implemented and is ready for testing! All endpoints now return proper JSON responses.

## Overview
The payment system has been simplified to remove payment forms and integrate easily with external payment merchants. When users click "pay", the system sets ticket status to ACTIVE and provides the ticket information.

## 📋 Available API Endpoints

### 1. Create Pending Booking ✅ IMPLEMENTED
Creates a booking with PENDING status, waiting for payment completion.

```
POST /api/booking/create-pending
Content-Type: application/json

{
    "scheduleId": 1,
    "selectedSeatIds": [1, 2],
    "ticketType": "ONE_WAY",
    "userFullName": "John Doe",
    "userEmail": "john@example.com",
    "userPhone": "1234567890",
    "notes": "Test booking"
}
```

**Response Example:**
```json
{
    "bookingCode": "BK20251028001",
    "status": "PENDING",
    "totalAmount": 700000.0,
    "bookingDate": "2025-10-28T23:15:00",
    "tickets": [
        {
            "ticketCode": "TK20251028001",
            "seatNumber": "1A",
            "status": "PENDING",
            "price": 350000.0
        },
        {
            "ticketCode": "TK20251028002", 
            "seatNumber": "1B",
            "status": "PENDING",
            "price": 350000.0
        }
    ]
}
```

### 2. Complete Payment (Simulate Payment Merchant) ✅ IMPLEMENTED
This endpoint simulates what a payment merchant would call after successful payment.

```
POST /api/payment/complete/{bookingCode}
```

**Example:** `POST /api/payment/complete/BK20251028001`

**Response Example:**
```json
{
    "success": true,
    "message": "Payment completed and tickets activated successfully",
    "bookingCode": "BK20251028001",
    "ticketCount": 2,
    "bookingStatus": "CONFIRMED",
    "ticketCodes": ["TK20251028001", "TK20251028002"]
}
```

### 3. Check Payment Status ✅ IMPLEMENTED
Check the current status of a booking and its tickets.

```
GET /api/payment/status/{bookingCode}
```

**Example:** `GET /api/payment/status/BK20251028001`

**Response Example:**
```json
{
    "bookingCode": "BK20251028001",
    "bookingStatus": "CONFIRMED", 
    "totalAmount": 700000.0,
    "ticketCount": 2,
    "allTicketsActive": true,
    "paymentCompleted": true
}
```

### 4. Get Booking Details ✅ IMPLEMENTED
Retrieve complete booking information.

```
GET /api/booking/{bookingCode}
```

**Example:** `GET /api/booking/BK20251028001`

## 🧪 Testing Workflow

### Step 1: Create a Test Booking
1. Use the create-pending endpoint
2. Save the returned `bookingCode` for next steps
3. Verify status is "PENDING" and tickets are "PENDING"

### Step 2: Simulate Payment Completion
1. Use the complete payment endpoint with the booking code
2. This simulates what your payment merchant would do
3. Verify tickets are now "ACTIVE" and booking is "CONFIRMED"

### Step 3: Verify Payment Status
1. Use the status endpoint to check final state
2. Confirm `paymentCompleted: true` and `allTicketsActive: true`

## 🌐 Web Interface for Testing
Visit: `http://localhost:8080/payment-test-simple`

This page provides a user-friendly interface for testing all the APIs without Postman.

## 📊 Sample Test Data
The application starts with pre-populated data:

- **Stations:** Hanoi, Ho Chi Minh City, Da Nang, Hue, Nha Trang
- **Trains:** SE1, SE3, SE7 with different carriages and seats
- **Schedules:** Multiple routes between major cities
- **Seats:** Various types (Economy, Business, VIP, Sleeper)

### Available Schedule IDs for Testing:
- Schedule ID 1: Hanoi → Ho Chi Minh City 
- Schedule ID 2: Ho Chi Minh City → Hanoi
- Schedule ID 3: Hanoi → Da Nang
- Schedule ID 4: Da Nang → Ho Chi Minh City
- Schedule ID 5: Hanoi → Hue

### Available Seat IDs:
- Seats 1-10: Economy class
- Seats 11-20: Business class  
- Seats 21-30: VIP class
- Seats 31-40: Sleeper class

## ⚡ Quick Test Commands

### Create Booking (Copy-paste ready)
```bash
curl -X POST http://localhost:8080/api/booking/create-pending \
  -H "Content-Type: application/json" \
  -d '{
    "scheduleId": 1,
    "selectedSeatIds": [1, 2],
    "ticketType": "ONE_WAY",
    "userFullName": "Test User",
    "userEmail": "test@example.com", 
    "userPhone": "1234567890",
    "notes": "Postman test booking"
  }'
```

### Complete Payment (Replace with actual booking code)
```bash
curl -X POST http://localhost:8080/api/payment/complete/BK20251028001
```

### Check Status (Replace with actual booking code)
```bash
curl -X GET http://localhost:8080/api/payment/status/BK20251028001
```

## 🔧 Integration Notes

### For Payment Merchant Integration:
1. Create pending booking first
2. Redirect user to payment merchant with booking code
3. Payment merchant calls `/api/payment/complete/{bookingCode}` after successful payment
4. System automatically activates tickets and updates booking status
5. User receives ticket confirmation

### Error Handling:
- Invalid booking codes return 400 with error message
- Bookings not in PENDING status cannot be completed
- All API responses include success/error indicators

## 🚀 Application Status
- **Server:** Running on http://localhost:8080
- **Database:** SQL Server with auto-generated test data
- **Status:** Ready for testing

Start testing by creating a pending booking and then completing the payment flow!
