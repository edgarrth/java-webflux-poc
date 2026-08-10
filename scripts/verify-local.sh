#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_DIR="$ROOT_DIR/infraestructura"
APP_PID=""

cleanup() {
  if [[ -n "$APP_PID" ]] && kill -0 "$APP_PID" 2>/dev/null; then
    kill "$APP_PID" || true
  fi
}
trap cleanup EXIT

echo "[1/5] Recreating PostgreSQL 18 infrastructure"
(
  cd "$COMPOSE_DIR"
  docker compose down -v --remove-orphans
  docker compose up -d
)

echo "[2/5] Waiting for PostgreSQL health"
for _ in {1..30}; do
  status="$(docker inspect -f '{{.State.Health.Status}}' webflux-payments-postgres 2>/dev/null || true)"
  if [[ "$status" == "healthy" ]]; then
    break
  fi
  sleep 2
done
[[ "$(docker inspect -f '{{.State.Health.Status}}' webflux-payments-postgres)" == "healthy" ]]

echo "[3/5] Running Maven tests"
cd "$ROOT_DIR"
mvn clean test

echo "[4/5] Starting application"
mvn spring-boot:run >/tmp/webflux-payment-poc.log 2>&1 &
APP_PID=$!
for _ in {1..45}; do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
    break
  fi
  sleep 2
done
curl -fsS http://localhost:8080/actuator/health

echo
echo "[5/5] Running API smoke flow"
create_response="$(curl -fsS -X POST http://localhost:8080/payments/v1/payments \
  -H 'Content-Type: application/json' \
  -d '{"merchantId":"merchant-smoke","customerId":"customer-smoke","amount":145.90,"currency":"PEN"}')"

payment_id="$(python3 -c 'import json,sys; print(json.loads(sys.stdin.read())["paymentId"])' <<<"$create_response")"
[[ -n "$payment_id" ]]

curl -fsS "http://localhost:8080/payments/v1/payments/$payment_id" >/dev/null
curl -fsS -X POST "http://localhost:8080/payments/v1/payments/$payment_id/authorizations" \
  | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"] == "AUTHORIZED"'
curl -fsS -X POST "http://localhost:8080/payments/v1/payments/$payment_id/settlements" \
  | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"] == "SETTLED"'

echo
echo "Checking NDJSON streaming orchestration"
curl -fsS --max-time 15 http://localhost:8080/payments/v1/streaming-demos/orchestrations/ndjson \
  | tee /tmp/webflux-streaming-demo.ndjson \
  | python3 -c 'import json,sys; rows=[json.loads(line) for line in sys.stdin if line.strip()]; assert len(rows) == 5; assert rows[-1]["step"] == "completed"'

echo "Verification completed successfully. paymentId=$payment_id"
