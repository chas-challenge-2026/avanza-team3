import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8083';

export const options = {
  vus: 1,
  duration: '15s',
  thresholds: {
    'http_req_duration{endpoint:holdings}': ['p(95)<200'],
    'http_req_duration{endpoint:portfolio}': ['p(95)<200'],
    'http_req_failed{endpoint:holdings}': ['rate<0.01'],
    'http_req_failed{endpoint:portfolio}': ['rate<0.01'],
  },
};

export function setup() {
  const response = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({
      email: 'anna@example.com',
      password: 'password123',
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (response.status !== 200 || !response.json('token')) {
    throw new Error(`Login failed: ${response.status}`);
  }

  return { token: response.json('token') };
}

export default function (data) {
  const headers = {
    Authorization: `Bearer ${data.token}`,
  };

  const holdings = http.get(
    `${BASE_URL}/api/holdings?page=0&size=20`,
    { headers, tags: { endpoint: 'holdings' } }
  );

  check(holdings, {
    'Holdings returns 200': (r) => r.status === 200,
  });

  const portfolio = http.get(
    `${BASE_URL}/api/portfolio?page=0&size=100`,
    { headers, tags: { endpoint: 'portfolio' } }
  );

  check(portfolio, {
    'Portfolio returns 200': (r) => r.status === 200,
  });

  sleep(1);
}



