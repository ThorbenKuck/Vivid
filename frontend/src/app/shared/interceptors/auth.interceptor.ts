import {inject, Injectable, signal} from '@angular/core';
import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpInterceptorFn,
    HttpRequest
} from '@angular/common/http';
import {BehaviorSubject, finalize, Observable, switchMap, take, throwError} from 'rxjs';
import {catchError, filter} from 'rxjs/operators';
import {AuthService} from '../../services/auth.service';
import {LoadingService} from "../../services/loading.service";
import {ACTIVE_REQUEST__COUNT} from "./loading.interceptor";

// @Injectable()
// export class AuthInterceptor implements HttpInterceptor {
//     private isRefreshing = false;
//     // Das Subject fungiert als "Signal" für wartende Requests
//     private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);
//     private authService = inject(AuthService);
//
//     intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
//         const token = localStorage.getItem('vivid_token');
//
//         if (token) {
//             request = this.addToken(request, token);
//         }
//
//         return next.handle(request).pipe(
//             catchError((error) => {
//                 if (error instanceof HttpErrorResponse && error.status === 401) {
//                     return this.handle401Error(request, next);
//                 }
//                 return throwError(() => error);
//             })
//         );
//     }
//
//     private handle401Error(request: HttpRequest<any>, next: HttpHandler) {
//         if (!this.isRefreshing) {
//             this.isRefreshing = true;
//             this.refreshTokenSubject.next(null);
//
//             return this.authService.refreshToken().pipe(
//                 switchMap((newToken) => {
//                     this.isRefreshing = false;
//                     this.refreshTokenSubject.next(newToken); // Signal an alle wartenden Requests
//                     return next.handle(this.addToken(request, newToken));
//                 }),
//                 catchError((err) => {
//                     this.isRefreshing = false;
//                     this.authService.logout();
//                     return throwError(() => err);
//                 })
//             );
//         } else {
//             // Wenn bereits ein Refresh läuft, warten wir auf das Signal
//             return this.refreshTokenSubject.pipe(
//                 filter(token => token !== null), // Warte, bis das Token da ist
//                 take(1),                         // Nimm nur den ersten Wert
//                 switchMap((token) => next.handle(this.addToken(request, token!)))
//             );
//         }
//     }
//
//     private addToken(request: HttpRequest<any>, token: string) {
//         return request.clone({
//             setHeaders: { Authorization: `Bearer ${token}` }
//         });
//     }
// }

// auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const isRefreshing = signal(false); // Lokaler Status innerhalb des Interceptors klappt hier nicht gut, besser im Service

    // 1. Wenn es der Refresh-Request selbst ist -> einfach durchwinken
    if (req.url.includes('/api/auth/config') || req.url.includes('/protocol/openid-connect/')) {
        return next(req);
    }

    const token = localStorage.getItem('vivid_token');
    let authReq = req;

    if (token) {
        authReq = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
    }

    return next(authReq).pipe(
        catchError((error) => {
            if (error instanceof HttpErrorResponse && error.status === 401) {
                // Hier rufen wir die Refresh-Logik im Service auf
                return authService.handle401(authReq, next);
            }
            return throwError(() => error);
        })
    );
};