import { Injectable, signal } from '@angular/core';
import { HttpService } from './http.service';
import { PermissionSetDto } from '../dtos/PermissionSetDto';
import {Observable, of, tap, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private permissions = signal<PermissionSetDto | null>(null);
  private loading = false;

  constructor(private http: HttpService) {}

  fetchPermissions(force = false): Observable<PermissionSetDto> {
    if (this.loading && !force) {
      return of(this.permissions() || { admin: false, environments: 'none', teams: 'none', departments: 'none', environment: { admin: false, all: 'none', specific: {} }, resolved: false });
    }

    this.loading = true;
    return this.http.get<PermissionSetDto>('/api/auth/permissions').pipe(
      tap(perms => {
        this.permissions.set(perms);
        this.loading = false;
      }),
      catchError(err => {
        this.loading = false;
        return throwError(() => err);
      })
    );
  }

  refreshPermissions(): Observable<PermissionSetDto> {
    return this.fetchPermissions(true);
  }

  hasPermission(resource: string, action: 'read' | 'write'): boolean {
    const perms = this.permissions();
    if (!perms) return false;
    if (perms.admin) return true;

    let level: 'none' | 'read' | 'write' = 'none';
    switch (resource) {
      case 'environments': level = perms.environments; break;
      case 'teams': level = perms.teams; break;
      case 'departments': level = perms.departments; break;
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
