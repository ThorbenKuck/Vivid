import { Component, TemplateRef, viewChild } from '@angular/core';

@Component({
    selector: 'app-tab-header', // Eigener Selektor!
    standalone: true,
    template: `
    <ng-template #tabHeaderTemplate>
      <ng-content></ng-content>
    </ng-template>
  `,
})
export class TabHeaderComponent {
    // Das viewChild stellt das Template für das Tab-System bereit
    readonly template = viewChild.required<TemplateRef<any>>('tabHeaderTemplate');
}
