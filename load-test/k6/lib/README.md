# k6 Library

k6 테스트 시나리오에서 공통으로 사용하는 설정 및 유틸리티 모듈입니다.

## 파일 목록

### config.js

모든 테스트 시나리오에서 공통으로 사용하는 설정 파일입니다.

---

## config.js 상세 설명

### 1. getConfig() 함수

테스트 실행 시 환경 변수를 기반으로 설정 객체를 생성합니다.

```javascript
import { getConfig } from '../lib/config.js';

const config = getConfig();
console.log(config.baseUrl);  // http://192.168.1.100:8080
```

#### 환경 변수

| 변수 | 설명 | 기본값 | 예시 |
|------|------|--------|------|
| `TARGET_HOST` | 서버 IP 또는 호스트명 | `localhost` | `192.168.1.100` |
| `APP_TYPE` | 테스트 대상 앱 | `mvc` | `mvc` 또는 `webflux` |
| `USE_PROXY` | Nginx 프록시 사용 여부 | `false` | `true` |

#### 반환 객체

```javascript
{
    targetHost: '192.168.1.100',
    appType: 'mvc',
    baseUrl: 'http://192.168.1.100:8080',
    endpoints: {
        info: 'http://192.168.1.100:8080/api/info',
        simple: 'http://192.168.1.100:8080/api/simple',
        delay: (ms) => `http://192.168.1.100:8080/api/delay/${ms}`,
        cpu: (iterations) => `http://192.168.1.100:8080/api/cpu/${iterations}`,
        dbRead: 'http://192.168.1.100:8080/api/db/users',
        dbWrite: 'http://192.168.1.100:8080/api/db/users',
        external: 'http://192.168.1.100:8080/api/external',
        stream: (count) => `http://192.168.1.100:8080/api/stream/${count}`,
    }
}
```

#### URL 생성 로직

```javascript
// USE_PROXY=false (기본값)
// APP_TYPE=mvc     → http://{host}:8080
// APP_TYPE=webflux → http://{host}:8081

// USE_PROXY=true
// APP_TYPE=mvc     → http://{host}/mvc
// APP_TYPE=webflux → http://{host}/webflux
```

---

### 2. THRESHOLDS 상수

k6 테스트의 성공/실패 기준을 정의합니다.

```javascript
import { THRESHOLDS } from '../lib/config.js';

export const options = {
    thresholds: THRESHOLDS,
};
```

#### 기본 Thresholds

```javascript
export const THRESHOLDS = {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.01'],
};
```

| 메트릭 | 조건 | 설명 |
|--------|------|------|
| `http_req_duration` | `p(95)<500` | 95% 요청이 500ms 이내 |
| `http_req_duration` | `p(99)<1000` | 99% 요청이 1000ms 이내 |
| `http_req_failed` | `rate<0.01` | 실패율 1% 미만 |

#### 커스텀 Thresholds 확장

```javascript
export const options = {
    thresholds: {
        ...THRESHOLDS,
        'custom_metric': ['avg<100'],
    },
};
```

---

## 사용 예시

### 기본 사용

```javascript
// scenarios/my-test.js
import http from 'k6/http';
import { check } from 'k6';
import { getConfig, THRESHOLDS } from '../lib/config.js';

const config = getConfig();

export const options = {
    vus: 10,
    duration: '1m',
    thresholds: THRESHOLDS,
};

export default function () {
    // 단순 엔드포인트 호출
    let res = http.get(config.endpoints.simple);
    check(res, { 'status 200': (r) => r.status === 200 });

    // 파라미터가 있는 엔드포인트
    res = http.get(config.endpoints.delay(100));
    check(res, { 'status 200': (r) => r.status === 200 });

    res = http.get(config.endpoints.cpu(5000));
    check(res, { 'status 200': (r) => r.status === 200 });
}
```

### 실행 명령어

```bash
# MVC 직접 접근 (포트 8080)
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc my-test.js

# WebFlux 직접 접근 (포트 8081)
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=webflux my-test.js

# Nginx 프록시 경유 (포트 80)
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc --env USE_PROXY=true my-test.js
```

---

## 엔드포인트 설명

| 엔드포인트 | 메서드 | 설명 | 테스트 목적 |
|-----------|--------|------|------------|
| `/api/info` | GET | 서버 정보 반환 | 헬스체크 |
| `/api/simple` | GET | 단순 JSON 응답 | 기본 처리량 |
| `/api/delay/{ms}` | GET | 지정 시간 대기 후 응답 | I/O 바운드 성능 |
| `/api/cpu/{iterations}` | GET | CPU 집약적 연산 | CPU 바운드 성능 |
| `/api/db/users` | GET | DB에서 사용자 목록 조회 | DB 읽기 성능 |
| `/api/db/users` | POST | DB에 사용자 생성 | DB 쓰기 성능 |
| `/api/external` | GET | 외부 API 호출 | 네트워크 I/O |
| `/api/stream/{count}` | GET | 대용량 데이터 스트리밍 | 스트리밍 성능 |

---

## 새 시나리오 추가하기

1. `scenarios/` 폴더에 새 파일 생성

```javascript
// scenarios/custom-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { getConfig, THRESHOLDS } from '../lib/config.js';

const config = getConfig();

export const options = {
    vus: 20,
    duration: '5m',
    thresholds: {
        ...THRESHOLDS,
        http_req_duration: ['p(95)<300'],  // 더 엄격한 기준
    },
};

export default function () {
    // 커스텀 테스트 로직
    const res = http.get(config.endpoints.simple);
    check(res, { 'status 200': (r) => r.status === 200 });
    sleep(0.5);
}
```

2. 실행

```bash
k6 run --env TARGET_HOST=192.168.1.100 --env APP_TYPE=mvc scenarios/custom-test.js
```