// load-test/k6/lib/config.js

export function getConfig() {
  const targetHost = __ENV.TARGET_HOST || 'localhost';
  const appType = __ENV.APP_TYPE || 'mvc';  // 'mvc' or 'webflux'

  // Nginx 리버스 프록시를 통한 접근 (전체 스택)
  // 또는 직접 접근 (개별 테스트)
  const useProxy = __ENV.USE_PROXY === 'true';

  let baseUrl;
  if (useProxy) {
    // Nginx 프록시 경유
    baseUrl = `http://${targetHost}/${appType}`;
  } else {
    // 직접 접근
    const port = appType === 'mvc' ? 8080 : 8081;
    baseUrl = `http://${targetHost}:${port}`;
  }

  return {
    targetHost,
    appType,
    baseUrl,
    endpoints: {
      info: `${baseUrl}/api/info`,
      simple: `${baseUrl}/api/simple`,
      delay: (ms) => `${baseUrl}/api/delay/${ms}`,
      cpu: (iterations) => `${baseUrl}/api/cpu/${iterations}`,
      dbRead: `${baseUrl}/api/db/users`,
      dbWrite: `${baseUrl}/api/db/users`,
      external: `${baseUrl}/api/external`,
      stream: (count) => `${baseUrl}/api/stream/${count}`,
    }
  };
}

export const THRESHOLDS = {
  http_req_duration: ['p(95)<500', 'p(99)<1000'],
  http_req_failed: ['rate<0.01'],
};