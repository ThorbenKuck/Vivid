import { Component, input, computed } from '@angular/core';
import { DatePipe } from '@angular/common';
import {TooltipDirective} from "../../directives/tooltip.directive";

@Component({
    selector: 'date-time',
    standalone: true,
    imports: [DatePipe, TooltipDirective], // Wichtig: DatePipe importieren
    template: `
        <div class="date-container" [vividTooltip]="value()">
      <span class="human-readable">
        {{ value() | date: format() }}
      </span>
            <span class="badge">
        {{ value() | date: 'HH:mm' }}
      </span>
        </div>
    `,
    styles: [
        `
            :host {
                display: inline-block;
            }

            .date-container {
                display: flex;
                align-items: center;
                gap: var(--spacing-xs);
                padding: var(--spacing-xs) var(--spacing-sm);
                border-radius: var(--radius-sm);
                transition: background-color 0.2s;
            }

            .date-container:hover {
                background: var(--surface-elevated, #2a2a2a);
            }

            .human-readable {
                color: var(--text-primary, #fff);
                font-weight: 500;
            }

            .badge {
                font-size: var(--font-size-sm);
                background: var(--surface-color, #1a1a1a);
                border: 1px solid var(--border-color, #333);
                color: var(--accent-mint, #2ecc71);
                padding: 2px 6px;
                border-radius: 4px;
                opacity: 0.7;
                transition: opacity 0.2s;
            }

            .date-container:hover .badge {
                opacity: 1;
                border-color: var(--accent-mint);
            }
        `
    ]
})
export class DateTimeComponent {
    // Nimmt Datum als String, Zahl oder Date-Objekt entgegen
    value = input.required<string>();

    // Optionales Format (Standard: Menschlich lesbar)
    format = input<string>('dd.MM.yyyy');

    // Der volle Zeitstempel für das "Badge" oder den nativen Tooltip beim Hover
    fullTimestamp = computed(() => {
        const d = new Date(this.value());
        return d.toLocaleString(); // Zeigt Datum + Uhrzeit beim Hover
    });
}