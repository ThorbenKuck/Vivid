import { Directive, Input, TemplateRef, ViewContainerRef, inject, effect, signal } from '@angular/core';
import { PermissionService } from '../../services/permission.service';

@Directive({
  selector: '[hasPermission]',
  standalone: true
})
export class HasPermissionDirective {
  private templateRef = inject(TemplateRef);
  private viewContainer = inject(ViewContainerRef);
  private permissionService = inject(PermissionService);

  private params = signal<[string, 'read' | 'write'] | null>(null);

  @Input() set hasPermission(val: [string, 'read' | 'write']) {
    this.params.set(val);
  }

  constructor() {
    effect(() => {
      const p = this.params();
      if (!p) return;
      
      const canAccess = this.permissionService.hasPermission(p[0], p[1]);
      
      if (canAccess && this.viewContainer.length === 0) {
        this.viewContainer.createEmbeddedView(this.templateRef);
      } else if (!canAccess && this.viewContainer.length > 0) {
        this.viewContainer.clear();
      }
    });
  }
}
