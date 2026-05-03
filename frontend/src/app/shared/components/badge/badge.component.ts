import { Component, input, computed } from '@angular/core';

@Component({
    selector: 'badge',
    standalone: true,
    templateUrl: './badge.component.html',
    styleUrl: './badge.component.css'
})
export class BadgeComponent {
    // Inputs für Farbe und Stil
    content = input<string | number | null>(); // Der Wert im Badge
    variant = input<'primary' | 'success' | 'error'>('primary');
    overlap = input<boolean>(true);
    size = input<'sm' | 'md' | 'lg'>('md');

    // Berechnete CSS-Klasse
    badgeClass = computed(() => `badge badge-${this.variant()} badge-${this.size()}`);
}