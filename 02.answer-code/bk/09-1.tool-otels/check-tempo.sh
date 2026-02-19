#!/bin/bash

echo "Checking Alloy -> Tempo trace collection..."

# 1. Check Alloy receiver logs for OTLP traces
echo "=== Alloy Receiver OTLP Logs ==="
kubectl logs -n observability grafana-k8s-monitoring-alloy-receiver-2gfvz -c alloy --tail=20 | grep -i "trace\|tempo\|otlp" || echo "No trace logs found in Alloy receiver"

# 2. Check Tempo pod status
echo "=== Tempo Pod Status ==="
kubectl get pods -n observability -l app=tempo

# 3. Check Tempo API for traces
echo "=== Tempo API Search ==="
kubectl port-forward -n observability svc/tempo 3201:3200 &
PF_PID=$!
sleep 3

# Search for traces in last hour
TRACES=$(curl -s "http://localhost:3201/api/search?limit=10" || echo "Failed to connect to Tempo")
echo "$TRACES"

# 4. Check for specific service traces  
echo "=== Search for HTTP traces ==="
SERVICE_TRACES=$(curl -s "http://localhost:3201/api/search?tags=span.kind%3Dserver&limit=5" || echo "Failed to search traces")
echo "$SERVICE_TRACES"

# Clean up port-forward
kill $PF_PID 2>/dev/null

echo "Check complete."