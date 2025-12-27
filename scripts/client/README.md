# Client Scripts

Client PC에서 k6 부하 테스트를 실행하기 위한 스크립트입니다.

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
sudo apt-get update
sudo apt-get install k6
```

---

## 스크립트 목록

### 1. `run-load-test.sh` (Linux / macOS)

k6 부하 테스트 실행 스크립트

```bash
./run-load-test.sh -h <host> -t <type> [-s <scenario>] [-p]
```

**옵션:**

| 옵션 | 설명 | 필수 | 기본값 |
|------|------|------|--------|
| `-h <host>` | 서버 IP 또는 호스트명 | ✅ | - |
| `-t <type>` | 앱 타입 (`mvc` / `webflux`) | ✅ | - |
| `-s <scenario>` | 테스트 시나리오 | ❌ | `load` |
| `-p` | Nginx 프록시 사용 (포트 80) | ❌ | `false` |

**시나리오 종류:**
- `smoke` - 기본 동작 확인 (5 VUs, 1분)
- `load` - 일반 부하 테스트 (100 VUs, 10분)
- `stress` - 고부하 테스트 (300 VUs, 10분)
- `spike` - 급격한 트래픽 테스트 (10↔500 VUs)

**사용 예시:**

```bash
# MVC 스모크 테스트
./run-load-test.sh -h 192.168.1.100 -t mvc -s smoke

# WebFlux 부하 테스트
./run-load-test.sh -h 192.168.1.100 -t webflux -s load

# MVC 스트레스 테스트 (Nginx 프록시 경유)
./run-load-test.sh -h 192.168.1.100 -t mvc -s stress -p

# 로컬 테스트
./run-load-test.sh -h localhost -t mvc -s smoke
```

**실행 내용:**
1. 입력 파라미터 검증
2. 결과 저장 디렉토리 생성 (`results/{type}_{scenario}_{timestamp}/`)
3. k6 실행 및 결과 저장
4. 콘솔 출력을 로그 파일로 저장

**출력 파일:**
```
results/
└── mvc_load_20241227_143052/
    ├── result.json    # k6 상세 결과 (JSON)
    └── output.log     # 콘솔 출력 로그
```

---

### 2. `run-load-test.bat` (Windows)

Windows용 k6 부하 테스트 실행 스크립트

```batch
run-load-test.bat -h <host> -t <type> [-s <scenario>] [-p]
```

**사용 예시:**

```batch
:: MVC 스모크 테스트
run-load-test.bat -h 192.168.1.100 -t mvc -s smoke

:: WebFlux 부하 테스트
run-load-test.bat -h 192.168.1.100 -t webflux -s load

:: 로컬 테스트
run-load-test.bat -h localhost -t mvc -s smoke
```

**동작 방식:**
- Linux 버전과 동일한 기능
- `wmic`을 사용한 타임스탬프 생성
- 결과는 `results\` 폴더에 저장

---

## 사용 시나리오

### 시나리오 1: 빠른 동작 확인 (Smoke Test)

```bash
# 서버가 정상 동작하는지 확인
./run-load-test.sh -h 192.168.1.100 -t mvc -s smoke
./run-load-test.sh -h 192.168.1.100 -t webflux -s smoke
```

### 시나리오 2: MVC vs WebFlux 성능 비교

```bash
# MVC 부하 테스트
./run-load-test.sh -h 192.168.1.100 -t mvc -s load

# WebFlux 부하 테스트
./run-load-test.sh -h 192.168.1.100 -t webflux -s load

# 결과 비교
ls -la ../results/
```

### 시나리오 3: 한계 성능 측정 (Stress Test)

```bash
# MVC 스트레스 테스트
./run-load-test.sh -h 192.168.1.100 -t mvc -s stress

# WebFlux 스트레스 테스트
./run-load-test.sh -h 192.168.1.100 -t webflux -s stress
```

### 시나리오 4: 급격한 트래픽 대응 (Spike Test)

```bash
# MVC 스파이크 테스트
./run-load-test.sh -h 192.168.1.100 -t mvc -s spike

# WebFlux 스파이크 테스트
./run-load-test.sh -h 192.168.1.100 -t webflux -s spike
```

---

## 직접 k6 실행

스크립트 없이 직접 k6를 실행할 수도 있습니다:

```bash
# 기본 실행
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc \
    ../load-test/k6/scenarios/load-test.js

# 결과 JSON 출력
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc \
    --out json=result.json \
    ../load-test/k6/scenarios/load-test.js

# Nginx 프록시 경유
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc \
    --env USE_PROXY=true \
    ../load-test/k6/scenarios/load-test.js
```

---

## 트러블슈팅

### 연결 실패

```bash
# 서버 접근 확인
curl http://<SERVER_IP>:8080/api/info
curl http://<SERVER_IP>:8081/api/info

# 방화벽 확인 (Server PC에서)
sudo ufw status
sudo ufw allow 8080
sudo ufw allow 8081
```

### k6 설치 확인

```bash
k6 version
```

### 결과 파일 확인

```bash
# 결과 디렉토리 확인
ls -la ../results/

# JSON 결과 분석
cat ../results/mvc_load_*/result.json | jq '.metrics'
```