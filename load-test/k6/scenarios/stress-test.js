// load-test/k6/scenarios/stress-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { getConfig } from '../lib/config.js';

const config = getConfig();
const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '3m', target: 300 },
    { duration: '3m', target: 300 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.1'],
    errors: ['rate<0.1'],
  },
};

export function setup() {
  console.log(`=== Stress Test: ${config.appType} ===`);
  console.log(`Base URL: ${config.baseUrl}`);
}

export default function () {
  const scenarios = [
    () => http.get(config.endpoints.simple),
    () => http.get(config.endpoints.delay(50)),
    () => http.get(config.endpoints.cpu(1000)),
    () => http.get(config.endpoints.dbRead),
  ];

  const scenario = scenarios[Math.floor(Math.random() * scenarios.length)];
  const res = scenario();

  errorRate.add(res.status !== 200);
  check(res, { 'status 200': (r) => r.status === 200 });

  sleep(0.1);
}