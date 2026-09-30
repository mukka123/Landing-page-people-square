import { HttpInterceptorFn } from "@angular/common/http";

/** Every request carries a client-generated request id, echoed back in Problem Details errors. */
export const requestIdInterceptor: HttpInterceptorFn = (req, next) => {
  const requestId = crypto.randomUUID();
  return next(req.clone({ setHeaders: { "X-Request-Id": requestId } }));
};
