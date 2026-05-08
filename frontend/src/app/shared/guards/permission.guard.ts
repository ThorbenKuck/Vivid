import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {PermissionService} from '../../services/permission.service';
import {map} from 'rxjs/operators';

export const permissionGuard: CanActivateFn = (route) => {
  const permissionService = inject(PermissionService);
  const router = inject(Router);
  const resource = route.data['resource'] as string;
  const action = (route.data['action'] as 'read' | 'write') || 'read';

  return permissionService.fetchPermissions().pipe(
    map(() => {
      if (permissionService.hasPermission(resource, action)) {
        return true;
      }
      return router.createUrlTree(['/403']);
    })
  );
};
