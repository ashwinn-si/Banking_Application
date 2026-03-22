# Digital Banking Application – System Design Document

## 1. Overview

The Digital Banking Application is a backend system designed to simulate real-world banking operations such as account management, secure authentication, and transaction processing. The system focuses on ensuring **data consistency, concurrency safety, and fault tolerance**, similar to production-grade financial systems.

---

## 2. User & Account Management

### 2.1 User Registration

* Users can register using **email and mobile number**
* OTP-based verification is used for authentication
* Each user is uniquely identified by their mobile number

### 2.2 Account Creation

* A single user can create **multiple bank accounts**
* Each account has:

  * Unique account number
  * Balance
  * Version (used for concurrency control)

### 2.3 Signup Bonus

* A **₹100 bonus** is credited **only once per user**
* Bonus is applied only to the **first account created**
* Prevents abuse by restricting bonus to unique users

---

## 3. Authentication System

### 3.1 OTP-Based Login

* OTP is sent to the user’s email during:

  * Login
  * Account creation
* OTP characteristics:

  * Time-bound (valid for ~5 minutes)
  * Stored temporarily in Redis

### 3.2 Dashboard

* After successful login:

  * User is redirected to dashboard
  * Account balance is displayed (fetched via cache/DB)

---

## 4. Transaction System

### 4.1 Supported Operations

* Deposit
* Withdraw
* Transfer between accounts

### 4.2 ACID Compliance

* All transactions are executed within **database transactions**
* Ensures:

  * Atomicity (all or nothing)
  * Consistency (no invalid state)
  * Isolation (no interference)
  * Durability (data persistence)

---

## 5. Concurrency Control (Optimistic Locking)

### 5.1 Problem

* Multiple users or requests may try to update the same account simultaneously
* This can lead to **race conditions** and incorrect balances

### 5.2 Solution

* Each account maintains a **version field**
* Transaction flow:

  1. Read current balance and version
  2. Perform transaction logic
  3. Update only if version matches

### 5.3 Update Logic

* If version matches → transaction succeeds
* If version mismatch → transaction retries

### 5.4 Benefit

* Prevents:

  * Double spending
  * Inconsistent balance updates

---

## 6. Idempotency Handling

### 6.1 Problem

* User may click “Pay” multiple times
* Can lead to duplicate transactions

### 6.2 Solution

* Frontend generates a **unique transaction ID (UUID)** before initiating transaction
* Backend checks:

  * If transaction ID exists → return previous result
  * Else → process transaction

### 6.3 Implementation

* Transaction ID stored in database with unique constraint

### 6.4 Benefit

* Ensures **exactly-once execution**
* Prevents duplicate financial operations

---

## 7. Redis Integration (Caching Layer)

### 7.1 Purpose

* Improve performance for read-heavy operations

### 7.2 Usage

* Cache account balance for dashboard display

### 7.3 Read Flow

* Check Redis
* If miss → fetch from DB → update Redis

### 7.4 Write Flow

* Always update DB first
* Then update Redis cache

### 7.5 Note

* Redis is **not used for transaction consistency**
* Database remains the source of truth

---

## 8. Transaction Lifecycle

### 8.1 Pre-Transaction

* Frontend generates transaction ID
* Backend validates request

### 8.2 Execution

* Transaction begins (DB transaction)
* Concurrency control applied (version check)
* Business logic executed

### 8.3 Post-Transaction

* Transaction committed if successful
* Redis cache updated
* Response returned to user

---

## 9. Key Design Principles

* **Scalability:** Supports multiple users and concurrent transactions
* **Consistency:** Ensures accurate financial data using ACID properties
* **Reliability:** Prevents duplicate and failed transactions
* **Performance:** Optimized using Redis caching
* **Security:** OTP-based authentication system

---

## 10. Technologies Used

* Backend: Spring Boot
* Database: MySQL / PostgreSQL
* Cache: Redis
* Authentication: OTP (Email-based)
* Deployment: AWS EC2 (optional)
* Containerization: Docker (optional)

---

## 11. Conclusion

This system demonstrates the implementation of a **real-world banking backend**, incorporating advanced concepts such as **optimistic locking, idempotency, caching, and transactional integrity**. It reflects production-level design considerations required in financial systems.

---
