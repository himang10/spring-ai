#!/bin/bash

# Langfuse 시크릿 생성 도우미 스크립트

set -e

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

info() {
    echo -e "${GREEN}$1${NC}"
}

step() {
    echo -e "${BLUE}$1${NC}"
}

warn() {
    echo -e "${YELLOW}$1${NC}"
}

echo ""
step "=========================================="
step "  Langfuse 시크릿 생성 도우미"
step "=========================================="
echo ""

# 1. NextAuth Secret
info "1. NextAuth Secret (32자 hex) 생성 중..."
NEXTAUTH_SECRET=$(openssl rand -hex 32)
NEXTAUTH_SECRET_BASE64=$(echo -n "$NEXTAUTH_SECRET" | base64)
echo "   원본: $NEXTAUTH_SECRET"
echo "   Base64: $NEXTAUTH_SECRET_BASE64"
echo ""

# 2. Salt
info "2. Salt 값 생성 중..."
SALT=$(openssl rand -hex 16)
SALT_BASE64=$(echo -n "$SALT" | base64)
echo "   원본: $SALT"
echo "   Base64: $SALT_BASE64"
echo ""

# 3. PostgreSQL Password
info "3. PostgreSQL 비밀번호 생성 중..."
POSTGRES_PASSWORD=$(openssl rand -base64 24)
POSTGRES_PASSWORD_BASE64=$(echo -n "$POSTGRES_PASSWORD" | base64)
echo "   원본: $POSTGRES_PASSWORD"
echo "   Base64: $POSTGRES_PASSWORD_BASE64"
echo ""

# 4. Redis Password
info "4. Redis 비밀번호 생성 중..."
REDIS_PASSWORD=$(openssl rand -base64 24)
REDIS_PASSWORD_BASE64=$(echo -n "$REDIS_PASSWORD" | base64)
echo "   원본: $REDIS_PASSWORD"
echo "   Base64: $REDIS_PASSWORD_BASE64"
echo ""

# 5. Encryption Key (선택사항)
info "5. Encryption Key 생성 중..."
ENCRYPTION_KEY=$(openssl rand -hex 32)
ENCRYPTION_KEY_BASE64=$(echo -n "$ENCRYPTION_KEY" | base64)
echo "   원본: $ENCRYPTION_KEY"
echo "   Base64: $ENCRYPTION_KEY_BASE64"
echo ""

step "=========================================="
step "  생성된 시크릿 값 요약"
step "=========================================="
echo ""

echo "다음 값들을 secret.yaml 파일에 복사하세요:"
echo ""
echo "nextauth-secret: $NEXTAUTH_SECRET_BASE64"
echo "salt: $SALT_BASE64"
echo "postgres-password: $POSTGRES_PASSWORD_BASE64"
echo "redis-password: $REDIS_PASSWORD_BASE64"
echo "encryption-key: $ENCRYPTION_KEY_BASE64"
echo ""

warn "주의: 이 값들을 안전한 곳에 보관하세요!"
echo ""

# secret.yaml 파일 자동 업데이트 옵션
read -p "secret.yaml 파일을 자동으로 업데이트하시겠습니까? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    SECRET_FILE="$SCRIPT_DIR/secret.yaml"

    if [ -f "$SECRET_FILE" ]; then
        info "secret.yaml 파일 업데이트 중..."

        # 백업 생성
        cp "$SECRET_FILE" "$SECRET_FILE.backup"

        # 값 업데이트
        sed -i.tmp "s|nextauth-secret: .*|nextauth-secret: $NEXTAUTH_SECRET_BASE64|g" "$SECRET_FILE"
        sed -i.tmp "s|salt: .*|salt: $SALT_BASE64|g" "$SECRET_FILE"
        sed -i.tmp "s|postgres-password: .*|postgres-password: $POSTGRES_PASSWORD_BASE64|g" "$SECRET_FILE"
        sed -i.tmp "s|redis-password: .*|redis-password: $REDIS_PASSWORD_BASE64|g" "$SECRET_FILE"

        # 임시 파일 삭제
        rm -f "$SECRET_FILE.tmp"

        info "secret.yaml 파일이 업데이트되었습니다!"
        info "백업 파일: $SECRET_FILE.backup"
    else
        warn "secret.yaml 파일을 찾을 수 없습니다: $SECRET_FILE"
    fi
fi

echo ""
info "시크릿 생성이 완료되었습니다!"
