#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
mkdir -p logs

SEP=":"
case "$(uname -s 2>/dev/null)" in MINGW*|MSYS*|CYGWIN*) SEP=";" ;; esac

DRIVER=$(ls lib/postgresql-*.jar 2>/dev/null | head -n 1)
if [ -z "$DRIVER" ]; then
  echo "ERROR: lib/postgresql-*.jar not found."
  exit 4
fi

java -cp "build/classes${SEP}${DRIVER}" com.netflixsub.app.App > logs/app.log 2>&1 &
echo $! > logs/app.pid
sleep 1
echo "Started netflix.sub (pid $(cat logs/app.pid)) on http://localhost:8080"
