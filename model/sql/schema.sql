-- Tables are created only if they don't already exist. User data persists across restarts.

CREATE TABLE IF NOT EXISTS plans (
  code                 TEXT PRIMARY KEY,
  name                 TEXT NOT NULL,
  monthly_inr          DOUBLE PRECISION NOT NULL,
  yearly_inr           DOUBLE PRECISION NOT NULL,
  screens              INT  NOT NULL,
  quality              TEXT NOT NULL,
  trial_days           INT  NOT NULL,
  yearly_discount_pct  INT  NOT NULL
);

CREATE TABLE IF NOT EXISTS movies (
  id        TEXT PRIMARY KEY,
  title     TEXT NOT NULL,
  language  TEXT NOT NULL,
  genre     TEXT NOT NULL,
  year      INT  NOT NULL,
  rating    DOUBLE PRECISION NOT NULL,
  min_plan  TEXT NOT NULL REFERENCES plans(code)
);

CREATE TABLE IF NOT EXISTS accounts (
  user_id       TEXT PRIMARY KEY,
  email         TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  role          TEXT NOT NULL DEFAULT 'USER',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS profiles (
  user_id     TEXT PRIMARY KEY REFERENCES accounts(user_id) ON DELETE CASCADE,
  first_name  TEXT NOT NULL,
  last_name   TEXT NOT NULL,
  age         INT  NOT NULL DEFAULT 0,
  email       TEXT UNIQUE NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sessions (
  token      TEXT PRIMARY KEY,
  user_id    TEXT NOT NULL REFERENCES accounts(user_id) ON DELETE CASCADE,
  issued_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS subscriptions (
  sub_id         TEXT PRIMARY KEY,
  user_id        TEXT NOT NULL REFERENCES accounts(user_id) ON DELETE CASCADE,
  plan_code      TEXT NOT NULL REFERENCES plans(code),
  billing_cycle  TEXT NOT NULL,
  status         TEXT NOT NULL,
  started_at     TIMESTAMPTZ NOT NULL,
  expires_at     TIMESTAMPTZ NOT NULL,
  trial_ends_at  TIMESTAMPTZ,
  cancelled_at   TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS payments (
  payment_id      TEXT PRIMARY KEY,
  user_id         TEXT NOT NULL,
  sub_id          TEXT NOT NULL,
  amount          DOUBLE PRECISION NOT NULL,
  card_last4      TEXT NOT NULL,
  status          TEXT NOT NULL,
  failure_reason  TEXT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS invoices (
  invoice_id  TEXT PRIMARY KEY,
  user_id     TEXT NOT NULL,
  sub_id      TEXT NOT NULL,
  payment_id  TEXT NOT NULL,
  amount      DOUBLE PRECISION NOT NULL,
  issued_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS notifications (
  id          TEXT PRIMARY KEY,
  user_id     TEXT NOT NULL,
  type        TEXT NOT NULL,
  message     TEXT NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
