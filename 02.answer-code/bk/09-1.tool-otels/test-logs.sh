#!/bin/bash

curl -s -G "https://loki.skala25a.project.skala-ai.com/loki/api/v1/query_range" \
  --data-urlencode 'query={service_name="spring-ai-tools"}' \
  --data-urlencode "start=$(date -u -v-1H +%s)000000000" \
  --data-urlencode "end=$(date -u +%s)000000000" \
  --data-urlencode 'limit=20' | jq -r '.data.result[0].values[]' | head -20

curl -s -G "https://loki.skala25a.project.skala-ai.com/loki/api/v1/query_range" \
  --data-urlencode 'query={service_name="spring-ai-tools"}' \
  --data-urlencode "start=$(date -u -v-2H +%s)000000000" \
  --data-urlencode "end=$(date -u +%s)000000000" \
  --data-urlencode 'limit=50' | jq '.'  