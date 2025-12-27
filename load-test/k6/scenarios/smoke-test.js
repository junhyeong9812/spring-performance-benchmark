// load-test/k6/scenarios/smoke-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { getConfig, THRESHOLDS } from '../lib/config.js';

const config = getConfig();

export const options = {
  vus: 5,
  duration: '1m',
  thresholds: THRESHOLDS,
};

export default function () {
  console.log(`Testing: ${config.baseUrl}`);

  // Simple endpoint
  let res = http.get(config.endpoints.simple);
  check(res, {
    'simple: status 200': (r) => r.status === 200,
    'simple: response time < 200ms': (r) => r.timings.duration < 200,
  });
  sleep(1);

  // Delay endpoint (100ms)
  res = http.get(config.endpoints.delay(100));
  check(res, {
    'delay: status 200': (r) => r.status === 200,
  });
  sleep(1);

  // CPU endpoint (1000 iterations)
  res = http.get(config.endpoints.cpu(1000));
  check(res, {
    'cpu: status 200': (r) => r.status === 200,
  });
  sleep(1);

  // DB read
  res = http.get(config.endpoints.dbRead);
  check(res, {
    'db read: status 200': (r) => r.status === 200,
  });
  sleep(1);
}