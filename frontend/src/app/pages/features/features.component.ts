import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Observable} from 'rxjs';
import {WebFeatureManagementService} from '../../services/web-feature-management.service';
import {EnvironmentService} from '../../services/environment.service';
import {FeatureDto} from '../../dtos';
import {Router} from '@angular/router';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {FormInputComponent} from '../../shared/components/form-input/form-input.component';
import {HasPermissionDirective} from '../../shared/directives/has-permission.directive';
import {TableComponent} from "../../shared/components/table/table.component";
import {Page, Pageable} from "../../shared/components/table/datastructure";
import {TableColumnComponent} from "../../shared/components/table/table-column.component";
import {EnvStatusComponent} from "../../shared/components/env-status/env-status.component";
import {LoadingIndicator} from "../../shared/components/loading-indicator/loading-indicator";
import {ContentHeaderComponent} from "../../shared/components/content-header/content-header.component";

@Component({
    selector: 'app-main-content',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        FormInputComponent,
        HasPermissionDirective,
        TableComponent,
        TableColumnComponent,
        EnvStatusComponent,
        LoadingIndicator,
        ContentHeaderComponent,
    ],
    templateUrl: './features.component.html',
    styleUrls: ['./features.component.css']
})
export class FeaturesComponent implements OnInit {
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
        private router: Router,
        private fb: FormBuilder,
    ) {
    }

    ngOnInit(): void {
        this.reload();
    }

    reload() {
        this.featuresPage$ = this.api.getAllFeatures(this.q, this.page, this.size);
    }

    search() {
        this.page = 0;
        this.reload();
    }

    next() {
        this.page++;
        this.reload();
    }

    prev() {
        if (this.page > 0) {
            this.page--;
            this.reload();
        }
    }

    isResolvedEnabled(f: FeatureDto, envId: string): boolean {
        const override = f.overrides.find(env => env.environmentId === envId);
        if (override && override.enabled !== null && override.enabled !== undefined) {
            return override.enabled;
        }
        return f.enabled;
    }

    openDetails(f: FeatureDto) {
        this.router.navigate(['/features', f.runningNumber]);
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
            tags: [],
            enabled: false,
            flags: {},
            metadata: {}
        }).subscribe(feature => {
            // Preload into details via router state for instant draft editing
            this.showAdd = false;
            this.addForm.reset();
            this.router.navigate(['/features', feature.runningNumber], {state: {feature}});
        });
    }

    loadData(pageable: Pageable) {
        // TODO: Add request to Backend
        console.log(pageable);
    }

    protected handle($event: Pageable) {
        console.log($event);
    }

    protected readonly open = open;
}
