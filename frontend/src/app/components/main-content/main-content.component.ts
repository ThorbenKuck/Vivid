import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { WebFeatureManagementService } from '../../services/web-feature-management.service';
import { EnvironmentService } from '../../services/environment.service';
import { FeatureStateService } from '../../services/feature-state.service';
import { FeatureDto, Page, TeamDto } from '../../dtos';
import { Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { FormInputComponent } from '../../shared/components/form-input/form-input.component';
import { GenericTableComponent } from '../../shared/components/generic-table/generic-table.component';
import { PaginationComponent } from '../../shared/components/pagination/pagination.component';
import { NoEnvironmentsWarningComponent } from '../../shared/components/no-environments-warning/no-environments-warning.component';

@Component({
  selector: 'app-main-content',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    ReactiveFormsModule, 
    TranslateModule, 
    FormInputComponent, 
    GenericTableComponent, 
    PaginationComponent,
    NoEnvironmentsWarningComponent
  ],
  templateUrl: './main-content.component.html',
  styleUrls: ['./main-content.component.css']
})
export class MainContentComponent implements OnInit {
  featuresPage$!: Observable<Page<FeatureDto>>;
  environments$ = this.envs.environments$;
  q = '';
  page = 0;
  size = 10;

  showAdd = false;
  addForm = this.fb.group({
    name: ['', Validators.required],
    description: ['']
  });

  constructor(
    private api: WebFeatureManagementService,
    private envs: EnvironmentService,
    private featureState: FeatureStateService,
    private router: Router,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload() {
    this.featuresPage$ = this.api.getAllFeatures(this.q, this.page, this.size);
  }

  search() { this.page = 0; this.reload(); }
  next() { this.page++; this.reload(); }
  prev() { if (this.page>0) { this.page--; this.reload(); } }

  getEnv(f: FeatureDto, envId: string) {
    return f.environments.find(env => env.environmentId === envId);
  }

  openDetails(f: FeatureDto) {
    this.router.navigate(['/feature', f.runningNumber]);
  }

  trackByTeamId(index: number, team: TeamDto): string {
    return team.id;
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
    this.api.createFeature({
      name: v.name!,
      description: v.description || undefined,
      tags: []
    }).subscribe(feature => {
      // Preload into details via router state for instant draft editing
      this.showAdd = false;
      this.addForm.reset();
      this.router.navigate(['/feature', feature.runningNumber], { state: { feature } });
    });
  }
}
