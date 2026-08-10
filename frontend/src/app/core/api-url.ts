/**
 * Runtime / deploy-friendly API base URL resolution.
 *
 * Priority:
 * 1. localStorage.apiBaseUrl (full URL, e.g. http://localhost:8081/fast-food-order-api/api)
 * 2. assets/config.json → apiBaseUrl (if non-empty)
 * 3. Auto:
 *    - localhost / 127.0.0.1 → http(s)://host:{apiPort}/fast-food-order-api/api
 *      (port from localStorage.apiPort or config.apiPort, default 8081)
 *    - otherwise (deployed) → {origin}{contextPath}/api  (same host, no hard-coded port)
 */

export interface RuntimeApiConfig {
  apiBaseUrl?: string;
  apiPort?: string | number;
  apiContextPath?: string;
}

const DEFAULT_PORT = '8081';
const DEFAULT_CONTEXT = '/fast-food-order-api';

export function resolveApiUrl(config: RuntimeApiConfig = {}): string {
  const fromStorage = (typeof localStorage !== 'undefined' && localStorage.getItem('apiBaseUrl')) || '';
  if (fromStorage.trim()) {
    return normalizeApiUrl(fromStorage.trim());
  }

  if (config.apiBaseUrl && config.apiBaseUrl.trim()) {
    return normalizeApiUrl(config.apiBaseUrl.trim());
  }

  const context = (config.apiContextPath || DEFAULT_CONTEXT).replace(/\/$/, '');
  const host = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
  const protocol = typeof window !== 'undefined' ? window.location.protocol : 'http:';
  const isLocal = host === 'localhost' || host === '127.0.0.1';

  if (isLocal) {
    const port =
      (typeof localStorage !== 'undefined' && localStorage.getItem('apiPort')) ||
      String(config.apiPort || DEFAULT_PORT);
    return `${protocol}//${host}:${port}${context}/api`;
  }

  // Deployed: same origin (nginx / reverse proxy) — port follows the page URL
  const origin = typeof window !== 'undefined' ? window.location.origin : '';
  return `${origin}${context}/api`;
}

function normalizeApiUrl(url: string): string {
  return url.replace(/\/$/, '');
}
