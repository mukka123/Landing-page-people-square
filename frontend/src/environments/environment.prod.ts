export const environment = {
  production: true,
  apiBaseUrl: "/api/v1",
  oidc: {
    issuer: "https://idp.production.internal/realms/political-ops",
    clientId: "political-ops-web",
    redirectUri: window.location.origin + "/callback"
  }
};
