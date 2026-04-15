# lib/

Drop third-party JARs here. The Postgres JDBC driver is required for Supabase connectivity.

## Required

**`postgresql-42.7.4.jar`** — download from https://jdbc.postgresql.org/download/

After placing the jar here, run:

```bash
# Windows
scripts\db-check.cmd

# macOS / Linux / Git Bash
bash scripts/db-check.sh
```

Expected output on success:
```
Connected.
  user   : postgres
  version: PostgreSQL 15.x ...
```

Jars in this folder are git-ignored (see `.gitignore`).
