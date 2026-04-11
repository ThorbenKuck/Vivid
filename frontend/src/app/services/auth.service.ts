import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { UserDto, UserSyncRequest } from '../dtos';
import { tap } from 'rxjs/operators';
import { HttpService } from './http.service';
import { Router } from '@angular/router';

export interface AuthConfig {
  issuer?: string;
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

  private config: AuthConfig = {
    clientId: 'vivid-client',
    scope: 'openid profile email',
    redirectUri: window.location.origin + '/login'
  };

  constructor(
    private http: HttpService,
    private router: Router
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

  getAuthConfig(): Observable<{ issuer?: string, logoutUrl?: string }> {
    return this.http.get<{ issuer?: string, logoutUrl?: string }>('/api/auth/config').pipe(
      tap(config => {
        this.config.issuer = config.issuer;
        this.config.logoutUrl = config.logoutUrl;
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
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString()
      })
      .then(res => res.json())
      .then(data => {
        const jwt = data.access_token;
        localStorage.setItem('vivid_token', jwt);
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
    const syncRequest: UserSyncRequest = {
      keycloakId: decoded.sub,
      username: decoded.preferred_username || decoded.sub,
      email: decoded.email,
      displayRole: decoded.vivid_role
    };

    return this.http.post<UserDto>('/api/web/users/sync', syncRequest).pipe(
      tap(user => {
        this.userSubject.next(user);
        localStorage.setItem('vivid_user', JSON.stringify(user));
      })
    );
  }

  logout() {
    this.userSubject.next(null);
    localStorage.removeItem('vivid_token');
    localStorage.removeItem('vivid_user');

    if (this.config.logoutUrl) {
      window.location.href = this.config.logoutUrl;
    } else {
      // Fallback if no logout URL is configured
      this.getAuthConfig().subscribe(config => {
        if (config.logoutUrl) {
          window.location.href = config.logoutUrl;
        } else {
          this.router.navigate(['/']);
        }
      });
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
}
