# Netflix-Style Subscription Management System

One Java process on port **8080**, a Vite + React frontend on **5173**, and Supabase Postgres for persistence. Plans, movies, admin account, user accounts, sessions, subscriptions, payments, invoices, and notifications all live in Supabase.

---

## 1. Prerequisites

| Tool | Version | Why |
|---|---|---|
| JDK 11+ | `java -version` | Compiles + runs the backend |
| Node 18+ | `node -v` | Runs Vite dev server |
| Git Bash *(Windows)* | any | To run `.sh` scripts, optional — `.cmd` equivalents exist |

No Maven/Gradle. No local Postgres install. No Docker.

---

## 2. First-time setup

### 2a. Rename `.env.test` → `.env`

The repo ships a working Supabase config under `.env.test`. Rename it so the backend picks it up:

```powershell
# Windows PowerShell
Rename-Item .env.test .env
```
```cmd
:: Windows cmd.exe
ren .env.test .env
```
```bash
# macOS / Linux / Git Bash
mv .env.test .env
```

`.env` is git-ignored. `.env.test` is committed so a fresh clone has a working-by-default template.

The file has three lines:
```
SUPABASE_DB_URL=jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require
SUPABASE_DB_USER=postgres.<project-ref>
SUPABASE_DB_PASSWORD=<password>
```
If you fork the project and point it at your own Supabase instance, replace all three values with your project's **Session Pooler** connection string (Supabase dashboard → Project Settings → Database → Connection string → Session pooler).

### 2b. Drop the Postgres JDBC driver into `lib/`

Only needed once. The driver jar is git-ignored so you won't commit it by accident.

1. Download `postgresql-42.7.4.jar` from <https://jdbc.postgresql.org/download/>.
2. Place it into `A:\Aanya\Subscription-Management-Aanya\lib\`.

### 2c. Install frontend deps

```bash
cd frontend
npm install
cd ..
```

---

## 3. Run the project

Always run from the **project root** (`A:\Aanya\Subscription-Management-Aanya`). The backend loads `.env` and `model/sql/*.sql` via relative paths — launching from any sub-folder will fail with `.env missing` or `Missing SQL file`.

### Backend

```powershell
# Windows PowerShell (note the dot-prefix)
.\scripts\build.cmd
.\scripts\start-all.cmd
.\scripts\stop-all.cmd
```
```cmd
:: Windows cmd.exe (no dot-prefix)
scripts\build.cmd
scripts\start-all.cmd
scripts\stop-all.cmd
```
```bash
# macOS / Linux / Git Bash
bash scripts/build.sh
bash scripts/start-all.sh
bash scripts/stop-all.sh
```

On startup the server:
1. Connects to Supabase using `.env`.
2. `DROP`s every app table and re-runs `model/sql/schema.sql` + `model/sql/seed.sql` — **a fresh restart wipes user data, plans and movies re-seed automatically.**
3. Opens port 8080 and prints the endpoint banner.

Tail the log (another terminal):
```powershell
Get-Content logs\app.log -Wait -Tail 50
```
```bash
tail -f logs/app.log
```

### Frontend

```bash
cd frontend
npm run dev
```
Opens on <http://localhost:5173>. Vite proxies `/api/*` to `:8080`.

### Quick connectivity test (skip server startup)

```bash
bash scripts/db-check.sh   # or scripts\db-check.cmd
```
Expected:
```
Connected in 1500 ms.
  user   : postgres
  version: PostgreSQL 17.6 ...
```

---

## 4. Built-in admin

Seeded on every boot (defined in `model/sql/seed.sql`, password hash rewritten by `Schema.applyAndSeed`):

| Field | Value |
|---|---|
| Email | `admin@gmail.com` |
| Password | `admin@123` |
| Role | `ADMIN` |

Log in through the normal login form — the UI auto-switches to the Admin Dashboard (manage subscriptions, delete users, list plans).

---

## 5. Project layout

```
├── services/
│   ├── app/                 # single entry point (App.java)
│   ├── auth-service/        # signup, login, sessions
│   ├── user-service/        # profiles
│   ├── plan-service/        # Basic/Pro/Premium
│   ├── subscription-service/
│   ├── payment-service/     # dummy Stripe + invoices
│   ├── notification-service/
│   ├── catalog-service/     # movies
│   └── admin-service/       # cross-module admin endpoints
├── model/                   # <-- NEW: DB layer
│   ├── src/com/netflixsub/model/
│   │   ├── Db.java          # connection holder, reads .env
│   │   ├── Schema.java      # runs schema.sql + seed.sql on boot
│   │   └── *Repository.java
│   └── sql/
│       ├── schema.sql       # drops & recreates every boot
│       └── seed.sql         # static plans / movies / admin
├── shared/                  # Json, Http, Ids utilities
├── frontend/                # Vite + React
├── scripts/                 # build / start / stop / db-check
├── lib/                     # postgresql-*.jar (you add)
├── docs/                    # ARCHITECTURE / PHASES / API / RUNBOOK
├── .env                     # your local DB credentials (git-ignored)
└── .env.test                # committed template — rename to .env
```

---

## 6. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `java.lang.IllegalStateException: .env missing ...` | You forgot to rename `.env.test` → `.env`, or launched the server from a sub-folder | Rename, and re-run from the project root |
| `ClassNotFoundException: org.postgresql.Driver` | Driver jar not in `lib/` | Download `postgresql-42.7.4.jar` into `lib/` |
| `UnknownHostException: db.<ref>.supabase.co` | Direct connection is IPv6-only; your network is IPv4-only | Use the **Session Pooler** URL in `.env` (already set in `.env.test`) |
| `PSQLException: Tenant or user not found` | Wrong region or wrong username | `SUPABASE_DB_USER` must be `postgres.<project-ref>` and the pooler host must match the project's region |
| `[vite] http proxy error: /auth/signup ECONNREFUSED` | Backend isn't running | `scripts\start-all.cmd` (or `bash scripts/start-all.sh`) |
| "All my data disappeared on restart" | By design — schema+seed re-runs on every boot | Comment out `Schema.applyAndSeed(...)` in `App.main()` if you want data to persist between restarts |

For deeper issues see `docs/RUNBOOK.md`.

---

## 7. Further reading

- `docs/ARCHITECTURE.md` — module layout, wiring, data flow
- `docs/API.md` — every endpoint + request/response shape
- `docs/PHASES.md` — phased build order with acceptance tests
- `docs/RUNBOOK.md` — expanded troubleshooting
- `DOCUMENTATION.md` — plain-English end-to-end walkthrough
