import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {EnvironmentService} from '../../services/environment.service';
import {EnvironmentDto} from '../../dtos';
import {TranslateModule} from '@ngx-translate/core';
import {FormInputComponent} from '../../shared/components/form-input/form-input.component';
import {HasPermissionDirective} from '../../shared/directives/has-permission.directive';
import {LoadingIndicator} from "../../shared/components/loading-indicator/loading-indicator";
import {TableColumnComponent} from "../../shared/components/table/table-column.component";
import {TableComponent} from "../../shared/components/table/table.component";
import {Page, Pageable} from "../../shared/components/table/datastructure";
import {ContentHeaderComponent} from "../../shared/components/content-header/content-header.component";

@Component({
    selector: 'app-environments',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, TranslateModule, FormInputComponent, HasPermissionDirective, LoadingIndicator, TableColumnComponent, TableComponent, ContentHeaderComponent],
    templateUrl: './environments.component.html',
    styleUrls: ['./environments.component.css']
})
export class EnvironmentsComponent implements OnInit {
    q = '';
    page = 0;
    size = 10;
    result = signal<Page<EnvironmentDto> | null>(null);

    private envs = inject(EnvironmentService)
    private fb = inject(FormBuilder)
    private router = inject(Router)

    showAdd = false;
    confirmDeleteId: string | null = null;
    addForm = this.fb.group({name: ['', Validators.required], description: ['']});

    open(id: string) {
        this.router.navigate(['/environments', id]);
    }

    ngOnInit(): void {
        this.search();
    }

    search() {
        this.envs.search(this.q, this.page, this.size).subscribe(res => {
            this.result.set(res);
        });
    }

    toggleAdd() {
        this.showAdd = !this.showAdd;
    }

    add() {
        const v = this.addForm.value;
        if (!v.name) return;
        this.envs.create({name: v.name!, description: v.description || undefined}).subscribe(env => {
            this.showAdd = false;
            this.addForm.reset();
            // Select and navigate to details for immediate follow-up editing
            const id = env.id!;
            this.envs.select(env);
            this.router.navigate(['/environments', id]);
        });
    }

    askDelete(id: string) {
        this.confirmDeleteId = id;
    }

    cancelDelete() {
        this.confirmDeleteId = null;
    }

    confirmDelete() {
        if (!this.confirmDeleteId) return;
        this.envs.delete(this.confirmDeleteId).subscribe(() => {
            this.confirmDeleteId = null;
            this.search();
        });
    }

    protected loadData($event: Pageable) {
        console.error("TODO: Implement loadData", $event)
    }
}
