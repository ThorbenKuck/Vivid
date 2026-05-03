import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'page-header',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.css']
})
export class PageHeaderComponent {
  @Input() collapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  constructor() {
  }

  onToggleSidebar() {
    this.toggleSidebar.emit();
  }
}
