# Banking Application Backend Guide (for Frontend Development)

This project is the backend API for a banking application built with Spring Boot.

Use this document as the source of truth for frontend page planning, API integration, and request/response contracts.

## 1. What this backend does

The backend supports:

- User signup and login
- Admin login
- Account creation and account listing
- Account-level transaction history (paginated)
- Transaction execution flows:
  - Withdraw
  - Transfer
  - Deposit (admin-only)

Main architecture:

- Controllers: REST API endpoints
- Services: business logic
- Repositories: database access (JPA)
- Filters: JWT auth + admin authorization

## 2. Tech stack

- Java + Spring Boot
- Spring Security (JWT-based auth)
- Spring Data JPA + MySQL
- Lombok

## 3. Base URL and environment

- Local backend base URL (default): http://localhost:8080
- CORS currently allows frontend origin: http://localhost:5173
- Config comes from:
  - src/main/resources/application.properties
  - .env (imported by Spring)

Required env variables:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET
- SALT
- JWT_EXPIRATION_TIME

## 4. Authentication model

All endpoints except /api/auth/\*\* require JWT token.

Header format:

- Authorization: Bearer <token>

JWT includes:

- userId (UUID)
- role (USER or ADMIN)

Admin-only authorization is applied to any path containing /admin.

## 5. Standard response format

### Success and failure wrapper

All API responses follow this structure:

{
"data": <any or null>,
"message": "string",
"success": true | false
}

### Common error behavior

- Validation errors -> 400
- Type mismatch in params/query -> 400
- Business errors (CustomError) -> defined status (e.g. 401, 404, 409)
- Unexpected errors -> 500

Error body also follows same wrapper with:

- data: null
- success: false

## 6. Suggested frontend pages

These are the natural pages/screens to build from this backend API.

### Public pages

1. User Login
2. User Signup
3. Admin Login

### Authenticated user pages

4. User Dashboard (summary + quick actions)
5. Accounts List
6. Account Details
7. Transaction History (paginated)
8. Start Withdraw + Confirm Withdraw
9. Start Transfer + Confirm Transfer
10. Transaction Details

### Authenticated admin pages

11. Admin Dashboard
12. Start Deposit + Confirm Deposit
13. Transaction Lookup (optional admin utility)

### Shared UX pages/components

14. Unauthorized / Session Expired page
15. Global Error / Not Found page
16. Reusable confirmation modal for transaction second-step APIs

## 7. API endpoints

## 7.1 Auth APIs

### POST /api/auth/signup

Create a new user.

Request body:
{
"email": "user@example.com",
"password": "123456",
"phoneNumber": "9444133344",
"name": "Ash",
"address": "Chennai"
}

Success response (200):
{
"data": null,
"message": "Account Created successfully",
"success": true
}

Possible errors:

- 409 if email or phone already exists

---

### POST /api/auth/login

Login as a normal user.

Notes:

- email is validated as email format
- password length validation is 1..8
- phoneNumber is optional but if provided, must be 10 chars
- service primarily resolves by email when email is provided

Request body:
{
"email": "user@example.com",
"password": "123456",
"phoneNumber": "9444133344"
}

Success response (200):
{
"data": {
"token": "<jwt-token>"
},
"message": "Login successful",
"success": true
}

Possible errors:

- 404 user not found
- 409 incorrect password
- 400 validation failure

---

### POST /api/auth/login-admin

Login as admin.

Request body:
{
"email": "admin@example.com",
"password": "root"
}

Success response (200):
{
"data": {
"token": "<jwt-token>"
},
"message": "Login successful",
"success": true
}

Possible errors:

- 404 admin not found
- 409 incorrect password

## 7.2 Account APIs (JWT required)

### GET /api/account/get-all

Get all accounts mapped to logged-in user.

Headers:

- Authorization: Bearer <jwt-token>

Success response (200):
{
"data": [
{
"accountId": "uuid",
"balance": 10000,
"blocked": false
}
],
"message": "All account details",
"success": true
}

Possible errors:

- 404 user not found
- 401 unauthorized

---

### GET /api/account/get/{accountId}

Get one account (only if mapped to logged-in user).

Path params:

- accountId (UUID)

Success response (200):
{
"data": {
"accountId": "uuid",
"balance": 10000,
"blocked": false
},
"message": "Account details",
"success": true
}

Possible errors:

- 404 account not found
- 401 if account not mapped to this user

---

### POST /api/account/add

Create a new account for logged-in user.

Headers:

- Authorization: Bearer <jwt-token>

Request body:

- none

Success response (200):
{
"data": null,
"message": "Account Created",
"success": true
}

Business note:

- First account may receive initial bonus balance of 10000

## 7.3 Transaction APIs (JWT required)

Transaction flow is two-step:

1. Start transaction -> get transaction id
2. Confirm transaction with amount + transaction id

Time/idempotency behavior:

- Transaction must be completed within 5 minutes
- Reusing non-STARTED transaction id is rejected

Transaction status enum values:

- STARTED
- TRANSCATION_STARTED
- FAILED
- COMPLETED

Transaction type enum values:

- DEPOSIT
- WITHDRAW
- TRANSACTION

### GET /api/transaction/get/{transactionId}

Get transaction details.

Path params:

- transactionId (UUID)

Success response (200):
{
"data": {
"transactionId": "uuid",
"sender": {
"id": "uuid",
"accountId": "uuid",
"name": "Ash",
"phoneNumber": "9444133344",
"email": "user@example.com"
},
"receiver": {
"id": "uuid",
"accountId": "uuid",
"name": "Bob",
"phoneNumber": "9999999999",
"email": "bob@example.com"
},
"amount": 500,
"comments": "",
"transactionType": "TRANSACTION",
"transactionStatus": "COMPLETED",
"createdAt": "2026-03-24T10:15:30"
},
"message": "Transaction Details",
"success": true
}

Notes:

- receiver is null for WITHDRAW and DEPOSIT transactions

---

### GET /api/transaction/get-all/{accountId}?page=1&size=10

Get paginated transactions for an account.

Path params:

- accountId (UUID)

Query params:

- page (1-based)
- size

Success response (200):
{
"data": {
"data": [
{
"transactionId": "uuid",
"senderAccountId": "uuid",
"receiverAccountId": "uuid or null",
"transactionType": "WITHDRAW",
"transactionStatus": "COMPLETED",
"amount": 200,
"createdAt": "2026-03-24T10:15:30"
}
],
"currPage": 1,
"size": 10,
"totalPages": 4
},
"message": "Transaction Details",
"success": true
}

Possible errors:

- 401 if account does not belong to user

---

### POST /api/transaction/start-withdraw

Start a withdraw transaction.

Request body:
{
"accountId": "uuid",
"receiverAccountId": null
}

Success response (200):
{
"data": {
"transcationId": "uuid"
},
"message": "Transaction Started",
"success": true
}

Important:

- Response key is transcationId (typo in backend DTO). Frontend should use this exact key.

---

### POST /api/transaction/withdraw

Confirm withdraw.

Request body:
{
"senderAccountId": "uuid",
"amount": 500,
"transactionId": "uuid",
"receiverAccountId": null
}

Success response (200):
{
"data": null,
"message": "Withdraw Sucessfull",
"success": true
}

Possible errors:

- 400 insufficient balance
- 400 expired transaction / invalid idempotency state

---

### POST /api/transaction/start-transfer

Start a transfer transaction.

Request body:
{
"accountId": "uuid",
"receiverAccountId": "uuid"
}

Success response (200):
{
"data": {
"transcationId": "uuid"
},
"message": "Transaction Started",
"success": true
}

---

### POST /api/transaction/transfer

Confirm transfer.

Request body:
{
"senderAccountId": "uuid",
"receiverAccountId": "uuid",
"amount": 500,
"transactionId": "uuid"
}

Success response (200):
{
"data": null,
"message": "Transfer Successful",
"success": true
}

Possible errors:

- 400 insufficient balance
- 400 expired transaction / invalid idempotency state

---

### POST /api/transaction/admin/start-deposit (admin JWT required)

Start a deposit transaction.

Request body:
{
"accountId": "uuid",
"receiverAccountId": null
}

Success response (200):
{
"data": {
"transcationId": "uuid"
},
"message": "Transaction Started",
"success": true
}

---

### POST /api/transaction/admin/deposit (admin JWT required)

Confirm deposit.

Request body:
{
"senderAccountId": "uuid",
"amount": 1000,
"transactionId": "uuid",
"receiverAccountId": null
}

Success response (200):
{
"data": null,
"message": "Amount Successfully Deposited",
"success": true
}

## 8. Frontend integration checklist

1. Build two auth flows:
   - User login/signup
   - Admin login
2. Store JWT and send Authorization header for protected APIs.
3. Implement route protection for user/admin paths.
4. Implement two-step transaction UX:
   - Start endpoint
   - Confirm endpoint with returned transcationId
5. Handle API wrapper consistently (data, message, success).
6. Build reusable error handling for 400/401/404/409/500.
7. For get-all transactions, handle nested pagination object:
   - response.data.data -> transaction list
   - response.data.currPage, size, totalPages -> pagination meta

## 9. Seeded default users (development)

A startup seeder creates defaults if absent:

- Default user email: admin@gmail.com
- Default user password: root
- Default admin email: admin@gmail.com
- Default admin password: root

Use this only for local/dev testing.

## 10. Notes and caveats for frontend team

- Some DTO/property names contain typos from backend and should be consumed as-is:
  - transcationId (not transactionId) in start transaction response
  - TRANSCATION_STARTED enum value spelling
- Password max length validation in login is currently 8.
- CORS is currently local-only (http://localhost:5173).

If backend contract changes later, this README should be updated first so frontend remains aligned.
