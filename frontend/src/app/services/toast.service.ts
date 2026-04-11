import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toasts = signal<Toast[]>([]);
  public readonly toastsSignal = this.toasts.asReadonly();
  private nextId = 0;

  show(message: string, type: 'success' | 'error' | 'info' = 'info', duration: number = 10000) {
    const id = this.nextId++;
    const toast: Toast = { id, message, type, duration };
    this.toasts.update(currentToasts => [...currentToasts, toast]);

    if (duration > 0) {
      setTimeout(() => this.remove(id), duration);
    }
  }

  error(message: string, duration?: number) {
    this.show(message, 'error', duration);
  }

  success(message: string, duration?: number) {
    this.show(message, 'success', duration);
  }

  remove(id: number) {
    this.toasts.update(currentToasts => currentToasts.filter(t => t.id !== id));
  }
}
