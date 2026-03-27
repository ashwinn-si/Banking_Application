# Frontend Migration Notes (Old Backend -> Current Backend)

This file captures what changed in backend contracts compared to the previous frontend assumptions documented in FE_README.md.

Use this as the implementation checklist for frontend updates.

## Critical UX Rule

- Never display actionId in UI, toast, logs shown to users, or query params.
- actionId must be treated as internal transient state only.

## 1) Major Functional Changes

1. Auth is now OTP-based for user login and signup.
2. Withdraw and transfer are now OTP-protected in their confirmation step.
3. New resend OTP endpoint exists.

Admin login and admin deposit flow remain mostly the same.

## 2) Auth Flow Changes

## Old behavior

- POST /api/auth/login returned JWT token directly.
- POST /api/auth/signup completed account creation directly.

## New behavior

### POST /api/auth/login

- Now returns actionId instead of token.
- Message: "Otp generated for login Kindly Check"
- FE rule: store actionId internally only; do not render it anywhere in UI.

Request body (same):

```json
{
  "email": "user@example.com",
  "password": "123456",
  "phoneNumber": "9444133344"
}
```

Success response data:

```json
{
  "actionId": "uuid"
}
```

### POST /api/auth/signup

- Now creates user in inactive state and returns actionId.
- Message: "Account Created OTP generated Kindly check email"
- FE rule: store actionId internally only; do not render it anywhere in UI.

Request body (same):

```json
{
  "email": "user@example.com",
  "password": "123456",
  "phoneNumber": "9444133344",
  "name": "Ash",
  "address": "Chennai"
}
```

Success response data:

```json
{
  "actionId": "uuid"
}
```

### New OTP verification endpoints for auth

#### POST /api/otp/login

Request:

```json
{
  "actionId": "uuid",
  "otp": 123456
}
```

Response data:

```json
{
  "token": "<jwt-token>"
}
```

#### POST /api/otp/signup

Request:

```json
{
  "actionId": "uuid",
  "otp": 123456
}
```

Response data: null

### New OTP resend endpoint

#### POST /api/otp/resend

Current controller accepts OtpDTO, but only actionId is functionally required.

Request (recommended):

```json
{
  "actionId": "uuid"
}
```

Response data: null

Use this same endpoint for both:

- auth OTP resend
- transaction OTP resend (withdraw/transfer)

## 3) Transaction Flow Changes

## Old behavior

- start-withdraw/start-transfer returned only transcationId.
- withdraw/transfer confirm APIs did not require OTP.

## New behavior

### POST /api/transaction/start-withdraw

Response data now includes both transcationId and actionId:

```json
{
  "transcationId": "uuid",
  "actionId": "uuid"
}
```

FE rule: keep actionId internal state only. Never show it in UI.

### POST /api/transaction/start-transfer

Response data now includes both transcationId and actionId:

```json
{
  "transcationId": "uuid",
  "actionId": "uuid"
}
```

FE rule: keep actionId internal state only. Never show it in UI.

### POST /api/transaction/withdraw

Now requires OTP payload fields:

```json
{
  "senderAccountId": "uuid",
  "amount": 500,
  "transactionId": "uuid",
  "actionId": "uuid",
  "otp": 123456,
  "receiverAccountId": null
}
```

### POST /api/transaction/transfer

Now requires OTP payload fields:

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

### POST /api/transaction/admin/start-deposit

- No OTP actionId expected for deposit start.
- Response still includes transcationId and actionId field may be null.

## 4) Frontend UI/UX Updates Required

1. Add OTP step after user login submit.
2. Add OTP step after user signup submit.
3. Add resend OTP button in auth OTP screens.
4. Add OTP modal/page for withdraw confirm.
5. Add OTP modal/page for transfer confirm.
6. Add resend OTP option in withdraw/transfer OTP step (uses same /api/otp/resend endpoint).
7. Store actionId from start/initial auth call and pass it in OTP verify/confirm calls, but never display it.
8. Handle account-blocking related errors for withdraw/transfer when OTP attempts are exceeded.

## 5) Error Handling Updates

New/important messages to handle in FE:

- "Incorrect OTP"
- "OTP Attempts Exceeded"
- "OTP attempts exceeded. Account blocked"
- "OTP Expired"
- "Invalid OTP action"
- "Unauthorized OTP for this account"
- "OTP already verified. Resend is not allowed"

Map these to clear user-facing messages and redirect/refresh paths when needed.

## 6) Suggested Frontend State Model Changes

For auth:

- authIntent: login | signup
- pendingActionId: string | null
- otpCode: string
- otpStep: boolean

For withdraw/transfer:

- pendingTransactionId: string
- pendingActionId: string
- otpCode: string
- transactionOtpStep: boolean
- allowResendOtp: boolean

## 7) API Client Delta Checklist

1. Update login API return type to `{ actionId }`.
2. Update signup API return type to `{ actionId }`.
3. Add apiOtpLogin(actionId, otp).
4. Add apiOtpSignup(actionId, otp).
5. Add apiOtpResend(actionId).
6. Update startWithdraw/startTransfer response type to include actionId (internal use only).
7. Update confirm withdraw/transfer API methods to include actionId + otp.
8. Add transaction OTP resend call path in withdraw/transfer OTP screen using apiOtpResend(actionId).

## 8) Known Contract Quirks (Still Present)

1. Property typo still exists: transcationId.
2. Status enum typo still exists in project naming history (Transcation naming).
3. Keep consuming current backend keys as-is unless backend is refactored.
