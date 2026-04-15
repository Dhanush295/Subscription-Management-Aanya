#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."

mkdir -p build/classes

DRIVER=$(ls lib/postgresql-*.jar 2>/dev/null | head -n 1)
if [ -z "$DRIVER" ]; then
  echo "ERROR: No postgresql-*.jar found in lib/"
  echo "       Download postgresql-42.7.4.jar from https://jdbc.postgresql.org/download/ and place it in lib/"
  exit 4
fi

javac -d build/classes shared/src/com/netflixsub/common/DbCheck.java

SEP=":"
case "$(uname -s 2>/dev/null)" in MINGW*|MSYS*|CYGWIN*) SEP=";" ;; esac
java -cp "build/classes${SEP}${DRIVER}" com.netflixsub.common.DbCheck
