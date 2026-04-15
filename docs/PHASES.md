# Phased Implementation Guide

Build the project phase by phase. Each phase produces a working, demonstrable milestone. Everything lives in **one Java process on port 8080**; modules stay in separate folders for source-level clarity, but there's no inter-service HTTP.

---

## Phase 1 — Foundation

**Goal**: a running server that answers a health check.

### Steps
1. Create folder layout (`services/`, `shared/`, `scripts/`, `docs/`, `frontend/`).
2. Write `shared/src/com/netflixsub/common/`:
   - `Json.java` — `readBody`, `field` (regex extractor), `array`, `esc`, `err`.
   - `Http.java` — `cors`, `send`, `authHeader`.
   - `Ids.java` — `shortId`, `token`.
3. Write `services/app/src/com/netflixsub/app/App.java`:
   - Starts `HttpServer` on port 8080.
   - Registers one context: `/` returning `{"service":"netflix.sub","status":"ok"}`.

### Acceptance
- `scripts\build.cmd` compiles clean.
- `scripts\start-all.cmd` starts the server.
- `curl http://localhost:8080/` returns the health JSON.

---

## Phase 2 — Identity (Auth + User)

**Goal**: signup + login.

### Steps
1. `services/user-service/`
   - `Profile.java` (firstName, lastName, age, email, fullName).
   - `ProfileStore.java` with `byId`, `byEmail`, `create`, `emailTaken`, `rebindEmail`.
   - `UserHandler.java` → GET/POST/PUT `/users`, `/users/{id}`.
2. `services/auth-service/`
   - `Account.java`, `AuthStore.java` (accounts + tokens).
   - `AuthHandler.java` takes `AuthStore` + `ProfileStore` in its constructor. On signup it calls `profiles.create()` directly (no HTTP).
3. Update `App.java` to construct both stores and register `/auth`, `/users`.

### Acceptance
```bash
curl -X POST http://localhost:8080/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"A","lastName":"B","age":24,"email":"a@b.com","password":"x"}'
# → {"token":"...","userId":"...","firstName":"A","lastName":"B","email":"a@b.com"}

curl http://localhost:8080/auth/validate -H "Authorization: Bearer <token>"
# → {"userId":"..."}
```

---

## Phase 3 — Content (Plans + Catalog)

**Goal**: something for the user to browse.

### Steps
1. `services/plan-service/`
   - `Plan.java` POJO, `PlanStore.java` seeded with Basic (₹199/mo), Pro (₹499/mo), Premium (₹799/mo).
   - `PlanHandler.java` → GET `/plans`, GET `/plans/{code}`.
2. `services/catalog-service/`
   - `Movie.java`, `CatalogStore.java` (15 movies: Kannada + English × 5 genres).
   - `CatalogHandler.java` → GET `/movies?language=&genre=`, GET `/movies/{id}`.
3. Register both in `App.java`.

### Acceptance
- `GET /plans` → 3 plans.
- `GET /movies?language=Kannada&genre=Drama` → filtered list.
- Frontend Browse + Plans tabs render.

---

## Phase 4 — Transactions (Subscription + Payment)

**Goal**: money moves (simulated).

### Steps
1. `services/payment-service/`
   - `Payment.java`, `Invoice.java`.
   - `PaymentProcessor.java` — `charge()` returns a `Result(Payment, Invoice, success)`. Cards `0000`/`4444` fail.
   - `PaymentHandler.java` takes a `PaymentProcessor`. Serves `/payments/charge`, `/payments`, `/payments/user/{id}`, `/invoices`, `/invoices/user/{id}`.
2. `services/subscription-service/`
   - `Subscription.java` with `status`, `startedAt`, `expiresAt`, `trialEndsAt`, lifecycle helpers (`cancel`, `renew`, `changePlan`, `isExpired`).
   - `SubStore.java` with `activeFor`, `forUser`, `sweepExpired`.
   - `SubHandler.java` takes `SubStore`, `PlanStore`, `PaymentProcessor` — calls them directly.
3. Wire everything in `App.java`.

### Acceptance
- Subscribe to Pro monthly with card `1234` → success + invoice.
- Subscribe again for same user → 400 "already has active subscription."
- Subscribe with `cardLast4: "0000"` (no-trial plan) → 402 "Payment failed."
- Cancel → status `CANCELLED`; `PUT /{id}/cancel` again → 400.
- Upgrade → new plan + fresh expiry.

---

## Phase 5 — Automation (Notifications + Scheduler)

**Goal**: the system takes care of itself.

### 5a. Notifications (implemented)
- `services/notification-service/`
  - `Notification.java`, `NotificationHandler.java` → POST `/notifications`, GET `/notifications`, GET `/notifications/user/{id}`.

### 5b. Recurring billing scheduler (scaffold)
Not yet wired in. Add to `App.main()` after services are built:

```java
ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
sched.scheduleAtFixedRate(() -> {
    Instant now = Instant.now();
    for (Subscription s : subs.all()) {
        if ("CANCELLED".equals(s.status)) continue;
        long daysLeft = ChronoUnit.DAYS.between(now, s.expiresAt);
        if (daysLeft == 3) {
            // notificationHandler.log(s.userId, "RENEWAL_REMINDER",
            //     "Your " + s.planCode + " plan renews in 3 days.");
        }
        if (daysLeft <= 0 && "ACTIVE".equals(s.status)) {
            Plan plan = plans.get(s.planCode).orElse(null);
            if (plan == null) continue;
            double price = "YEARLY".equalsIgnoreCase(s.billingCycle) ? plan.yearlyInr : plan.monthlyInr;
            var res = payments.charge(s.userId, s.subId, price, "0000"); // stored on file in real version
            if (res.success) s.renewFrom(now);
            else s.status = "PAYMENT_FAILED";
        }
    }
}, 0, 1, TimeUnit.HOURS);
```

### Acceptance (when wired)
- A subscription expiring today is auto-renewed on the next scheduler tick.
- A failing card flips it to `PAYMENT_FAILED` and logs a notification.

---

## Beyond Phase 5 (Ideas)

- Real persistence (Postgres via JDBC).
- Email delivery in notification-service (SMTP).
- JWTs instead of random tokens.
- Rate limiting.
- Metrics endpoint on `/metrics`.
- Split one module back out into its own process when traffic justifies it.
