#!/bin/bash

# 사용법 출력
usage() {
    echo "Usage: $0 -h <host> -t <type> [-s <scenario>] [-p]"
    echo ""
    echo "Options:"
    echo "  -h <host>      Server IP or hostname (required)"
    echo "  -t <type>      App type: mvc or webflux (required)"
    echo "  -s <scenario>  Test scenario: smoke, load, stress, spike (default: load)"
    echo "  -p             Use Nginx proxy (port 80)"
    echo ""
    echo "Examples:"
    echo "  $0 -h 192.168.1.100 -t mvc -s smoke"
    echo "  $0 -h 192.168.1.100 -t webflux -s load"
    echo "  $0 -h localhost -t mvc -p"
    exit 1
}

# 기본값
SCENARIO="load"
USE_PROXY="false"

# 인자 파싱
while getopts "h:t:s:p" opt; do
    case $opt in
        h) TARGET_HOST="$OPTARG" ;;
        t) APP_TYPE="$OPTARG" ;;
        s) SCENARIO="$OPTARG" ;;
        p) USE_PROXY="true" ;;
        *) usage ;;
    esac
done

# 필수 인자 확인
if [ -z "$TARGET_HOST" ] || [ -z "$APP_TYPE" ]; then
    usage
fi

# APP_TYPE 검증
if [ "$APP_TYPE" != "mvc" ] && [ "$APP_TYPE" != "webflux" ]; then
    echo "Error: APP_TYPE must be 'mvc' or 'webflux'"
    exit 1
fi

# 스크립트 디렉토리로 이동
cd "$(dirname "$0")/.."

# 결과 디렉토리
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULT_DIR="../results/${APP_TYPE}_${SCENARIO}_${TIMESTAMP}"
mkdir -p "$RESULT_DIR"

echo "=========================================="
echo "Load Test Configuration"
echo "=========================================="
echo "  Target Host: $TARGET_HOST"
echo "  App Type:    $APP_TYPE"
echo "  Scenario:    $SCENARIO"
echo "  Use Proxy:   $USE_PROXY"
echo "  Results:     $RESULT_DIR"
echo "=========================================="
echo ""

# k6 실행
k6 run \
    --env TARGET_HOST="$TARGET_HOST" \
    --env APP_TYPE="$APP_TYPE" \
    --env USE_PROXY="$USE_PROXY" \
    --out json="$RESULT_DIR/result.json" \
    "k6/scenarios/${SCENARIO}-test.js" \
    2>&1 | tee "$RESULT_DIR/output.log"

echo ""
echo "=========================================="
echo "Test completed. Results saved to: $RESULT_DIR"
echo "=========================================="