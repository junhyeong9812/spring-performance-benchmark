// load-test/k6/scenarios/spike-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { getConfig } from '../lib/config.js';

const config = getConfig();
const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '30s', target: 500 },
    { duration: '1m', target: 500 },
    { duration: '30s', target: 10 },
    { duration: '1m', target: 10 },
    { duration: '30s', target: 500 },
    { duration: '1m', target: 500 },
    { duration: '30s', target: 10 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.15'],
    errors: ['rate<0.15'],
  },
};

export function setup() {
  console.log(`=== Spike Test: ${config.appType} ===`);
  console.log(`Base URL: ${config.baseUrl}`);
}

export default function () {
  const res = http.get(config.endpoints.delay(100));

  errorRate.add(res.status !== 200);
  check(res, { 'status 200': (r) => r.status === 200 });

  sleep(0.1);
}
