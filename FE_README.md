# Banking Application Backend Guide (for Frontend Development)

This document is the current frontend contract for the Spring Boot backend.

## 1. Scope

Backend features:

- User signup and login (OTP-based)
- Admin login
- Account creation and listing
- Transaction history
- Transaction flows:
- Withdraw (OTP-protected)
- Transfer (OTP-protected)
- Deposit (admin-only)
- OTP resend support

## 2. Tech Stack

- Java + Spring Boot
- Spring Security (JWT)
- Spring Data JPA + MySQL
- Lombok

## 3. Base URL and Environment

- Local base URL: http://localhost:8080
- Allowed origins include:
- http://localhost:5173
- https://bank-application-front-end.vercel.app
- Config sources:
- src/main/resources/application.properties
- .env

Required env vars:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET
- SALT
- JWT_EXPIRATION_TIME

## 4. Authentication and Security Notes

JWT header format:

- Authorization: Bearer <token>

JWT claims include:

- userId
- role (USER or ADMIN)

Important current behavior:

- Security config explicitly permits /api/auth/\*\*.
- Other endpoints are authenticated unless backend security is expanded.
- Frontend should treat /api/otp/\* as part of auth/verification flow.

## 5. Standard Response Wrapper

All responses use:

```json
{
  "data": {},
  "message": "string",
  "success": true
}
```

Error wrapper uses same shape with success=false and data=null.

## 6. Critical Frontend Rule

- Never show actionId in UI, logs visible to users, links, or query params.
- actionId is an internal transient token for OTP verification only.

## 7. Auth APIs

## 7.1 POST /api/auth/signup

Creates user and returns actionId for OTP verification.

Request:

```json
{
  "email": "user@example.com",
  "password": "123456",
  "phoneNumber": "9444133344",
  "name": "Ash",
  "address": "Chennai"
}
```

Success:

```json
{
  "data": {
    "actionId": "uuid"
  },
  "message": "Account Created OTP generated Kindly check email",
  "success": true
}
```

## 7.2 POST /api/auth/login

Starts login and returns actionId for OTP verification.

Validation notes:

- email format validation exists
- password length validation: 1..8
- phoneNumber optional, length 10 if provided

Request:

```json
{
  "email": "user@example.com",
  "password": "123456",
  "phoneNumber": "9444133344"
}
```

Success:

```json
{
  "data": {
    "actionId": "uuid"
  },
  "message": "Otp generated for login Kindly Check",
  "success": true
}
```

## 7.3 POST /api/otp/login

Verifies login OTP and returns JWT.

Request:

```json
{
  "actionId": "uuid",
  "otp": 123456
}
```

Success:

```json
{
  "data": {
    "token": "<jwt-token>"
  },
  "message": "Login Successful",
  "success": true
}
```

## 7.4 POST /api/otp/signup

Verifies signup OTP and activates account.

Request:

```json
{
  "actionId": "uuid",
  "otp": 123456
}
```

Success:

```json
{
  "data": null,
  "message": "Signup Successfull",
  "success": true
}
```

## 7.5 POST /api/otp/resend

Resends OTP by actionId. Same endpoint for auth OTP and transaction OTP.

Request:

```json
{
  "actionId": "uuid"
}
```

Success:

```json
{
  "data": null,
  "message": "OTP sent successfully",
  "success": true
}
```

## 7.6 POST /api/auth/login-admin

Admin login remains token-based.

Request:

```json
{
  "email": "admin@example.com",
  "password": "root"
}
```

Success:

```json
{
  "data": {
    "token": "<jwt-token>"
  },
  "message": "Login successful Admin",
  "success": true
}
```

## 8. Account APIs

## 8.1 GET /api/account/get-all

Returns all accounts mapped to logged-in user.

## 8.2 GET /api/account/get/{accountId}

Returns one account if mapped to user.

## 8.3 POST /api/account/add

Creates account for logged-in user.

All account endpoints use JWT auth.

## 9. Transaction APIs

Transaction flow is now OTP-aware for withdraw and transfer.

## 9.1 GET /api/transaction/get/{transactionId}

Returns transaction details.

## 9.2 GET /api/transaction/get-all/{accountId}?page=1&size=10

Returns paginated transaction list in:

- data.data
- data.currPage
- data.size
- data.totalPages

## 9.3 POST /api/transaction/start-withdraw

Starts withdraw and returns both transaction id and actionId.

Request:

```json
{
  "accountId": "uuid",
  "receiverAccountId": null
}
```

Success data:

```json
{
  "transcationId": "uuid",
  "actionId": "uuid"
}
```

## 9.4 POST /api/transaction/withdraw

Confirm withdraw with OTP.

Request:

```json
{
  "senderAccountId": "uuid",
  "amount": 500,
  "transactionId": "uuid",
  "receiverAccountId": null,
  "actionId": "uuid",
  "otp": 123456
}
```

## 9.5 POST /api/transaction/start-transfer

Starts transfer and returns both transaction id and actionId.

Request:

```json
{
  "accountId": "uuid",
  "receiverAccountId": "uuid"
}
```

Success data:

```json
{
  "transcationId": "uuid",
  "actionId": "uuid"
}
```

## 9.6 POST /api/transaction/transfer

Confirm transfer with OTP.

Request:

```json
{
  "senderAccountId": "uuid",
  "receiverAccountId": "uuid",
  "amount": 500,
  "transactionId": "uuid",
  "actionId": "uuid",
  "otp": 123456
}
```

## 9.7 POST /api/transaction/admin/start-deposit

Starts admin deposit.

Request:

```json
{
  "accountId": "uuid",
  "receiverAccountId": null
}
```

Success data includes `transcationId`. `actionId` may be null for deposit starts.

## 9.8 POST /api/transaction/admin/deposit

Confirms admin deposit.

Request:

```json
{
  "senderAccountId": "uuid",
  "amount": 1000,
  "transactionId": "uuid",
  "receiverAccountId": null
}
```

## 10. Frontend UX Checklist

1. User login is now 2-step: login -> OTP verify.
2. User signup is now 2-step: signup -> OTP verify.
3. Add resend OTP in auth OTP screens.
4. Withdraw is 2-step with OTP at confirm.
5. Transfer is 2-step with OTP at confirm.
6. Add resend OTP in withdraw/transfer OTP step.
7. Keep actionId internal only.
8. Store JWT after OTP login success.
9. Keep admin flow unchanged.

## 11. Important Error Messages to Handle

- Incorrect OTP
- OTP Attempts Exceeded
- OTP attempts exceeded. Account blocked
- OTP Expired
- Invalid OTP action
- Unauthorized OTP for this account
- OTP already verified. Resend is not allowed

## 12. Known Contract Quirks

1. Response key typo remains: `transcationId`.
2. Class naming typo remains in codebase: `TranscationService`.
3. Continue consuming backend fields exactly as returned.

## 13. Dev Seed Users

Default development users may be auto-seeded:

- user: admin@gmail.com / root
- admin: admin@gmail.com / root

For local testing only.
