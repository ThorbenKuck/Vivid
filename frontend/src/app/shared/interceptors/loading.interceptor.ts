import {inject, Injectable, InjectionToken} from '@angular/core';
import {HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpInterceptorFn} from '@angular/common/http';
import { Observable, finalize } from 'rxjs';
import {LoadingService} from "../../services/loading.service";

// @Injectable()
// export class LoadingInterceptor implements HttpInterceptor {
//   private activeRequests = 0;
//
//   constructor(private loadingService: LoadingService) {}
//
//   intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
//     this.activeRequests++;
//     this.loadingService.setLoading(true);
//
//     return next.handle(request).pipe(
//       finalize(() => {
//         this.activeRequests--;
//         if (this.activeRequests === 0) {
//           this.loadingService.setLoading(false);
//         }
//       })
//     );
//   }
// }

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
    const loadingService = inject(LoadingService);
    const activeRequests = inject(ACTIVE_REQUEST__COUNT, { optional: true }) || { count: 0 };

    activeRequests.count++;
    loadingService.setFetching(true);

    return next(req).pipe(
        finalize(() => {
            activeRequests.count--;
            if (activeRequests.count === 0) {
                loadingService.setFetching(false);
            }
        })
    );
};

// Kleiner Helper für den Count (als Injection Token)
export const ACTIVE_REQUEST__COUNT = new InjectionToken<{count: number}>('ActiveRequestCount', {
    providedIn: 'root',
    factory: () => ({ count: 0 })
});