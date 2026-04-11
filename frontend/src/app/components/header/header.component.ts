import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DepartmentService } from '../../services/department.service';
import { ContextService } from '../../services/context.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  @Input() collapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  departments$ = this.departmentService.departments$;
  selectedDepartmentId$ = this.contextService.activeDepartmentId$;

  constructor(
    private departmentService: DepartmentService,
    private contextService: ContextService,
    private router: Router
  ) {
    this.departmentService.loadAll().subscribe();
  }

  onToggleSidebar() {
    this.toggleSidebar.emit();
  }

  selectDepartment(id: string | null) {
    this.contextService.setActiveDepartment(id);
  }

  addDepartment() {
    this.router.navigate(['/departments']);
  }
}
