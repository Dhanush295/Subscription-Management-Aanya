# Architecture Deep Dive

## Runtime Model

**One JVM, one port, one process.** The project is organised into `services/<name>/` folders for source clarity, but at runtime `services/app/App.java` boots a single `HttpServer` on port 8080 and registers every handler directly. Modules talk to each other through plain object references — no HTTP inter-service hops.

This is deliberately **not** a full microservice deployment. It's a modular monolith: separate in source, unified in runtime.

## Persistence

All state lives in **Supabase Postgres** (free tier, accessed via the Session Pooler). The `model/` folder owns the DB layer:

- `model/src/com/netflixsub/model/Db.java` — single `Connection` holder; reads credentials from `.env` at boot.
- `model/src/com/netflixsub/model/Schema.java` — runs `model/sql/schema.sql` (DROP + CREATE all tables) + `model/sql/seed.sql` (3 plans, 15 movies, 1 admin account) **on every boot**. Then overwrites the admin password hash with the live `AuthStore.hash("admin@123")` value so login matches.
- `model/src/com/netflixsub/model/*Repository.java` — one JDBC repository per entity (accounts, profiles, sessions, plans, movies, subscriptions, payments, invoices, notifications).

Each in-memory `*Store` is now a **write-through cache**: on construction it loads all rows from its repository; every mutation also calls `repo.upsert` / `repo.delete` so the DB stays in sync. Handlers did not need significant changes — the store's public API is identical to the pre-DB version.

**Restart semantics:** "clean slate on boot" is preserved — `Schema.applyAndSeed` drops all data, plans + movies + admin come back from seed, user accounts/subs/payments are wiped. Comment out `Schema.applyAndSeed(...)` in `App.main()` if you want data to survive restarts.

## Module Catalog

| Folder | Package | Owns |
|---|---|---|
| `services/app`                | `com.netflixsub.app`          | Boot + wiring (`App.java`) |
| `services/auth-service`       | `com.netflixsub.auth`         | Accounts, passwords, tokens |
| `services/user-service`       | `com.netflixsub.user`         | `ProfileStore` with firstName/lastName/age/email (email unique) |
| `services/plan-service`       | `com.netflixsub.plan`         | Plan catalog (Basic/Pro/Premium) |
| `services/subscription-service` | `com.netflixsub.subscription` | Subscription lifecycle |
| `services/payment-service`    | `com.netflixsub.payment`      | `PaymentProcessor`, payments, invoices |
| `services/notification-service` | `com.netflixsub.notification` | Notification log |
| `services/catalog-service`    | `com.netflixsub.catalog`      | Movie metadata |
| `services/admin-service`      | `com.netflixsub.admin`        | Cross-module admin ops: list/cancel/delete any subscription, delete any user |
| `model`                       | `com.netflixsub.model`        | JDBC layer: `Db`, `Schema`, `*Repository`; `model/sql/{schema,seed}.sql` |
| `shared`                      | `com.netflixsub.common`       | `Json`, `Http`, `Ids` utilities |

## Wiring Diagram (what `App.java` does)

```
App.main():
    profiles   = new ProfileStore()
    auth       = new AuthStore()
    plans      = new PlanStore()         ──── seed Basic/Pro/Premium
    catalog    = new CatalogStore()      ──── seed 15 movies
    payments   = new PaymentProcessor()
    Db.init()                                         ──── reads .env
    Schema.applyAndSeed(AuthStore.hash("admin@123"))  ──── drops + reseeds
    subs       = new SubStore()

    // admin@gmail.com / admin@123 is seeded by model/sql/seed.sql and loaded
    // into AuthStore + ProfileStore during store construction

    server.createContext("/auth",          new AuthHandler(auth, profiles))
    server.createContext("/users",         new UserHandler(profiles))
    server.createContext("/plans",         new PlanHandler(plans))
    server.createContext("/movies",        new CatalogHandler(catalog))
    server.createContext("/subscriptions", new SubHandler(subs, plans, payments))
    server.createContext("/payments",      new PaymentHandler(payments))
    server.createContext("/invoices",      new PaymentHandler(payments))
    server.createContext("/notifications", new NotificationHandler())
    server.createContext("/admin",         new AdminHandler(auth, profiles, subs, plans))
    server.createContext("/",              new Root())
```

## Data Flow: "Subscribe to Pro" walkthrough

```
Browser ──POST /api/subscriptions──▶ Vite proxy ──▶ :8080/subscriptions
                                                       │
                                                       ▼
                                                  SubHandler.create()
                                                       │ 1. subs.activeFor(userId)      → none
                                                       │ 2. plans.get("PRO")            → Plan{...}
                                                       │ 3. new Subscription(...)
                                                       │ 4. payments.charge(...)        → Result(success, Payment, Invoice)
                                                       │ 5. subs.put(sub)
                                                       └ returns Subscription JSON
```

Every call in that flow is a plain Java method invocation — no JSON round-trips, no network hops, no latency.

## Package Conventions

- `App.java` — the single boot class.
- `*Handler.java` — implements `HttpHandler`, dispatches by method + path.
- `*Store.java` — in-memory collection + query methods (owns state).
- `PaymentProcessor` — domain-service style helper that both the HTTP handler and the subscription module use.
- Model classes (`Subscription`, `Plan`, `Movie`, `Profile`, …) are POJOs with a `toString()` that emits JSON.

Only stores mutate their own state. Handlers and other modules read/write through the store's API.

## Error Handling

| HTTP | Meaning |
|---|---|
| 200 | Success with body |
| 204 | CORS preflight |
| 400 | Validation failure — body is `{"error":"..."}` |
| 401 | Not authenticated |
| 402 | Payment declined |
| 404 | Resource not found |
| 500 | Unexpected exception |

## Why Modules Instead of Micro-Services

The first cut of this project used one Java process per service on ports 8080–8087 with a gateway. It worked but:
- Every inter-service call went through HTTP + JSON serialization (slow + fragile).
- 8 processes = 8 log tails, 8 possible port conflicts, 8 cold starts.
- The demo complexity outweighed the separation benefit.

The current modular-monolith approach keeps the **source-level** separation (each module in its own folder, with its own package), but collapses the runtime to one process. If this ever needs to scale out, each module already has a clean boundary — you'd extract one at a time rather than refactor from scratch.

## Port Map

```
8080   netflix.sub (every endpoint)
5173   Vite dev server (proxies /api → 8080)
```

Change the port by passing an argument: `java -cp build\classes com.netflixsub.app.App 9090`.
