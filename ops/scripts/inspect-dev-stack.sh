#!/usr/bin/env bash
# Detailed, read-only inspection of the running P0 development stack, for the evidence record.
# Complements verify-dev-stack.sh (pass/fail gate) with the facts behind each check.
# Usage: bash ops/scripts/inspect-dev-stack.sh > docs/evidence/p0/dev-stack-inspection.txt
# Uses only the development-only example values; prints no other credentials.
set -uo pipefail

cd "$(dirname "$0")/../.."
ENV_FILE="${ENV_FILE:-ops/compose/.env}"
C=(docker compose -f ops/compose/compose.yaml --env-file "$ENV_FILE")
val() { sed -n "s/^$1=//p" "$ENV_FILE" | tail -1; }
IDP_PORT="$(val IDP_PORT)"; IDP_PORT="${IDP_PORT:-8081}"
S3_PORT="$(val S3_PORT)"; S3_PORT="${S3_PORT:-8333}"
S3_KEYS="$(python -c "import json;i=json.load(open('ops/compose/seaweedfs/s3.dev.json'))['identities'][0]['credentials'][0];print(i['accessKey']+':'+i['secretKey'])")"
section() { printf '\n== %s\n' "$1"; }

section "Context"
echo "date_utc: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo "git_head: $(git rev-parse --short HEAD)"
echo "docker_server: $(docker version --format '{{.Server.Version}}')"
echo "compose: $(docker compose version --short)"

section "Images (configured reference -> local RepoDigest)"
for img in $("${C[@]}" config --images); do
  echo "$img -> $(docker image inspect --format '{{join .RepoDigests ","}}' "$img")"
done

section "Services"
"${C[@]}" ps --format '{{.Service}}  {{.Status}}  {{.Ports}}'

section "Published ports (must all be 127.0.0.1)"
for s in postgres object-store idp doc-worker-stub; do
  echo "$s: $(docker port "$("${C[@]}" ps -q "$s")" | tr '\n' ' ')"
done

section "PostgreSQL"
"${C[@]}" exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tA' <<'SQL'
select 'version: ' || version();
select 'user_tables: ' || count(*) from pg_tables where schemaname not in ('pg_catalog', 'information_schema');
select 'rls_policies: ' || count(*) from pg_policy;
select 'tables_with_rls_enabled: ' || count(*) from pg_class where relrowsecurity;
select 'non_system_roles: ' || string_agg(rolname, ',') from pg_roles where rolname !~ '^pg_';
SQL

section "SeaweedFS S3 (dev-only substitute, ADR-0005)"
echo "anonymous ListBuckets http: $(curl -s -o /dev/null -w '%{http_code}' "http://127.0.0.1:${S3_PORT}/")"
echo "wrong-secret ListBuckets http: $(curl -s -o /dev/null -w '%{http_code}' --aws-sigv4 'aws:amz:us-east-1:s3' --user "${S3_KEYS%%:*}:wrong" "http://127.0.0.1:${S3_PORT}/")"
echo "dev-key ListBuckets: $(curl -s -w ' http=%{http_code}' --aws-sigv4 'aws:amz:us-east-1:s3' --user "$S3_KEYS" "http://127.0.0.1:${S3_PORT}/")"
for p in 9333 8080 8888 18080 19333; do
  echo "internal port $p reachable from host: $(curl -s -m 2 -o /dev/null -w '%{http_code}' "http://127.0.0.1:$p/" 2>/dev/null | grep -qE '^[1-5]' && echo yes || echo no)"
done

section "Keycloak dev IdP"
curl -s "http://127.0.0.1:${IDP_PORT}/realms/fineprint-dev/.well-known/openid-configuration" \
  | python -c "import sys,json;d=json.load(sys.stdin);print('issuer:',d['issuer'])"
echo "password grant (direct access grants must be off): $(curl -s -X POST "http://127.0.0.1:${IDP_PORT}/realms/fineprint-dev/protocol/openid-connect/token" -d grant_type=password -d client_id=fineprint-web-dev -d username=synthetic.user -d password=x)"
plain="$(curl -s -o /dev/null -w '%{http_code} %{redirect_url}' "http://127.0.0.1:${IDP_PORT}/realms/fineprint-dev/protocol/openid-connect/auth?client_id=fineprint-web-dev&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A5173%2Fcb&scope=openid&code_challenge=abc&code_challenge_method=plain")"
echo "auth request with PKCE method 'plain' (client requires S256): $plain"
nopkce="$(curl -s -o /dev/null -w '%{http_code} %{redirect_url}' "http://127.0.0.1:${IDP_PORT}/realms/fineprint-dev/protocol/openid-connect/auth?client_id=fineprint-web-dev&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A5173%2Fcb&scope=openid")"
echo "auth request without PKCE: $nopkce"
echo "management port 9000 reachable from host: $(curl -s -m 2 -o /dev/null -w '%{http_code}' http://127.0.0.1:9000/health/ready | grep -qE '^[1-5]' && echo yes || echo no)"

section "Inert worker stub"
W="$("${C[@]}" ps -q doc-worker-stub)"
docker inspect -f 'NetworkMode={{.HostConfig.NetworkMode}} ReadonlyRootfs={{.HostConfig.ReadonlyRootfs}} User={{.Config.User}} CapDrop={{.HostConfig.CapDrop}} CapAdd={{.HostConfig.CapAdd}} SecurityOpt={{.HostConfig.SecurityOpt}} PidsLimit={{.HostConfig.PidsLimit}} MemoryBytes={{.HostConfig.Memory}} Privileged={{.HostConfig.Privileged}}' "$W"
echo "interfaces: $(docker exec "$W" cat /proc/net/dev | tail -n +3 | cut -d: -f1 | tr -d ' ' | tr '\n' ' ')"
echo "egress to 1.1.1.1: $(docker exec "$W" sh -c 'wget -q -T 3 -O /dev/null http://1.1.1.1/ 2>&1 && echo REACHABLE || echo blocked')"
echo "write to root fs: $(docker exec "$W" sh -c 'touch /probe 2>/dev/null && echo WRITABLE || echo read-only')"
echo "identity: $(docker exec "$W" id)"
echo "effective capabilities: $(docker exec "$W" sh -c 'grep CapEff /proc/1/status')"
echo "process list: $(docker exec "$W" ps -o pid,user,args | tail -n +2 | tr -s ' ' | tr '\n' ';')"
echo "log: $("${C[@]}" logs --no-log-prefix doc-worker-stub | head -1)"

section "Compose network (non-worker services)"
docker network inspect fineprint-dev_default -f 'name={{.Name}} internal={{.Internal}} driver={{.Driver}}'
