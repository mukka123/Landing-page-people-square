import { ApplicationConfig, provideZoneChangeDetection } from "@angular/core";
import { provideRouter } from "@angular/router";
import { provideHttpClient, withInterceptors } from "@angular/common/http";
import { routes } from "./app.routes";
import { authTokenInterceptor } from "./core/auth-token.interceptor";
import { requestIdInterceptor } from "./core/request-id.interceptor";

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // Order matters: stamp the request id first, then attach the bearer token.
    provideHttpClient(withInterceptors([requestIdInterceptor, authTokenInterceptor]))
  ]
};
