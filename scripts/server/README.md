# Server Scripts

Server PC에서 Docker 기반 벤치마크 환경을 실행하기 위한 스크립트입니다.

## 사전 요구사항

- Java 25
- Gradle 9.2.1+
- Docker & Docker Compose

## 스크립트 목록

### 1. `start-all.sh` / `start-all.bat`

**전체 스택 실행** - MVC + WebFlux + Nginx + Prometheus + Grafana

```bash
# Linux / macOS
./start-all.sh

# Windows
start-all.bat
```

**실행 내용:**
1. Gradle로 전체 프로젝트 빌드
2. Docker Compose로 모든 서비스 시작
3. 헬스체크 대기 (10초)
4. 서비스 상태 출력

**시작되는 서비스:**
| 서비스 | 포트 | 설명 |
|--------|------|------|
| Nginx | 80 | 리버스 프록시 |
| MVC App | 8080 | Spring MVC + Tomcat |
| WebFlux App | 8081 | Spring WebFlux + Netty |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 대시보드 (admin/admin) |

**접속 URL:**
- MVC API: http://localhost/mvc/api/info
- WebFlux API: http://localhost/webflux/api/info
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

---

### 2. `start-mvc.sh` / `start-mvc.bat`

**MVC 단독 실행** - MVC + Prometheus + Grafana (WebFlux 제외)

```bash
# Linux / macOS
./start-mvc.sh

# Windows
start-mvc.bat
```

**실행 내용:**
1. MVC 모듈만 빌드
2. `docker-compose.mvc.yml`로 서비스 시작

**시작되는 서비스:**
| 서비스 | 포트 | 설명 |
|--------|------|------|
| MVC App | 80, 8080 | Spring MVC + Tomcat |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 대시보드 |

**접속 URL:**
- MVC API: http://localhost:8080/api/info
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

---

### 3. `start-webflux.sh` / `start-webflux.bat`

**WebFlux 단독 실행** - WebFlux + Prometheus + Grafana (MVC 제외)

```bash
# Linux / macOS
./start-webflux.sh

# Windows
start-webflux.bat
```

**실행 내용:**
1. WebFlux 모듈만 빌드
2. `docker-compose.webflux.yml`로 서비스 시작

**시작되는 서비스:**
| 서비스 | 포트 | 설명 |
|--------|------|------|
| WebFlux App | 80, 8081 | Spring WebFlux + Netty |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 대시보드 |

**접속 URL:**
- WebFlux API: http://localhost:8081/api/info
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

---

### 4. `stop-all.sh` / `stop-all.bat`

**모든 서비스 중지**

```bash
# Linux / macOS
./stop-all.sh

# Windows
stop-all.bat
```

**실행 내용:**
1. `docker-compose.yml` 서비스 중지
2. `docker-compose.mvc.yml` 서비스 중지
3. `docker-compose.webflux.yml` 서비스 중지

---

## 사용 시나리오

### 시나리오 1: MVC vs WebFlux 동시 비교

```bash
# 전체 스택 실행
./start-all.sh

# Client PC에서 MVC 테스트
k6 run --env TARGET_HOST=<SERVER_IP> --env APP_TYPE=mvc ...

# Client PC에서 WebFlux 테스트
k6 run --env TARGET_HOST=<SERVER_IP> --env APP_TYPE=webflux ...

# 종료
./stop-all.sh
```

### 시나리오 2: MVC만 집중 테스트

```bash
# MVC만 실행 (리소스 독점)
./start-mvc.sh

# Client PC에서 테스트
k6 run --env TARGET_HOST=<SERVER_IP> --env APP_TYPE=mvc ...

# 종료
./stop-all.sh
```

### 시나리오 3: WebFlux만 집중 테스트

```bash
# WebFlux만 실행 (리소스 독점)
./start-webflux.sh

# Client PC에서 테스트
k6 run --env TARGET_HOST=<SERVER_IP> --env APP_TYPE=webflux ...

# 종료
./stop-all.sh
```

---

## 트러블슈팅

### 포트 충돌

```bash
# 사용 중인 포트 확인
netstat -tulpn | grep -E '80|8080|8081|9090|3000'

# 기존 컨테이너 강제 삭제
docker rm -f nginx mvc-app webflux-app prometheus grafana
```

### 빌드 실패

```bash
# Gradle 캐시 삭제 후 재빌드
./gradlew clean build -x test --refresh-dependencies
```

### 컨테이너 로그 확인

```bash
# 전체 로그
docker compose logs -f

# 특정 서비스 로그
docker logs -f mvc-app
docker logs -f webflux-app
```