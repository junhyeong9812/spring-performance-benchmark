# k6 Test Scenarios

k6 부하 테스트 시나리오 파일들입니다.

## 시나리오 개요

| 파일 | 시나리오 | VUs | 시간 | 목적 |
|------|---------|-----|------|------|
| `smoke-test.js` | Smoke | 5 | 1분 | 기본 동작 확인 |
| `load-test.js` | Load | 50→100 | 10분 | 일반 부하 성능 |
| `stress-test.js` | Stress | 100→300 | 10분 | 고부하 한계 확인 |
| `spike-test.js` | Spike | 10↔500 | 6분 | 급격한 트래픽 대응 |

---

## 1. smoke-test.js

### 목적
- 서버가 정상적으로 동작하는지 **기본 확인**
- 배포 후 빠른 검증에 적합
- 본격적인 부하 테스트 전 사전 점검

### 설정
```javascript
export const options = {
    vus: 5,           // 동시 사용자 5명
    duration: '1m',   // 1분간 실행
};
```

### 테스트 흐름
1. `/api/simple` - 단순 응답 테스트
2. `/api/delay/100` - 100ms 지연 응답
3. `/api/cpu/1000` - CPU 1000회 반복 연산
4. `/api/db/users` - DB 조회

### Thresholds (성공 기준)
- P95 응답시간 < 500ms
- P99 응답시간 < 1000ms
- 에러율 < 1%

### 사용 시점
- 서버 배포 직후
- 설정 변경 후
- 네트워크 연결 확인

```bash
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc smoke-test.js
```

---

## 2. load-test.js

### 목적
- **일반적인 부하 상황**에서의 성능 측정
- MVC vs WebFlux 성능 비교의 주요 시나리오
- 응답시간, 처리량, 에러율 종합 분석

### 설정
```javascript
export const options = {
    stages: [
        { duration: '1m', target: 50 },   // 1분간 50 VUs로 증가
        { duration: '8m', target: 100 },  // 8분간 100 VUs 유지
        { duration: '1m', target: 0 },    // 1분간 0으로 감소
    ],
};
```

### 부하 패턴
```
VUs
100 |          ┌────────────────────┐
 50 |     ┌────┘                    └────┐
  0 |─────┘                              └─────
    0    1min                   9min    10min
```

### 테스트 흐름
1. `/api/simple` - 단순 응답 (simpleLatency 측정)
2. `/api/delay/100` - 100ms 지연 (delayLatency 측정)
3. `/api/cpu/5000` - CPU 5000회 반복 (cpuLatency 측정)
4. `/api/db/users` - DB 조회 (dbReadLatency 측정)

### 커스텀 메트릭
- `simple_latency` - 단순 응답 지연시간
- `delay_latency` - 지연 API 응답시간
- `cpu_latency` - CPU 연산 응답시간
- `db_read_latency` - DB 조회 응답시간
- `errors` - 에러율

### Thresholds (성공 기준)
- simple_latency P95 < 100ms
- delay_latency P95 < 300ms
- cpu_latency P95 < 500ms
- db_read_latency P95 < 200ms

### 사용 시점
- 일반적인 성능 벤치마크
- MVC vs WebFlux 비교 테스트
- 성능 회귀 테스트

```bash
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc load-test.js
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=webflux load-test.js
```

---

## 3. stress-test.js

### 목적
- 시스템의 **한계점**을 찾기 위한 고부하 테스트
- 최대 처리량(throughput) 측정
- 시스템이 어느 지점에서 degradation이 발생하는지 확인

### 설정
```javascript
export const options = {
    stages: [
        { duration: '2m', target: 100 },  // 2분간 100 VUs로 증가
        { duration: '3m', target: 300 },  // 3분간 300 VUs로 증가
        { duration: '3m', target: 300 },  // 3분간 300 VUs 유지
        { duration: '2m', target: 0 },    // 2분간 0으로 감소
    ],
};
```

### 부하 패턴
```
VUs
300 |               ┌────────────────┐
200 |          ┌────┘                └────┐
100 |     ┌────┘                          └────┐
  0 |─────┘                                    └─────
    0   2min    5min              8min       10min
```

### 테스트 흐름
- 4개 엔드포인트 중 **랜덤 선택**하여 요청
    - `/api/simple`
    - `/api/delay/50`
    - `/api/cpu/1000`
    - `/api/db/users`

### Thresholds (성공 기준)
- P95 응답시간 < 2000ms (여유 있게 설정)
- 에러율 < 10%

### 사용 시점
- 시스템 한계 측정
- 용량 계획(Capacity Planning)
- 병목 지점 파악

```bash
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc stress-test.js
```

---

## 4. spike-test.js

### 목적
- **급격한 트래픽 변화**에 대한 시스템 대응 능력 테스트
- 이벤트, 프로모션 등 트래픽 급증 상황 시뮬레이션
- 오토스케일링 없이 단일 인스턴스의 복원력 측정

### 설정
```javascript
export const options = {
    stages: [
        { duration: '30s', target: 10 },   // 워밍업
        { duration: '30s', target: 500 },  // 급증 1
        { duration: '1m', target: 500 },   // 유지
        { duration: '30s', target: 10 },   // 급감
        { duration: '1m', target: 10 },    // 안정화
        { duration: '30s', target: 500 },  // 급증 2
        { duration: '1m', target: 500 },   // 유지
        { duration: '30s', target: 10 },   // 급감
    ],
};
```

### 부하 패턴
```
VUs
500 |     ┌────┐          ┌────┐
    |    ╱    ╲          ╱    ╲
 10 |────┘      └────────┘      └────
    0  30s 1m  2m   3m  4m  5m   6m
```

### 테스트 흐름
- `/api/delay/100` 엔드포인트만 집중 테스트
- I/O 바운드 작업에서의 동시성 처리 능력 측정

### Thresholds (성공 기준)
- 에러율 < 15% (스파이크 상황 감안하여 여유 있게 설정)

### 사용 시점
- 트래픽 급증 대응 능력 측정
- WebFlux의 논블로킹 장점 확인
- 시스템 복원력(Resilience) 테스트

```bash
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc spike-test.js
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=webflux spike-test.js
```

---

## 예상 결과 비교

### I/O 바운드 (delay, external, db)
```
WebFlux >> MVC

- WebFlux: 이벤트 루프로 적은 스레드로 많은 동시 요청 처리
- MVC: 스레드 풀 고갈 시 대기 발생
```

### CPU 바운드 (cpu)
```
WebFlux ≈ MVC

- 둘 다 실제 CPU 작업이 필요하므로 큰 차이 없음
- WebFlux는 boundedElastic 스케줄러 사용
```

### 메모리 사용량
```
WebFlux < MVC

- WebFlux: 스레드 수가 적어 메모리 효율적
- MVC: 스레드당 스택 메모리 사용
```

### Spike 상황
```
WebFlux >> MVC

- WebFlux: 급증해도 스레드 수 일정, 안정적 처리
- MVC: 스레드 풀 고갈로 요청 거부 가능
```

---

## 결과 분석

k6 실행 후 출력되는 주요 메트릭:

```
http_req_duration..............: avg=45.2ms  min=2ms  med=35ms  max=1.2s  p(90)=89ms  p(95)=120ms
http_req_failed................: 0.05%  ✓ 50  ✗ 99950
http_reqs......................: 100000  1666.67/s
iterations.....................: 25000   416.67/s
```

| 메트릭 | 설명 |
|--------|------|
| `http_req_duration` | 요청 응답시간 통계 |
| `http_req_failed` | 실패한 요청 비율 |
| `http_reqs` | 총 요청 수 및 초당 처리량(RPS) |
| `iterations` | 테스트 반복 횟수 |