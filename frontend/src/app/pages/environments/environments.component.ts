import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { EnvironmentService } from '../../services/environment.service';
import { EnvironmentDto } from '../../dtos';
import { TranslateModule } from '@ngx-translate/core';
import { FormInputComponent } from '../../shared/components/form-input/form-input.component';
import { GenericTableComponent } from '../../shared/components/generic-table/generic-table.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-environments',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, TranslateModule, FormInputComponent, GenericTableComponent, PaginationComponent],
  templateUrl: './environments.component.html',
  styleUrls: ['./environments.component.css']
})
export class EnvironmentsComponent implements OnInit {
  q = '';
  page = 0;
  size = 10;
  result: { content: EnvironmentDto[]; totalElements: number } = { content: [], totalElements: 0 };

  showAdd = false;
  addForm = this.fb.group({ name: ['', Validators.required], description: [''] });
  confirmDeleteId: string | null = null;

  constructor(private envs: EnvironmentService, private fb: FormBuilder, private router: Router) {}

  get totalPages() {
    return Math.ceil(this.result.totalElements / this.size);
  }

  get isLast() {
    return (this.page + 1) * this.size >= this.result.totalElements;
  }

  ngOnInit(): void {
    this.search();
  }

  search() {
    this.envs.search(this.q, this.page, this.size).subscribe((res: any) => {
      this.result = { content: res.content, totalElements: res.totalElements };
    });
  }

  toggleAdd() { this.showAdd = !this.showAdd; }

  add() {
    const v = this.addForm.value;
    if (!v.name) return;
    this.envs.create({ name: v.name!, description: v.description || undefined }).subscribe(env => {
      this.showAdd = false;
      this.addForm.reset();
      // Select and navigate to details for immediate follow-up editing
      const id = env.id!;
      this.envs.select(id);
      this.router.navigate(['/environment', id]);
    });
  }

  askDelete(id: string) { this.confirmDeleteId = id; }
  cancelDelete() { this.confirmDeleteId = null; }
  confirmDelete() {
    if (!this.confirmDeleteId) return;
    this.envs.delete(this.confirmDeleteId).subscribe(() => {
      this.confirmDeleteId = null;
      this.search();
    });
  }
}
