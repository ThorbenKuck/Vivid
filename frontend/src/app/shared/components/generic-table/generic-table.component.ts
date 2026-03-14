import { Component, Input, ContentChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-generic-table',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="list-container flex-col gap-sm">
      <div *ngFor="let item of items; let i = index" 
           class="list-item card" 
           [class.expanded]="item.__expanded"
           (click)="toggleExpand(item)">
        <ng-container *ngTemplateOutlet="rowTemplate; context: { \$implicit: item, index: i }">
        </ng-container>
        
        <div class="item-expand" *ngIf="item.__expanded">
          <ng-container *ngTemplateOutlet="expandTemplate; context: { \$implicit: item }">
          </ng-container>
        </div>
      </div>
      
      <div *ngIf="!items || items.length === 0" class="empty-state card text-center py-lg">
        <span class="material-symbols-rounded text-muted">info</span>
        <p class="text-secondary text-sm">No items found</p>
      </div>
    </div>
  `,
  styles: [`
    .list-container {
      width: 100%;
    }
    .list-item {
      padding: var(--spacing-sm) var(--spacing-md);
      cursor: pointer;
      position: relative;
    }
    .item-expand {
      margin-top: var(--spacing-sm);
      padding-top: var(--spacing-sm);
      border-top: var(--border-thin);
    }
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--spacing-sm);
      padding: var(--spacing-xl) 0;
    }
    .py-lg { padding-top: var(--spacing-xl); padding-bottom: var(--spacing-xl); }
    .text-center { text-align: center; }
  `]
})
export class GenericTableComponent {
  @Input() items: any[] = [];
  @Input() expandable: boolean = true;
  
  @ContentChild('rowTemplate') rowTemplate!: TemplateRef<any>;
  @ContentChild('expandTemplate') expandTemplate!: TemplateRef<any>;

  toggleExpand(item: any) {
    if (this.expandable) {
      item.__expanded = !item.__expanded;
    }
  }
}
