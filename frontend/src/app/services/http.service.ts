import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

import { ToastService } from './toast.service';

@Injectable({
  providedIn: 'root'
})
export class HttpService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(
    private http: HttpClient, 
    private toastService: ToastService
  ) {}

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    const token = localStorage.getItem('vivid_token');
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    
    return headers;
  }

  get<T>(url: string, params?: any): Observable<T> {
    return this.http.get<T>(this.formatUrl(url), {
      headers: this.getHeaders(),
      params: this.formatParams(params, url)
    }).pipe(
      catchError(this.handleError)
    );
  }

  post<T>(url: string, body: any, params?: any): Observable<T> {
    return this.http.post<T>(this.formatUrl(url), body, {
      headers: this.getHeaders(),
      params: this.formatParams(params, url)
    }).pipe(
      catchError(this.handleError)
    );
  }

  put<T>(url: string, body: any, params?: any): Observable<T> {
    return this.http.put<T>(this.formatUrl(url), body, {
      headers: this.getHeaders(),
      params: this.formatParams(params, url)
    }).pipe(
      catchError(this.handleError)
    );
  }

  delete<T>(url: string, params?: any): Observable<T> {
    return this.http.delete<T>(this.formatUrl(url), {
      headers: this.getHeaders(),
      params: this.formatParams(params, url)
    }).pipe(
      catchError(this.handleError)
    );
  }

  private formatUrl(url: string): string {
    if (url.startsWith('http')) {
      return url;
    }
    const cleanUrl = url.startsWith('/') ? url : `/${url}`;
    return `${this.baseUrl}${cleanUrl}`;
  }

  private formatParams(params?: any, url?: string): HttpParams {
    let httpParams = new HttpParams();

    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return httpParams;
  }

  private handleError = (error: HttpErrorResponse) => {
    console.error('[HttpService] Error occurred:', error);
    
    if (error.status === 403) {
      this.toastService.error("You don't have permission to access this feature.");
    } else {
      const message = error.error?.message || error.message || 'An unexpected error occurred';
      this.toastService.error(`Error ${error.status}: ${message}`);
    }
    
    return throwError(() => error);
  };
}
