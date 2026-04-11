import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { DepartmentDto } from '../../../dtos/DepartmentDto';
import { DepartmentService } from '../../../services/department.service';
import { Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { FormInputComponent } from '../../../shared/components/form-input/form-input.component';
import { GenericTableComponent } from '../../../shared/components/generic-table/generic-table.component';

@Component({
  selector: 'app-department-overview',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    FormInputComponent,
    GenericTableComponent
  ],
  templateUrl: './department-overview.component.html',
  styleUrls: ['./department-overview.component.css']
})
export class DepartmentOverviewComponent implements OnInit {
  departments$!: Observable<DepartmentDto[]>;
  showAdd = false;
  addForm = this.fb.group({
    name: ['', Validators.required],
    description: ['']
  });

  constructor(
    private departmentService: DepartmentService,
    private router: Router,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.departments$ = this.departmentService.departments$;
    this.departmentService.loadAll().subscribe();
  }

  openDetails(dept: DepartmentDto) {
    this.router.navigate(['/department', dept.id]);
  }

  toggleAdd() {
    this.showAdd = !this.showAdd;
    if (!this.showAdd) {
      this.addForm.reset();
    }
  }

  add() {
    const v = this.addForm.value;
    if (!v.name) return;
    this.departmentService.create({
      name: v.name!,
      description: v.description || undefined
    }).subscribe(() => {
      this.toggleAdd();
    });
  }
}
