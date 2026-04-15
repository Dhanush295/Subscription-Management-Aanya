# Runbook — Troubleshooting

## "The module 'scripts' could not be loaded" (PowerShell)

PowerShell doesn't run scripts from the current directory unless you prefix with `.\`. Two things to check:

1. You're in the **project root** (`A:\Aanya\Subscription-Management-Aanya`), not `backend\` or any sub-folder. Run `dir` — you should see `services\`, `frontend\`, `scripts\`, `shared\`, `docs\`.
2. Use the dot-prefix form:
   ```powershell
   .\scripts\build.cmd
   .\scripts\start-all.cmd
   .\scripts\stop-all.cmd
   ```

In **cmd.exe** (not PowerShell) the bare form `scripts\build.cmd` works fine — this quirk is PowerShell-only.

## "Windows Subsystem for Linux has no installed distributions"

You ran `bash scripts/build.sh` from PowerShell / cmd, and Windows mapped `bash` to WSL (which isn't installed).
**Fix**: use the Windows-native scripts instead:
```cmd
scripts\build.cmd
scripts\start-all.cmd
scripts\stop-all.cmd
```
Or install Git for Windows (which ships with Git Bash) and run the `.sh` versions from there.

## "Port 8080 already in use" / 404 "No context found"

You'll see `Address already in use: bind` in `logs\app.log`, and `curl http://localhost:8080/plans` returns an HTML 404 instead of JSON. Some other Java process is holding the port.

**PowerShell**:
```powershell
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
netstat -ano | findstr ":8080"        # should print nothing
.\scripts\start-all.cmd
```

**cmd.exe**:
```cmd
taskkill /IM java.exe /F
scripts\start-all.cmd
```

If `curl http://localhost:8080/` returns `{"service":"netflix.sub","status":"ok"}`, you're good.

## Frontend shows "Cannot connect to server"

- Is the server running? `curl http://localhost:8080/`
- Vite dev server proxies `/api` → `:8080`. Confirm `frontend/vite.config.js` still has the proxy block.

## "Subscription already cancelled" but I want to re-subscribe

By design, `SubStore.activeFor(userId)` blocks a second active sub per user. Let the sub expire, or remove that guard in `SubHandler.create`.

## Payment fails for every card

Card last-4 `0000` or `4444` are hardcoded fail-cases in `PaymentProcessor.charge`. Use any other 4 digits.

## Admin login returns 401 / admin dashboard doesn't appear

The admin account (`admin@gmail.com` / `admin@123`) is seeded in `App.main()` via `auth.registerAdmin(...)`. If login fails:
- Confirm the server is running the latest build (`scripts\build.cmd` then restart).
- Check `logs\app.log` for the startup banner — it should include the `/admin/*` line. If not, the admin module isn't wired in.
- The credentials are case-insensitive on email; the password is exact (`admin@123`).
- Admin state lives in memory — restart the server and try again if the in-memory account was accidentally deleted via `DELETE /admin/users/{id}` (it guards against this, but test code may bypass).
- In the browser, `localStorage.user.role` must be `"ADMIN"`; if it's `"USER"`, log out and log back in to refresh the cached role.

## All my data disappeared after restart

Expected — everything is in-memory. Restarting the server clears every store.

## "Build failed"

Tail the `javac` output. Most common causes:
- You added a new class but forgot to include its folder in `scripts\build.cmd`.
- You broke compilation in `shared\common\*.java` — every module imports those, so the error cascades.

Run just the shared compile to isolate:
```cmd
javac -d build\classes shared\src\com\netflixsub\common\*.java
```

## How do I tail the log?

```powershell
Get-Content logs\app.log -Wait -Tail 50
```
Or in bash:
```bash
tail -f logs/app.log
```

## How do I restart just the server?

```powershell
.\scripts\stop-all.cmd
.\scripts\start-all.cmd
```

## Clean everything and start fresh

```powershell
.\scripts\stop-all.cmd
Remove-Item -Recurse -Force build, logs -ErrorAction SilentlyContinue
.\scripts\build.cmd
.\scripts\start-all.cmd
```
