import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pagination flex items-center justify-center gap-md">
      <button 
        class="icon-btn" 
        [disabled]="page === 0" 
        (click)="onPrev()" 
        title="Previous">
        <span class="material-symbols-rounded">chevron_left</span>
      </button>
      
      <span class="page-info text-sm text-secondary">
        {{ page + 1 }} / {{ totalPages }}
      </span>
      
      <button 
        class="icon-btn" 
        [disabled]="isLast" 
        (click)="onNext()" 
        title="Next">
        <span class="material-symbols-rounded">chevron_right</span>
      </button>
    </div>
  `,
  styles: [`
    .pagination {
      margin-top: var(--spacing-lg);
    }
    .icon-btn {
      width: 32px;
      height: 32px;
      padding: 0;
      border-radius: var(--radius-sm);
    }
    .page-info {
      font-weight: 500;
      min-width: 60px;
      text-align: center;
    }
  `]
})
export class PaginationComponent {
  @Input() page: number = 0;
  @Input() totalPages: number = 0;
  @Input() isLast: boolean = false;
  
  @Output() prev = new EventEmitter<void>();
  @Output() next = new EventEmitter<void>();

  onPrev() { this.prev.emit(); }
  onNext() { this.next.emit(); }
}
