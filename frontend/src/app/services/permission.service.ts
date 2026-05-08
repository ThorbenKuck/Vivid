import {inject, Injectable, signal} from '@angular/core';
import { HttpService } from './http.service';
import { PermissionSetDto } from '../dtos/PermissionSetDto';
import {Observable, of, tap, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {LoadingService} from "./loading.service";

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private permissions = signal<PermissionSetDto | null>(null);
  private loading = false;
  private http = inject(HttpService);
  private loadingService = inject(LoadingService);

  fetchPermissions(force = false, informLoadingService: boolean = true): Observable<PermissionSetDto> {
    if (this.loading && !force) {
      return of(this.permissions() || {
        admin: false,
        environments: 'none',
        environment: { admin: false, all: 'none', specific: {} },
        clients: 'none',
        settings: 'none',
        features: 'none',
        resolved: false
      });
    }

    if (informLoadingService) {
      this.loadingService.setApplicationLoading(true);
    }

    this.loading = true;
    return this.http.get<PermissionSetDto>('/api/auth/permissions').pipe(
      tap(perms => {
        this.permissions.set(perms);
        this.loading = false;
        if (informLoadingService) {
          this.loadingService.setApplicationLoading(false);
        }
      }),
      catchError(err => {
        this.loading = false;
        if (informLoadingService) {
          this.loadingService.setApplicationLoading(false);
        }
        return throwError(() => err);
      })
    );
  }

  refreshPermissions(): Observable<PermissionSetDto> {
    return this.fetchPermissions(true);
  }

  clearPermissions() {
    this.permissions.set(null);
  }

  hasPermission(resource: string, action: 'read' | 'write'): boolean {
    const perms = this.permissions();
    if (!perms) return false;
    if (perms.admin) return true;

    let level: 'none' | 'read' | 'write' = 'none';
    switch (resource) {
      case 'environments': level = perms.environments; break;
      case 'clients': level = perms.clients; break;
      case 'settings': level = perms.settings; break;
      case 'features': level = perms.features; break;
    }

    return this.checkAccess(level, action);
  }

  hasEnvPermission(envName: string, action: 'read' | 'write'): boolean {
    const perms = this.permissions();
    if (!perms) return false;
    if (perms.admin || perms.environment.admin) return true;

    if (this.checkAccess(perms.environment.all, action)) return true;

    const specificLevel = perms.environment.specific[envName] || 'none';
    return this.checkAccess(specificLevel, action);
  }

  private checkAccess(level: 'none' | 'read' | 'write', action: 'read' | 'write'): boolean {
    if (action === 'write') return level === 'write';
    if (action === 'read') return level === 'read' || level === 'write';
    return false;
  }
}
