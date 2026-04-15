#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
mkdir -p build/classes

SEP=":"
case "$(uname -s 2>/dev/null)" in MINGW*|MSYS*|CYGWIN*) SEP=";" ;; esac

DRIVER=$(ls lib/postgresql-*.jar 2>/dev/null | head -n 1)
if [ -z "$DRIVER" ]; then
  echo "ERROR: lib/postgresql-*.jar not found. Download from https://jdbc.postgresql.org/download/"
  exit 4
fi

javac -cp "$DRIVER" -d build/classes \
  shared/src/com/netflixsub/common/*.java \
  services/auth-service/src/com/netflixsub/auth/*.java \
  services/user-service/src/com/netflixsub/user/*.java \
  services/plan-service/src/com/netflixsub/plan/*.java \
  services/subscription-service/src/com/netflixsub/subscription/*.java \
  services/payment-service/src/com/netflixsub/payment/*.java \
  services/notification-service/src/com/netflixsub/notification/*.java \
  services/catalog-service/src/com/netflixsub/catalog/*.java \
  services/admin-service/src/com/netflixsub/admin/*.java \
  model/src/com/netflixsub/model/*.java \
  services/app/src/com/netflixsub/app/*.java
echo "Build successful."
