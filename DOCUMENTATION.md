# Netflix-Style Subscription Management System — Full Documentation

A beginner-friendly guide to understanding, running, and using this project. **One Java process, one port (8080)**, all APIs served together. React + Vite frontend. In-memory storage.

---

## 1. What Is This Project (Simple Words)

Think of it like a miniature Netflix admin + customer app:

- Users **sign up** with first name, last name, age, email (unique), and password, then **log in**.
- After login, the app greets them by **first name only** — internal IDs (`userId`, `subId`) are never shown.
- They **browse a catalog** of Kannada and English movies across 5 genres.
- They see **three plans** — Basic, Pro, Premium — with monthly or yearly billing and a free trial.
- They **pay with a dummy card** to subscribe. We simulate success and failure.
- Subscriptions have a **start date** and an **expiry date**. After expiry, status flips to `EXPIRED`.
- Users can **cancel**, **renew**, or **upgrade** their plan.
- Every charge produces an **invoice**, and users can see their **payment history**.
- The app stores **notifications** (renewal reminders, payment failures) for other modules to post.

---

## 2. Architecture in One Picture

```
  Browser (React, :5173)  ──▶  Netflix.sub Server  (:8080)
                                    │
                     One JVM holding every module in memory
                     ┌──────┬───────┼────────────┬────────────┐
                     ▼      ▼       ▼            ▼            ▼
                   /auth /users  /plans    /subscriptions   /movies
                   /payments /invoices /notifications
```

Every API lives on **one port, one process**. Modules are organised by folder (`services/auth-service`, `services/plan-service`, …) so each domain stays clearly separated in source code, but at runtime they're wired together directly — no inter-service HTTP calls. `services/app/App.java` is the single entry point that boots everything.

---

## 3. Folder Structure

```
Subscription-Management-Aanya/
├── services/
│   ├── app/                    single-process entry (App.java) — starts port 8080
│   ├── auth-service/           signup, login, token validation
│   ├── user-service/           user profiles (firstName, lastName, age, email)
│   ├── plan-service/           Basic / Pro / Premium definitions + pricing
│   ├── subscription-service/   subscribe/cancel/upgrade/expiry
│   ├── payment-service/        dummy Stripe + invoices
│   ├── notification-service/   renewal/expiry alerts (log-based)
│   └── catalog-service/        Kannada + English movies (5 genres)
│
├── shared/
│   └── src/com/netflixsub/common/   Json, Http, Ids helpers
│
├── frontend/
│   └── src/
│       ├── api.js              single network layer (hits /api/* → :8080)
│       ├── App.jsx             top-level tabs + auth gate
│       ├── pages/              Login, Browse, Plans, Account
│       └── components/         ErrorModal
│
├── scripts/
│   ├── build.{cmd,sh}          compile everything
│   ├── start-all.{cmd,sh}      launch the single-port server
│   └── stop-all.{cmd,sh}       stop it
│
├── docs/
│   ├── ARCHITECTURE.md         module layout and conventions
│   ├── PHASES.md               phased implementation guide
│   ├── API.md                  complete endpoint reference
│   └── RUNBOOK.md              troubleshooting
│
├── build/classes/              compiled .class files (from build.*)
├── logs/app.log                runtime log
│
├── DOCUMENTATION.md            ← this file
└── CLAUDE.md                   guide for AI assistants
```

---

## 4. Prerequisites

| Tool | Why | Check |
|---|---|---|
| **Java 11+** | Runs the server | `java -version` |
| **Node.js 18+** | Runs the frontend | `node -v` |
| **npm** | Installs frontend deps | `npm -v` |

> **Script flavors**: Use `.cmd` for Windows (PowerShell / cmd), `.sh` for Git Bash / macOS / Linux. If PowerShell rejects `bash scripts/build.sh` with "WSL has no distributions", use `.\scripts\build.cmd` instead.

---

## 5. How to Run — Step by Step

Run every command from the **project root** (`A:\Aanya\Subscription-Management-Aanya`).

```powershell
cd A:\Aanya\Subscription-Management-Aanya
```

### Step 1 — Compile

Windows PowerShell:
```powershell
.\scripts\build.cmd
```
Windows cmd.exe:
```cmd
scripts\build.cmd
```
macOS / Linux / Git Bash:
```bash
bash scripts/build.sh
```

Outputs `Build successful.` and creates `build\classes\`.

### Step 2 — Start the server

Windows PowerShell:
```powershell
.\scripts\start-all.cmd
```
Windows cmd.exe:
```cmd
scripts\start-all.cmd
```
macOS / Linux / Git Bash:
```bash
bash scripts/start-all.sh
```

On Windows the server runs in a minimized window titled `netflixsub`. On bash it runs in the background. Logs always stream to `logs\app.log`.

Verify:
```bash
curl http://localhost:8080/
# → {"service":"netflix.sub","status":"ok"}

curl http://localhost:8080/plans
# → [{"code":"BASIC",...},{"code":"PRO",...},{"code":"PREMIUM",...}]
```

### Step 3 — Start the frontend (separate terminal)

```bash
cd frontend
npm install           # first time only
npm run dev
```

Open the URL it prints (usually `http://localhost:5173`). The frontend calls `/api/...`, which Vite proxies to `http://localhost:8080/...`.

### Step 4 — Try the full flow

1. **Sign up** — first name, last name, age (13+), email (must be unique), password.
2. **Browse** — filter movies by Kannada/English and by genre.
3. **Plans** — pick monthly or yearly, enter card last-4 (use `0000` to force a payment failure), click Subscribe.
   - First-time subscriptions with a trial skip payment.
   - Trial ends after 7 or 14 days depending on plan.
4. **My Account** — see your subscription, try **Upgrade** (Basic → Pro → Premium), **Cancel**, **Renew**. Check Invoices and Payment History.
5. **Logout** from the top bar.

### Step 5 — Stop everything

Windows PowerShell:
```powershell
.\scripts\stop-all.cmd
```
Windows cmd.exe:
```cmd
scripts\stop-all.cmd
```
macOS / Linux / Git Bash:
```bash
bash scripts/stop-all.sh
```

Ctrl+C the frontend terminal.

---

## 6. Phased Implementation Guide

Build the project phase by phase. Each phase produces something demo-able.

### Phase 1 — Foundation
- Create folder layout + shared utilities (`Json`, `Http`, `Ids`).
- Write `App.java` stub that starts an HttpServer on :8080 with a `/` health endpoint.

### Phase 2 — Identity (Auth + User)
- Build `ProfileStore` (in-memory users keyed by id AND email).
- Build `AuthStore` (accounts + tokens).
- Wire `/auth/signup`, `/auth/login`, `/auth/logout`, `/auth/validate`.
- AuthHandler calls `ProfileStore.create()` directly — no HTTP hop.

### Phase 3 — Content (Plans + Catalog)
- Seed `PlanStore` with Basic/Pro/Premium (monthly + yearly + trial + yearly-discount).
- Seed `CatalogStore` with 15 movies (Kannada + English × 5 genres).
- Mount `/plans` and `/movies` handlers.

### Phase 4 — Transactions (Subscription + Payment)
- Build `PaymentProcessor` with dummy rules: cards `0000` / `4444` fail.
- Build `SubStore` + `Subscription` with `status` (TRIAL/ACTIVE/CANCELLED/EXPIRED), `startedAt`, `expiresAt`, `trialEndsAt`.
- Mount `/subscriptions` and `/payments`, `/invoices`. SubHandler takes `PlanStore` and `PaymentProcessor` directly.

### Phase 5 — Automation (Notifications + Scheduler)
- Mount `/notifications` handler with an in-memory log.
- **Scheduler (scaffold)** — not yet wired. Drop a `ScheduledExecutorService` in `App.main()`:

```java
Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
    for (Subscription s : subs.all()) {
        long daysLeft = ChronoUnit.DAYS.between(Instant.now(), s.expiresAt);
        if (daysLeft == 3) { /* send RENEWAL_REMINDER */ }
        if (daysLeft <= 0 && "ACTIVE".equals(s.status)) {
            var res = payments.charge(s.userId, s.subId, price, "0000");
            if (res.success) s.renewFrom(Instant.now());
            else s.status = "PAYMENT_FAILED";
        }
    }
}, 0, 1, TimeUnit.HOURS);
```

See `docs/PHASES.md` for the full per-phase acceptance criteria.

---

## 7. Features at a Glance

| Feature | Module | Notes |
|---|---|---|
| Login / Signup | auth + user | Bearer-token via random UUID. Email unique across both stores. |
| Plans & pricing | plan | 3 tiers, monthly/yearly, trial days, yearly discount |
| Free trial | subscription | If plan has `trialDays > 0`, charge is skipped, status starts as `TRIAL` |
| Subscribe | subscription + payment | In-process charge, no HTTP round-trip |
| Cancel | subscription | Flips status to `CANCELLED`; user keeps access until `expiresAt` |
| Upgrade / Downgrade | subscription | `PUT /subscriptions/{id}/change` |
| Expiry | subscription | `sweepExpired()` runs on every request |
| Recurring billing | scheduler scaffold | Manual `PUT /subscriptions/{id}/renew` for now |
| Payment integration | payment | Dummy Stripe; card `0000`/`4444` → fail |
| Invoices | payment | Auto-created on successful charge; id `INV-XXXXXXXX` |
| Notifications | notification | POST + list in-memory + stdout log |
| Catalog | catalog | 15 movies; Kannada & English; 5 genres |
| Admin console | admin | Hardcoded `admin@gmail.com` / `admin@123` — list/cancel/delete any subscription, delete any user |

---

## 7a. Admin Access

The platform ships with one built-in admin, seeded in memory at startup:

- **Email:** `admin@gmail.com`
- **Password:** `admin@123`

Log in through the **normal login form** — the backend returns `role: "ADMIN"` and the frontend automatically routes you to the **Admin Dashboard** instead of the regular Browse/Plans/Account tabs.

The dashboard has three sections:

- **Subscriptions** — every subscription across every user; can **cancel** (soft) or **delete** (hard).
- **Users** — every signed-up user; can **delete** (cascade-removes their account, profile, and all subscriptions).
- **Plans** — read-only list of the three seeded plans.

Every `/admin/*` API requires a bearer token that maps to the admin account; other tokens get `401 Admin privileges required`.

Because storage is in-memory, the admin account is re-seeded on every server restart. Deleting the admin via the API is blocked (`400 Cannot delete admin account`).

---

## 8. Dummy Payment Rules

| Card last-4 | Result |
|---|---|
| `0000` | Always fails with "Card declined (test card)" |
| `4444` | Always fails |
| anything else | Succeeds, creates a `SUCCESS` payment + an invoice |

---

## 9. curl Recipes (Single-Port, No Gateway Prefix)

With the server running on :8080, endpoints are served **directly** — no `/api` prefix. The frontend adds the `/api` prefix and Vite strips it on the way through.

```bash
# 1. Sign up
curl -X POST http://localhost:8080/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Aanya","lastName":"Rao","age":24,"email":"aanya@test.com","password":"pass123"}'
# Returns: {"token":"...","userId":"abc12345","firstName":"Aanya","lastName":"Rao","email":"aanya@test.com"}

TOKEN=<paste token>
UID=<paste userId>

# 2. Browse plans
curl http://localhost:8080/plans

# 3. Subscribe to Pro yearly
curl -X POST http://localhost:8080/subscriptions \
  -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$UID\",\"planCode\":\"PRO\",\"billingCycle\":\"YEARLY\",\"cardLast4\":\"1234\"}"

# 4. List my subs
curl http://localhost:8080/subscriptions/user/$UID

# 5. See invoices
curl http://localhost:8080/invoices/user/$UID

# 6. Admin: log in and list every subscription
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@gmail.com","password":"admin@123"}'
# Response includes "role":"ADMIN"

ADMIN_TOKEN=<paste admin token>
curl -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/admin/subscriptions
curl -X PUT    -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/admin/subscriptions/<subId>/cancel
curl -X DELETE -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/admin/subscriptions/<subId>
curl -X DELETE -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/admin/users/<userId>
```

---

## 10. What's Intentionally Simple (Trade-offs)

- **In-memory storage.** Restart = clean slate. A production system would use Postgres, Redis, etc.
- **Passwords hashed with `String.hashCode()`.** Demo only — swap for bcrypt/argon2 in real apps.
- **Tokens aren't JWTs.** Random UUIDs stored server-side. For real apps use JWT or OAuth.
- **Single-process.** Great for demos and small deployments. Scaling horizontally would require pulling state into a shared store.
- **Hand-rolled JSON.** Good enough for flat payloads; switch to Jackson for anything non-trivial.
- **No inter-module auth.** Modules trust each other by being in the same JVM.

---

## 11. Where to Read More

- `docs/ARCHITECTURE.md` — module layout, data models, wiring.
- `docs/PHASES.md` — phased implementation recipe.
- `docs/API.md` — every endpoint, payload, and response shape.
- `docs/RUNBOOK.md` — "it broke, what do I check?"

---

## 12. Glossary (Plain English)

- **Module** — a folder under `services/` that owns one concern (e.g. `payment-service`). At runtime everything runs in one Java process.
- **Bearer token** — a random string you attach to requests so the server knows who you are.
- **Trial** — a grace period during which you're not charged. Controlled by `trialDays` on each plan.
- **Billing cycle** — monthly or yearly; determines how long a subscription lasts before expiring.
- **Invoice** — a record of a successful payment.
- **Expiry sweep** — a quick scan that marks old subscriptions as `EXPIRED` so the UI stays in sync.
- **CORS** — a browser safety rule. The Vite dev proxy routes `/api` → `:8080`, sidestepping CORS in dev.

---

**Version**: 3.0 — single-port consolidation
**Stack**: Java (JDK-only) single-process + Vite/React frontend
**Storage**: In-memory (HashMap/List) — data is lost on restart
