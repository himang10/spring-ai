#!/bin/bash

echo "Testing trace collection pipeline..."

echo "1. Generating traces via API"
curl -s "https://trace-call-app.skala25a.project.skala-ai.com/api/test/traces" > /dev/null
curl -s "https://trace-call-app.skala25a.project.skala-ai.com/api/test/chain" > /dev/null
echo "API calls completed"

sleep 5

echo "2. Checking Beyla logs for HTTP traces"
kubectl logs -n observability beyla-bqtxc --tail=20 | grep -i "http\|trace\|span" || echo "No HTTP traces in Beyla logs"

echo "3. Checking Alloy receiver for OTLP data"
kubectl logs -n observability grafana-k8s-monitoring-alloy-receiver-2gfvz -c alloy --tail=30 | grep -E "(otlp|tempo|trace)" || echo "No OTLP data in Alloy"

echo "4. Checking Tempo for stored traces"
kubectl port-forward -n observability svc/tempo 3202:3200 &
PF_PID=$!
sleep 2
TRACE_COUNT=$(curl -s "http://localhost:3202/api/search" | jq -r '.traces | length')
echo "Traces in Tempo: ${TRACE_COUNT}"
kill $PF_PID 2>/dev/null

echo "Test complete."