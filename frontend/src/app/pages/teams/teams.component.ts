import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TeamManagementService } from '../../services/team-management.service';
import { TeamDto } from '../../dtos';
import { FormInputComponent } from '../../shared/components/form-input/form-input.component';
import { GenericTableComponent } from '../../shared/components/generic-table/generic-table.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { HasPermissionDirective } from '../../shared/directives/has-permission.directive';
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'app-teams',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, TranslateModule, FormInputComponent, GenericTableComponent, PaginationComponent, HasPermissionDirective],
  templateUrl: './teams.component.html',
  styleUrls: ['./teams.component.css']
})
export class TeamsComponent implements OnInit {
  q = '';
  page = 0;
  size = 10;
  result: { content: TeamDto[]; totalElements: number } = { content: [], totalElements: 0 };

  showAdd = false;
  addForm = this.fb.group({ name: ['', Validators.required], description: [''] });

  constructor(private teams: TeamManagementService, private fb: FormBuilder, private router: Router) {}

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
    this.teams.getTeams(this.q, this.page, this.size).subscribe((res: any) => {
      this.result = { content: res.content, totalElements: res.totalElements };
    });
  }

  toggleAdd() { this.showAdd = !this.showAdd; }

  add() {
    const v = this.addForm.value;
    if (!v.name) return;
    this.teams.createTeam({ name: v.name!, description: v.description || undefined }).subscribe(team => {
      this.showAdd = false;
      this.addForm.reset();
      this.router.navigate(['/team', team.id]);
    });
  }

  navigate(team: TeamDto) {
    this.router.navigate(['/team', team.id]);
  }

  trackByTeamId(index: number, team: TeamDto): string {
    return team.id;
  }
}
