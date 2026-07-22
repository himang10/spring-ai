#!/bin/bash
# API 테스트 스크립트

BASE_URL="http://localhost:8082"

echo "=== STDIO MCP Server API Test ==="
echo ""

# Health Check
echo "1. Health Check"
curl -s "$BASE_URL/actuator/health" | jq '.'
echo ""

# Create User
echo "2. Create User"
USER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}')
echo $USER_RESPONSE | jq '.'
USER_ID=$(echo $USER_RESPONSE | jq -r '.id')
echo ""

# Get All Users
echo "3. Get All Users"
curl -s "$BASE_URL/api/users" | jq '.'
echo ""

# Get User by ID
echo "4. Get User by ID: $USER_ID"
curl -s "$BASE_URL/api/users/$USER_ID" | jq '.'
echo ""

# Search Users by Name
echo "5. Search Users by Name: John"
curl -s "$BASE_URL/api/users/search?name=John" | jq '.'
echo ""

# Create Product
echo "6. Create Product"
PRODUCT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"High-end laptop","price":1500.00,"stock":10}')
echo $PRODUCT_RESPONSE | jq '.'
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | jq -r '.id')
echo ""

# Get All Products
echo "7. Get All Products"
curl -s "$BASE_URL/api/products" | jq '.'
echo ""

# Get Product by ID
echo "8. Get Product by ID: $PRODUCT_ID"
curl -s "$BASE_URL/api/products/$PRODUCT_ID" | jq '.'
echo ""

# Search Products by Name
echo "9. Search Products by Name: Laptop"
curl -s "$BASE_URL/api/products/search?name=Laptop" | jq '.'
echo ""

echo "=== Test Completed ==="
