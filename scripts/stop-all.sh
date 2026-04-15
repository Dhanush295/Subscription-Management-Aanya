#!/usr/bin/env bash
cd "$(dirname "$0")/.."
if [ -f logs/app.pid ]; then
  kill "$(cat logs/app.pid)" 2>/dev/null && echo "Stopped."
  rm -f logs/app.pid
fi
