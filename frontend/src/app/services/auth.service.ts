import {Injectable} from '@angular/core';
import {BehaviorSubject, delay, EMPTY, Observable, of, take, throwError} from 'rxjs';
import {UserDto} from '../dtos';
import {catchError, filter, map, switchMap, tap} from 'rxjs/operators';
import {HttpService} from './http.service';
import {Router} from '@angular/router';
import {PermissionService} from './permission.service';
import {HttpEvent, HttpHandlerFn, HttpRequest} from "@angular/common/http";
import {AuthConfigDto} from "../dtos/AuthConfigDto";

export interface AuthConfig {
    issuer?: string;
    issuerName?: string;
    logoutUrl?: string;
    clientId: string;
    scope: string;
    redirectUri: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private userSubject = new BehaviorSubject<UserDto | null>(null);
    user$ = this.userSubject.asObservable();
    private isRefreshing = false;
    private refreshTokenSubject = new BehaviorSubject<string | null>(null);

    private config: AuthConfig = {
        clientId: 'vivid-client',
        scope: 'openid profile email',
        redirectUri: window.location.origin + '/login'
    };

    constructor(
        private http: HttpService,
        private router: Router,
        private permissionService: PermissionService
    ) {
        this.restoreSession();
    }

    private restoreSession() {
        const savedUser = localStorage.getItem('vivid_user');
        if (savedUser) {
            this.userSubject.next(JSON.parse(savedUser));
        }
    }

    isAuthenticated(): Observable<boolean> {
        return of(!!this.userSubject.value);
    }

    getAuthConfig(): Observable<AuthConfigDto> {
        return this.http.get<AuthConfigDto>('/api/auth/config').pipe(
            tap(config => {
                this.config.issuer = config.issuer;
                this.config.logoutUrl = config.logoutUrl;
                this.config.issuerName = config.issuerName;
            })
        );
    }

    initiateLogin(issuer: string) {
        const state = this.generateRandomString(32);
        const codeVerifier = this.generateRandomString(64);

        localStorage.setItem('vivid_auth_state', state);
        localStorage.setItem('vivid_auth_code_verifier', codeVerifier);

        this.generateCodeChallenge(codeVerifier).then(codeChallenge => {
            const authUrl = new URL(`${issuer}/protocol/openid-connect/auth`);
            authUrl.searchParams.set('client_id', this.config.clientId);
            authUrl.searchParams.set('redirect_uri', this.config.redirectUri);
            authUrl.searchParams.set('response_type', 'code');
            authUrl.searchParams.set('scope', this.config.scope);
            authUrl.searchParams.set('state', state);
            authUrl.searchParams.set('code_challenge', codeChallenge);
            authUrl.searchParams.set('code_challenge_method', 'S256');

            window.location.href = authUrl.toString();
        });
    }

    handleCallback(code: string, state: string, issuer: string): Observable<UserDto> {
        const savedState = localStorage.getItem('vivid_auth_state');
        const codeVerifier = localStorage.getItem('vivid_auth_code_verifier');

        if (state !== savedState) {
            throw new Error('Invalid state');
        }

        const tokenUrl = `${issuer}/protocol/openid-connect/token`;
        const body = new URLSearchParams();
        body.set('grant_type', 'authorization_code');
        body.set('client_id', this.config.clientId);
        body.set('code', code);
        body.set('redirect_uri', this.config.redirectUri);
        body.set('code_verifier', codeVerifier || '');

        // Note: We're using standard fetch here because HttpService might not be ready for token exchange
        // without a token, or we might want to avoid prefixing if tokenUrl is absolute.
        return new Observable<UserDto>(subscriber => {
            fetch(tokenUrl, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: body.toString()
            })
                .then(res => res.json())
                .then(data => {
                    const jwt = data.access_token;
                    const refreshToken = data.refresh_token;
                    localStorage.setItem('vivid_token', jwt);
                    if (refreshToken) {
                        localStorage.setItem('vivid_refresh_token', refreshToken);
                    }
                    this.login(jwt).subscribe({
                        next: user => {
                            subscriber.next(user);
                            subscriber.complete();
                        },
                        error: err => subscriber.error(err)
                    });
                })
                .catch(err => subscriber.error(err));
        });
    }

    login(jwt: string): Observable<UserDto> {
        const decoded = this.parseJwt(jwt);
        console.log(decoded);

        return this.http.post<UserDto>('/api/web/users/sync', {}).pipe(
            switchMap(user => {
                this.userSubject.next(user);
                localStorage.setItem('vivid_user', JSON.stringify(user));
                return this.permissionService.refreshPermissions().pipe(
                    map(() => user),
                    catchError(() => of(user))
                );
            })
        );
    }

    refreshToken(): Observable<string> {
        const refreshToken = localStorage.getItem('vivid_refresh_token');
        const issuer = this.config.issuer;

        // SOFORTIGER ABBRUCH statt throwError, wenn nichts da ist
        if (!refreshToken || !issuer) {
            this.logout(); // Direkt ausloggen ohne Umwege
            return EMPTY;  // Import von 'rxjs'
        }

        const tokenUrl = `${issuer}/protocol/openid-connect/token`;
        const body = new URLSearchParams();
        body.set('grant_type', 'refresh_token');
        body.set('client_id', this.config.clientId);
        body.set('refresh_token', refreshToken);

        return new Observable<string>(subscriber => {
            fetch(tokenUrl, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: body.toString()
            })
                .then(res => {
                    if (!res.ok) throw new Error('Refresh failed');
                    return res.json();
                })
                .then(data => {
                    localStorage.setItem('vivid_token', data.access_token);
                    localStorage.setItem('vivid_refresh_token', data.refresh_token);
                    subscriber.next(data.access_token);
                    subscriber.complete();
                })
                .catch(err => {
                    this.logout(); // Wenn Refresh fehlschlägt, ist die Session wirklich tot
                    subscriber.error(err);
                });
        });
    }

    logout() {
        this.userSubject.next(null);
        localStorage.removeItem('vivid_token');
        localStorage.removeItem('vivid_refresh_token'); // Wichtig: Auch das Refresh-Token löschen!
        localStorage.removeItem('vivid_user');
        this.permissionService.clearPermissions();

        if (this.config.logoutUrl) {
            window.location.href = this.config.logoutUrl;
        } else {
            this.router.navigate(['/login']);
        }
    }

    private parseJwt(token: string): any {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            console.error('Failed to parse JWT', e);
            return {};
        }
    }

    private generateRandomString(length: number): string {
        const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
        let result = '';
        const values = new Uint32Array(length);
        crypto.getRandomValues(values);
        for (let i = 0; i < length; i++) {
            result += charset[values[i] % charset.length];
        }
        return result;
    }

    private async generateCodeChallenge(verifier: string): Promise<string> {
        const encoder = new TextEncoder();
        const data = encoder.encode(verifier);
        const digest = await crypto.subtle.digest('SHA-256', data);
        return btoa(String.fromCharCode(...new Uint8Array(digest)))
            .replace(/\+/g, '-')
            .replace(/\//g, '_')
            .replace(/=+$/, '');
    }

    handle401(req: HttpRequest<any>, next: HttpHandlerFn): Observable<HttpEvent<any>> {
        if (!this.isRefreshing) {
            this.isRefreshing = true;
            this.refreshTokenSubject.next(null);

            return this.refreshToken().pipe(
                delay(1000),
                switchMap((newToken) => {
                    this.isRefreshing = false;
                    localStorage.setItem('vivid_token', newToken);
                    this.refreshTokenSubject.next(newToken);
                    return next(this.addToken(req, newToken));
                }),
                catchError((err) => {
                    this.isRefreshing = false;
                    this.logout();
                    return throwError(() => err);
                })
            );
        }

        return this.refreshTokenSubject.pipe(
            filter(token => token !== null),
            take(1),
            switchMap((token) => next(this.addToken(req, token!)))
        );
    }

    private addToken(req: HttpRequest<any>, token: string) {
        return req.clone({setHeaders: {Authorization: `Bearer ${token}`}});
    }
}
