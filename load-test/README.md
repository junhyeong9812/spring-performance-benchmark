# Load Test Guide

k6를 사용한 부하 테스트 가이드입니다.

## 사전 요구사항

### k6 설치
```bash
# macOS
brew install k6

# Windows (Chocolatey)
choco install k6

# Windows (winget)
winget install k6

# Linux (Debian/Ubuntu)
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

## 사용법

### 환경 변수

| 변수 | 설명 | 기본값 | 예시 |
|------|------|--------|------|
| `TARGET_HOST` | 서버 IP 또는 호스트명 | `localhost` | `192.168.1.100` |
| `APP_TYPE` | 테스트 대상 앱 | `mvc` | `mvc` 또는 `webflux` |
| `USE_PROXY` | Nginx 프록시 사용 여부 | `false` | `true` |

### 테스트 실행 예시

#### 로컬 테스트 (1대 PC)
```bash
# MVC 앱 직접 테스트 (포트 8080)
k6 run --env TARGET_HOST=localhost --env APP_TYPE=mvc k6/scenarios/smoke-test.js

# WebFlux 앱 직접 테스트 (포트 8081)
k6 run --env TARGET_HOST=localhost --env APP_TYPE=webflux k6/scenarios/smoke-test.js

# Nginx 프록시 경유 테스트 (포트 80)
k6 run --env TARGET_HOST=localhost --env APP_TYPE=mvc --env USE_PROXY=true k6/scenarios/smoke-test.js
```

#### 원격 테스트 (2대 PC)
```bash
# Server PC IP: 192.168.1.100

# MVC 테스트
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc k6/scenarios/load-test.js

# WebFlux 테스트
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=webflux k6/scenarios/load-test.js
```

## 테스트 전 체크리스트

1. [ ] Server PC에서 Docker Compose 실행 확인
2. [ ] 앱 헬스체크 통과 확인: `curl http://SERVER_IP/mvc/api/info`
3. [ ] Prometheus/Grafana 접근 확인
4. [ ] 네트워크 연결 확인
5. [ ] 방화벽 포트 오픈 확인 (80, 8080, 8081, 9090, 3000)