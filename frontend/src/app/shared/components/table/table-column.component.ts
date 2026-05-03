import {Component, ContentChild, input, TemplateRef} from "@angular/core";

@Component({
    selector: 'table-column',
    standalone: true,
    template: ``, // Nur ein Container für Metadaten
})
export class TableColumnComponent {
    key = input.required<string>(); // z.B. 'name'
    label = input.required<string>(); // Header Text
    width = input<string>('auto'); // Breite (z.B. '40px' oder '1fr')
    center = input<boolean>(false);

    // Wir fangen das Cell-Template ein
    @ContentChild(TemplateRef) cellTemplate!: TemplateRef<any>;
}