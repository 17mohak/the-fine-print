#!/usr/bin/env bash
# Verifies the P0 development stack (acceptance criterion 5). Requires a running Docker engine and a stack
# started with:  docker compose -f ops/compose/compose.yaml --env-file ops/compose/.env up -d --wait
# Exit status is non-zero if any check fails. Prints no secrets.
set -euo pipefail

cd "$(dirname "$0")/../.."
COMPOSE=(docker compose -f ops/compose/compose.yaml --env-file "${ENV_FILE:-ops/compose/.env}")
IDP_PORT="${IDP_PORT:-8081}"
S3_PORT="${S3_PORT:-8333}"
failures=0

check() {
  local name="$1"; shift
  if "$@" >/dev/null 2>&1; then
    echo "PASS  $name"
  else
    echo "FAIL  $name"
    failures=$((failures + 1))
  fi
}

health() {
  local id
  id="$("${COMPOSE[@]}" ps -q "$1")"
  [ -n "$id" ] && [ "$(docker inspect -f '{{.State.Health.Status}}' "$id")" = "healthy" ]
}

for svc in postgres object-store idp doc-worker-stub; do
  check "$svc container is healthy" health "$svc"
done

check "postgres accepts connections" \
  "${COMPOSE[@]}" exec -T postgres sh -c 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
check "postgres has no application schema yet (P1)" \
  "${COMPOSE[@]}" exec -T postgres sh -c \
  'test "$(psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc "select count(*) from pg_tables where schemaname not in ('"'"'pg_catalog'"'"','"'"'information_schema'"'"')")" = 0'
check "object store answers S3 requests on 127.0.0.1:${S3_PORT}" \
  sh -c "curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:${S3_PORT}/ | grep -qE '^(200|403)$'"
check "dev IdP publishes OIDC discovery for realm fineprint-dev" \
  sh -c "curl -sf http://127.0.0.1:${IDP_PORT}/realms/fineprint-dev/.well-known/openid-configuration | grep -q '\"issuer\"'"
check "worker stub has no network (network_mode none)" \
  sh -c "test \"\$(docker inspect -f '{{.HostConfig.NetworkMode}}' \$(${COMPOSE[*]} ps -q doc-worker-stub))\" = none"
check "worker stub announces it is an inert stub" \
  sh -c "${COMPOSE[*]} logs doc-worker-stub | grep -q 'doc-worker STUB (P0)'"
check "published ports bind to loopback only" \
  sh -c "! ${COMPOSE[*]} ps --format '{{.Ports}}' | grep -E '0\.0\.0\.0:|\[::\]:'"

if [ "$failures" -ne 0 ]; then
  echo "$failures check(s) failed"
  exit 1
fi
echo "All dev-stack checks passed"
