import { Directive, Input, TemplateRef, ViewContainerRef, inject, effect, signal } from '@angular/core';
import { PermissionService } from '../../services/permission.service';

@Directive({
  selector: '[vividPerms]',
  standalone: true
})
export class VividPermsDirective {
  private templateRef = inject(TemplateRef);
  private viewContainer = inject(ViewContainerRef);
  private permissionService = inject(PermissionService);

  private params = signal<{resource: string, action: 'read' | 'write'} | null>(null);

  @Input() set vividPerms(val: {resource: string, action: 'read' | 'write'}) {
    this.params.set(val);
  }

  constructor() {
    effect(() => {
      const p = this.params();
      if (!p) return;
      
      const canAccess = this.permissionService.hasPermission(p.resource, p.action);
      
      if (canAccess && this.viewContainer.length === 0) {
        this.viewContainer.createEmbeddedView(this.templateRef);
      } else if (!canAccess && this.viewContainer.length > 0) {
        this.viewContainer.clear();
      }
    });
  }
}
