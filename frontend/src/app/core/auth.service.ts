import { Injectable, signal } from "@angular/core";
import { environment } from "../../environments/environment";

/**
 * Minimal OIDC auth-code-with-PKCE placeholder. Swap for a real client library
 * (angular-oauth2-oidc or oidc-client-ts) before Phase One sign-off — this exists so
 * the interceptor and route guards have a real contract to code against.
 */
@Injectable({ providedIn: "root" })
export class AuthService {
  private readonly token = signal<string | null>(sessionStorage.getItem("access_token"));

  accessToken(): string | null {
    return this.token();
  }

  isAuthenticated(): boolean {
    return this.token() !== null;
  }

  login(): void {
    const url = new URL(environment.oidc.issuer + "/protocol/openid-connect/auth");
    url.searchParams.set("client_id", environment.oidc.clientId);
    url.searchParams.set("redirect_uri", environment.oidc.redirectUri);
    url.searchParams.set("response_type", "code");
    url.searchParams.set("scope", "openid profile email");
    window.location.assign(url.toString());
  }

  logout(): void {
    sessionStorage.removeItem("access_token");
    this.token.set(null);
  }
}
