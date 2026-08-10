export const environment = {
  production: false,
  /**
   * Overridden at startup from assets/config.json (see main.ts + api-url.ts).
   * Do not hard-code a port here for deploys — leave resolution to runtime config.
   */
  apiUrl: 'http://localhost:8081/fast-food-order-api/api'
};
