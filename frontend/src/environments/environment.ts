export const environment = {
  production: false,
  apiBaseUrl: "http://localhost:8080/api/v1",
  oidc: {
    issuer: "https://idp.example.internal/realms/political-ops",
    clientId: "political-ops-web",
    redirectUri: window.location.origin + "/callback"
  }
};
