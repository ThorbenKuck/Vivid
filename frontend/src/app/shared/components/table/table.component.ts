import {
    Component,
    computed,
    ContentChild,
    contentChildren,
    ContentChildren, effect,
    input,
    output, signal,
    TemplateRef
} from '@angular/core';
import {Page, Pageable} from "./datastructure";
import {NgTemplateOutlet} from "@angular/common";
import {TableColumnComponent} from "./table-column.component"; // Dein Interface Pfad

@Component({
    selector: 'app-table',
    standalone: true,
    templateUrl: './table.component.html',
    imports: [
        NgTemplateOutlet
    ],
    styleUrl: './table.component.css'
})
export class TableComponent<T> {
    readonly page = input.required<Page<T>>();
    readonly pageChange = output<Pageable>();

    // Wir sammeln die Spalten ein
    readonly columnDefs = contentChildren(TableColumnComponent);
    @ContentChild('expandedRowTemplate') expandedRowTemplate?: TemplateRef<any>;

    expandedRowId = signal<any>(null);

    // Pagination Helper (gegen das NaN Problem)
    readonly currentPage = computed(() => (this.page().pageable?.page ?? 0) + 1);
    // Der Nutzer kann sagen: [rowIdKey]="'runningNumber'" oder [rowIdKey]="'uid'"
    readonly rowIdKey = input<keyof T | null>(null);

    readonly gridTemplate = computed(() => {
        const baseGrid = this.columnDefs()
            .map(col => col.width() === 'auto' ? '1fr' : col.width())
            .join(' ');

        // Wenn ein Detail-Template da ist, hängen wir 30px für den Pfeil dran
        return this.expandedRowTemplate ? `${baseGrid} 30px` : baseGrid;
    });

    // Hilfsfunktion für das Tracking und die Expansion
    getRowId(item: T): any {
        const key = this.rowIdKey();
        return key ? item[key] : item; // Fallback auf die Objektreferenz
    }

    toggleRow(item: T) {
        const id = this.getRowId(item);
        this.expandedRowId.update(current => current === id ? null : id);
    }

    isExpanded(item: T): boolean {
        return this.expandedRowId() === this.getRowId(item);
    }
}