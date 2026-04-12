import { Directive, Input, TemplateRef, ViewContainerRef, inject, effect, signal } from '@angular/core';
import { PermissionService } from '../../services/permission.service';

@Directive({
  selector: '[hasEnvPermission]',
  standalone: true
})
export class HasEnvPermissionDirective {
  private templateRef = inject(TemplateRef);
  private viewContainer = inject(ViewContainerRef);
  private permissionService = inject(PermissionService);

  private params = signal<[string, 'read' | 'write'] | null>(null);

  @Input() set hasEnvPermission(val: [string, 'read' | 'write']) {
    this.params.set(val);
  }

  constructor() {
    effect(() => {
      const p = this.params();
      if (!p) return;
      
      const canAccess = this.permissionService.hasEnvPermission(p[0], p[1]);
      
      if (canAccess && this.viewContainer.length === 0) {
        this.viewContainer.createEmbeddedView(this.templateRef);
      } else if (!canAccess && this.viewContainer.length > 0) {
        this.viewContainer.clear();
      }
    });
  }
}
