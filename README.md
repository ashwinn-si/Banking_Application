# Banking Application Backend

This repository contains the backend service for a Banking Application built with Spring Boot.

It provides authentication, account management, and transaction workflows that power the mobile/web frontend.

## Application Overview

The backend is designed around three core domains:

- Authentication and authorization
- User bank accounts
- Financial transactions

The system supports both regular users and admins.

## Core Features

### 1. Authentication

- User signup with email, phone number, and profile details
- User login (JWT-based)
- Admin login (JWT-based)
- Protected routes for authenticated users
- Extra admin protection for admin APIs

### 2. Account Management

- Create account for logged-in user
- List all accounts mapped to logged-in user
- Fetch single account details
- Account ownership validation
- Initial bonus balance for first account (10000)

### 3. Transaction Management

- Start + confirm transaction pattern (two-step flow)
- Withdraw money from account
- Transfer money between accounts
- Deposit money (admin-only)
- Fetch transaction details
- Fetch paginated transaction history for an account

### 4. Business Rules and Safety

- JWT token validation for secured endpoints
- Idempotency-style transaction state checks
- Transaction expiry window (5 minutes)
- Insufficient balance checks
- Blocked-account checks
- Role-based access checks for admin routes

### 5. Error Handling and API Contract

- Consistent response wrapper:
  - data
  - message
  - success
- Global exception handling for validation and business errors
- Standard HTTP status codes for client handling


#### Transaction Service 
The `TranscationService` class improve performance and maintainability. Below are the key changes:

1. **Caching Mechanism**:
   - Introduced a caching mechanism to store and reuse transaction data, reducing redundant database queries.
   - Added methods `getCachedTransaction` and `updateTransactionCache` to manage the cache.

2. **Transaction Updates**:
   - Refactored methods to update transaction status, comments, and amounts (`updateTransactionStatus`, `updateTransactionComment`, `updateTransactionAmount`).
   - These methods now utilize the caching mechanism for efficiency.

3. **Deposit, Withdraw, and Transfer Operations**:
   - Enhanced the `deposit`, `withdraw`, and `transfer` methods to handle transactions more robustly.
   - Added validation for transaction types and account statuses.

4. **Error Handling**:
   - Improved error handling to ensure consistency in transaction states.
   - Added custom error messages for better debugging.

5. **Code Organization**:
   - Organized methods logically for better readability and maintainability.

## User Roles

### User

- Signup and login
- Create and view own accounts
- Start and complete withdraw/transfer
- View account transaction history

### Admin

- Admin login
- Start and complete deposit transactions

## Suggested Frontend Screens

### Public

- Login
- Signup
- Admin Login

### User

- Dashboard
- Accounts List
- Account Detail
- Transaction History (paginated)
- Withdraw (start + confirm)
- Transfer (start + confirm)
- Transaction Detail

### Admin

- Admin Dashboard
- Deposit (start + confirm)

### Shared

- Unauthorized / Session Expired
- Error / Not Found

## API Groups

The backend APIs are grouped as:

- /api/auth/\*
- /api/account/\*
- /api/transaction/\*
- /api/transaction/admin/\* (admin only)

A detailed API contract with request/response examples is available in FE_README.md.

## Tech Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- Lombok

## Local Configuration

Environment values are loaded from .env and application.properties.

Required values:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET
- SALT
- JWT_EXPIRATION_TIME

## Development Seed Data

On startup, the app seeds default user/admin credentials in development if they do not exist.

Use these only for local testing.

# Banking Application

## Overview
The Banking Application is a robust and secure platform designed to manage user accounts, transactions, and authentication processes. It leverages modern technologies to ensure scalability, security, and maintainability.

## Features

### 1. Business Logic
- **Account Management**: Create, retrieve, and manage user accounts.
- **Transaction Management**: Start, update, and retrieve transactions.
- **Authentication**: User and admin login, signup, and OTP-based verification.

### 2. Security
- **Authentication and Authorization**:
  - JWT-based authentication for secure API access.
  - Role-based access control with custom filters (`JwtFilter`, `AdminFilter`).
- **Password Security**:
  - Passwords are securely hashed using `BCryptPasswordEncoder` with a configurable salt.
- **CORS Configuration**:
  - Allows specific origins for frontend integration.

### 3. Transactional Management
- Ensures atomicity and consistency during database operations using `@Transactional` annotations.

### 4. Technologies Used
- **Backend**: Spring Boot, Spring Security, Spring Data JPA.
- **Database**: Relational database (e.g., PostgreSQL, MySQL).
- **Validation**: Input validation using `@Valid` annotations.

## Future Enhancements
- Implement caching mechanisms for improved performance.
- Add production-ready configurations for CORS and security settings.

## How to Use
- Clone the repository.
- Configure the database connection in `application.properties`.
- Build and run the application using Maven.

---
This README provides a high-level overview of the application and its features. For detailed documentation, refer to the source code and comments.
