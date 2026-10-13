#!/bin/bash
# Prepares a Claude Code cloud session to build, test and run GreatBooks:
# JDK 21, a local PostgreSQL with the app's database, and the Gradle dependencies.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

cd "$CLAUDE_PROJECT_DIR"

DB_NAME=greatbooks
DB_USER=greatbooks
DB_PASSWORD=greatbooks

# --- JDK 21 and PostgreSQL (usually preinstalled in the cloud image) ---
missing=()
java -version 2>&1 | grep -q 'version "21' || missing+=(openjdk-21-jdk-headless)
command -v pg_ctlcluster >/dev/null || missing+=(postgresql)
if [ ${#missing[@]} -gt 0 ]; then
  apt-get update -qq
  DEBIAN_FRONTEND=noninteractive apt-get install -y -qq "${missing[@]}"
fi

# --- Start PostgreSQL (processes don't survive the container snapshot, so do this every session) ---
if ! pg_lsclusters --no-header | grep -q .; then
  pg_createcluster "$(ls /usr/lib/postgresql | sort -n | tail -1)" main
fi
read -r PG_VERSION PG_CLUSTER _ < <(pg_lsclusters --no-header | head -1)
pg_ctlcluster "$PG_VERSION" "$PG_CLUSTER" start >/dev/null 2>&1 || true
for _ in $(seq 1 30); do
  pg_isready -q -h 127.0.0.1 -p 5432 && break
  sleep 1
done

# --- App role and database (idempotent) ---
psql_admin() { su postgres -c "psql -v ON_ERROR_STOP=1 -qtAc \"$1\""; }
if [ "$(psql_admin "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'")" != "1" ]; then
  psql_admin "CREATE ROLE $DB_USER LOGIN PASSWORD '$DB_PASSWORD'"
fi
if [ "$(psql_admin "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'")" != "1" ]; then
  psql_admin "CREATE DATABASE $DB_NAME OWNER $DB_USER"
fi

# --- Point the app, tests and seed scripts at it (same settings as compose.yaml) ---
if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
  cat >> "$CLAUDE_ENV_FILE" <<ENV
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/$DB_NAME
export SPRING_DATASOURCE_USERNAME=$DB_USER
export SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD
ENV
fi

# --- Download Gradle and dependencies, compile main/test/scripts (cached in the snapshot) ---
# Maven Central rate-limits (429) through the cloud proxy, so resolve from a mirror.
mkdir -p "$HOME/.gradle/init.d"
cp .claude/hooks/maven-mirror.init.gradle "$HOME/.gradle/init.d/"
# Retry transient failures; already-downloaded artifacts stay cached between attempts.
for attempt in 1 2 3; do
  ./gradlew --no-daemon --quiet classes testClasses scriptsClasses && exit 0
  echo "Gradle dependency download failed (attempt $attempt), retrying..." >&2
  sleep $((attempt * 10))
done
exit 1
