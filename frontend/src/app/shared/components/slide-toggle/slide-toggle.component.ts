import { Component, input, model, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'slide-toggle',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './slide-toggle.component.html',
    styleUrls: ['./slide-toggle.component.css']
})
export class SlideToggleComponent {
    // Das Label, das neben dem Toggle angezeigt wird
    label = input<string>('');

    // Der Status des Toggles (An oder Aus)
    checked = model<boolean>(false);

    disabled = input<boolean>(false);

    // Event, das gefeuert wird, wenn der User klickt
    onToggle = output<boolean>();

    toggle(): void {
        if (this.disabled()) return;
        const newState = !this.checked();
        this.checked.set(newState);
        this.onToggle.emit(newState);
    }
}