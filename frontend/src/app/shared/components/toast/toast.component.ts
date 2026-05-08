import { Component, inject } from '@angular/core';
import { ToastService } from '../../../services/toast.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css'
})
export class ToastComponent {
  private readonly toastService = inject(ToastService);

  get toasts() {
    return this.toastService.toastsSignal();
  }

  remove(id: number) {
    this.toastService.remove(id);
  }
}
