# Test the Booking API

## Test 1: Create Pending Booking

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
    "notes": "Test booking from API"
  }'
```

## Test 2: Complete Payment

Replace `{bookingCode}` with the code from Test 1:

```bash
curl -X POST http://localhost:8080/api/payment/complete/{bookingCode}
```

## Test 3: Check Status

Replace `{bookingCode}` with the code from Test 1:

```bash
curl -X GET http://localhost:8080/api/payment/status/{bookingCode}
```

## Expected Results:

### Test 1 Response:
```json
{
    "bookingCode": "BK20251028001",
    "status": "PENDING",
    "totalAmount": [calculated amount],
    "tickets": [
        {
            "ticketCode": "TK20251028001",
            "seatNumber": "1A",
            "status": "PENDING"
        }
    ]
}
```

### Test 2 Response:
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

### Test 3 Response:
```json
{
    "bookingCode": "BK20251028001",
    "bookingStatus": "CONFIRMED",
    "totalAmount": [amount],
    "ticketCount": 2,
    "allTicketsActive": true,
    "paymentCompleted": true
}
```
