import {Component, computed, input} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
    selector: 'env-status',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './env-status.component.html',
    styleUrl: './env-status.component.css'
})
export class EnvStatusComponent {

    active = input.required<boolean>();
    haloType = input<'primary' | 'secondary' | 'success' | 'error' | 'warning' | null>(null);

    haloClass = computed(() => `halo-${this.haloType()}`);

}
