import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {ModalService} from "../../../services/modal.service";

@Component({
    selector: 'modal',
    standalone: true,
    imports: [CommonModule, FormsModule],
    changeDetection: ChangeDetectionStrategy.OnPush, // Extrem wichtig für CPU-Last!
    templateUrl: './modal.component.html',
    styleUrls: ['./modal.component.css']
})
export class ModalComponent {
    inputValue = signal(''); // Auch hier ein Signal für Stabilität
    service = inject(ModalService)

    onConfirm() {
        const config = this.service.activeConfig();
        if (config?.type === 'prompt') {
            this.service.submit(this.inputValue());
            this.inputValue.set('');
        } else {
            this.service.submit(true);
        }
    }

    onCancel() {
        this.service.dismiss();
        this.inputValue.set('');
    }
}