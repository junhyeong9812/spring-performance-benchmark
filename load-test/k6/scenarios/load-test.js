// load-test/k6/scenarios/load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { getConfig, THRESHOLDS } from '../lib/config.js';

const config = getConfig();

const errorRate = new Rate('errors');
const simpleLatency = new Trend('simple_latency');
const delayLatency = new Trend('delay_latency');
const cpuLatency = new Trend('cpu_latency');
const dbReadLatency = new Trend('db_read_latency');

export const options = {
  stages: [
    { duration: '1m', target: 50 },
    { duration: '8m', target: 100 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    ...THRESHOLDS,
    'simple_latency': ['p(95)<100'],
    'delay_latency': ['p(95)<300'],
    'cpu_latency': ['p(95)<500'],
    'db_read_latency': ['p(95)<200'],
  },
};

export function setup() {
  console.log(`=== Load Test Configuration ===`);
  console.log(`Target Host: ${config.targetHost}`);
  console.log(`App Type: ${config.appType}`);
  console.log(`Base URL: ${config.baseUrl}`);
  console.log(`===============================`);
}

export default function () {
  // Simple endpoint
  let res = http.get(config.endpoints.simple);
  simpleLatency.add(res.timings.duration);
  errorRate.add(res.status !== 200);
  check(res, { 'simple: status 200': (r) => r.status === 200 });
  sleep(0.5);

  // Delay endpoint (100ms)
  res = http.get(config.endpoints.delay(100));
  delayLatency.add(res.timings.duration);
  errorRate.add(res.status !== 200);
  check(res, { 'delay: status 200': (r) => r.status === 200 });
  sleep(0.5);

  // CPU endpoint (5000 iterations)
  res = http.get(config.endpoints.cpu(5000));
  cpuLatency.add(res.timings.duration);
  errorRate.add(res.status !== 200);
  check(res, { 'cpu: status 200': (r) => r.status === 200 });
  sleep(0.5);

  // DB read
  res = http.get(config.endpoints.dbRead);
  dbReadLatency.add(res.timings.duration);
  errorRate.add(res.status !== 200);
  check(res, { 'db read: status 200': (r) => r.status === 200 });
  sleep(0.5);
}