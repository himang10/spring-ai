#!/bin/bash
# STDIO MCP Server 빌드 및 실행 스크립트

echo "=== STDIO MCP Server Build & Run ==="

# 프로젝트 디렉토리로 이동
cd "$(dirname "$0")"

# Maven 빌드
echo "Building project..."
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo ""
    echo "Starting STDIO MCP Server..."
    java -jar target/stdio-mcp-server-1.0.0.jar
else
    echo "Build failed!"
    exit 1
fi
