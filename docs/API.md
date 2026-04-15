# API Reference

All endpoints are served on **`http://localhost:8080`** by a single Java process. The React frontend calls them through `/api/...`, which Vite rewrites to the base URL (no `/api` prefix on the server side).

Every non-GET request expects `Content-Type: application/json`. Endpoints that imply a logged-in user expect `Authorization: Bearer <token>` (tokens are issued by `/auth/signup` and `/auth/login`).

---

## Auth

### POST `/auth/signup`
```json
{ "firstName": "Alice", "lastName": "Ray", "age": 24, "email": "a@b.com", "password": "pass123" }
```
**200** `{ "token":"...", "userId":"abc12345", "firstName":"Alice", "lastName":"Ray", "email":"a@b.com", "role":"USER" }`
**400** duplicate email, missing fields

### POST `/auth/login`
```json
{ "email": "a@b.com", "password": "pass123" }
```
**200** `{ "token":"...", "userId":"...", "email":"...", "role":"USER" }` (role is `ADMIN` for the built-in admin account)
**401** invalid credentials

### POST `/auth/logout`
Header: `Authorization: Bearer <token>`
**200** `{ "ok":true, "message":"logged out" }`

### GET `/auth/validate`
Header: `Authorization: Bearer <token>`
**200** `{ "userId":"..." }`
**401** invalid token

---

## Users

### GET `/users` → list all profiles
### GET `/users/{userId}` → one profile
### POST `/users`
```json
{ "userId":"...", "firstName":"...", "lastName":"...", "age": 24, "email":"..." }
```
(auth-service calls this automatically on signup in-process; it's still exposed as a plain endpoint)

### PUT `/users/{userId}`
```json
{ "firstName":"...", "lastName":"...", "age": 25, "email":"new@mail.com" }
```

Profile shape:
```json
{
  "userId":"abc12345",
  "firstName":"Alice",
  "lastName":"Ray",
  "fullName":"Alice Ray",
  "age":24,
  "email":"a@b.com",
  "createdAt":"2026-04-15T..."
}
```

---

## Plans

### GET `/plans`
### GET `/plans/{code}` where code ∈ `BASIC | PRO | PREMIUM`

Plan shape:
```json
{
  "code":"PRO",
  "name":"Pro",
  "monthlyInr":499,
  "yearlyInr":4999,
  "screens":2,
  "quality":"1080p",
  "trialDays":7,
  "yearlyDiscountPct":17
}
```

---

## Catalog

### GET `/movies`
Optional query params: `language=Kannada|English`, `genre=Action|Drama|Thriller|Comedy|Romance`

### GET `/movies/{id}`

Movie shape:
```json
{ "id":"m01", "title":"KGF: Chapter 2", "language":"Kannada", "genre":"Action", "year":2022, "rating":8.4, "minPlan":"BASIC" }
```

---

## Subscriptions

### GET `/subscriptions` → all (admin view)
### GET `/subscriptions/user/{userId}` → all subs for one user
### POST `/subscriptions`
```json
{ "userId":"...", "planCode":"PRO", "billingCycle":"MONTHLY", "cardLast4":"1234" }
```
**200** Subscription JSON • **400** active sub already exists / unknown plan • **402** payment failed

### PUT `/subscriptions/{subId}/cancel`
### PUT `/subscriptions/{subId}/change`
```json
{ "planCode":"PREMIUM", "billingCycle":"YEARLY" }
```
### PUT `/subscriptions/{subId}/renew`

Subscription shape:
```json
{
  "subId":"...",
  "userId":"...",
  "planCode":"PRO",
  "billingCycle":"MONTHLY",
  "status":"ACTIVE",           // TRIAL | ACTIVE | CANCELLED | EXPIRED | PAYMENT_FAILED
  "startedAt":"2026-04-15T...",
  "expiresAt":"2026-05-15T...",
  "trialEndsAt":null,
  "cancelledAt":null
}
```

---

## Payments

### POST `/payments/charge`
```json
{ "userId":"...", "subId":"...", "amount":499, "cardLast4":"1234" }
```
Rules: cardLast4 `0000` or `4444` → 402 FAILED. Anything else → 200 SUCCESS (auto-creates an invoice).

### GET `/payments` • GET `/payments/user/{userId}`
### GET `/invoices` • GET `/invoices/user/{userId}`

Invoice shape:
```json
{ "invoiceId":"INV-ABCD1234", "userId":"...", "subId":"...", "paymentId":"...", "amount":499, "issuedAt":"2026-04-15T..." }
```

---

## Notifications

### POST `/notifications`
```json
{ "userId":"...", "type":"RENEWAL_REMINDER", "message":"Your plan renews in 3 days." }
```

### GET `/notifications` • GET `/notifications/user/{userId}`

---

## Admin

All `/admin/*` endpoints require `Authorization: Bearer <token>` where the token belongs to the built-in admin account.

**Seeded admin credentials:** `admin@gmail.com` / `admin@123` (in-memory; re-seeded every boot).

Non-admin tokens receive **401** `{"error":"Admin privileges required"}`.

### GET `/admin/subscriptions`
All subscriptions across all users, enriched with user + plan info:
```json
[{
  "subId":"...", "userId":"...", "planCode":"PRO", "billingCycle":"MONTHLY",
  "status":"ACTIVE", "startedAt":"...", "expiresAt":"...", "trialEndsAt":null, "cancelledAt":null,
  "userEmail":"a@b.com", "userFullName":"Alice Ray", "planName":"Pro"
}]
```

### PUT `/admin/subscriptions/{subId}/cancel`
Cancel any subscription regardless of owner. **200** updated Subscription.

### DELETE `/admin/subscriptions/{subId}`
Hard-delete. **200** `{"ok":true,"message":"deleted"}` • **404** not found.

### GET `/admin/users`
All user profiles (excluding the admin itself), each augmented with `activeSubPlan` and `activeSubStatus` (both `null` if none).

### DELETE `/admin/users/{userId}`
Deletes the profile, auth account, and cascades delete across all of their subscriptions.
**400** if the target is an admin.

### GET `/admin/plans`
Read-only list of all plans.

---

## Status Code Conventions

| Code | Meaning |
|---|---|
| 200 | Success with body |
| 204 | CORS preflight |
| 400 | Bad request / validation failure |
| 401 | Not authenticated |
| 402 | Payment failed |
| 404 | Resource not found |
| 500 | Unexpected server error |

Error body is always `{"error":"...message..."}`.
