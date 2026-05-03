import { Component, input, model, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TooltipDirective} from "../../directives/tooltip.directive";

export interface ToggleOption {
    label: string;
    value: any;
    icon?: string;
    tooltip?: string;
}

@Component({
    selector: 'button-toggle',
    standalone: true,
    imports: [CommonModule, TooltipDirective],
    templateUrl: './button-toggle.component.html',
    styleUrls: ['./button-toggle.component.css']
})
export class VividButtonToggleComponent {
    // Die Liste der verfügbaren Optionen
    options = input.required<ToggleOption[]>();

    // Der aktuell ausgewählte Wert (Two-Way-Binding!)
    value = model<any>();

    // Event, falls man zusätzlich auf Änderungen reagieren will
    change = output<any>();

    selectOption(val: any) {
        this.value.set(val);
        this.change.emit(val);
    }
}