import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EnvironmentService } from '../../services/environment.service';
import { DepartmentService } from '../../services/department.service';
import { ContextService } from '../../services/context.service';
import { TranslateModule } from '@ngx-translate/core';
import {EnvironmentDto} from "../../dtos";

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

  environments$ = this.envs.environments$;
  selectedEnvironment$ = this.envs.selectedEnvironment$;

  constructor(
    private envs: EnvironmentService,
    private departmentService: DepartmentService,
    private contextService: ContextService,
    private router: Router
  ) {
    this.departmentService.loadAll().subscribe();

    // Re-load environments when department changes
    this.contextService.activeDepartmentId$.subscribe(deptId => {
      if (deptId) {
        this.envs.loadAll().subscribe();
      }
    });
  }

  onToggleSidebar() {
    this.toggleSidebar.emit();
  }

  selectDepartment(id: string | null) {
    this.contextService.setActiveDepartment(id);
    // When department changes, we might want to clear the environment selection
    this.envs.select(null);
  }

  selectEnvironmentById(environmentId: string | null) {
    this.envs.selectById(environmentId);
  }

  selectEnvironment(env: EnvironmentDto | null) {
    this.envs.select(env);
  }


  addDepartment() {
    this.router.navigate(['/departments']);
  }

  addEnvironment() {
    this.router.navigate(['/environments']);
  }
}
