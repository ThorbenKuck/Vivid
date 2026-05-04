import { Injectable, signal } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface ModalConfig {
    title: string;
    message: string;
    type: 'confirm' | 'prompt';
}

@Injectable({ providedIn: 'root' })
export class ModalService {
    // Das Signal hält nur die Daten. Ist es null, ist das Modal zu.
    activeConfig = signal<ModalConfig | null>(null);

    private responseSubject = new Subject<any>();

    confirm(title: string, message: string): Observable<boolean> {
        this.responseSubject = new Subject<boolean>(); // Neues Subject für diesen Call
        this.activeConfig.set({ title, message, type: 'confirm' });
        return this.responseSubject.asObservable();
    }

    prompt(title: string, message: string): Observable<string | null> {
        this.responseSubject = new Subject<string | null>(); // Neues Subject für diesen Call
        this.activeConfig.set({ title, message, type: 'prompt' });
        return this.responseSubject.asObservable();
    }

    submit(result: string | boolean | null) {
        const subject = this.responseSubject;
        this.activeConfig.set(null); // Erst schließen (stoppt UI-Loops)
        subject.next(result);
        subject.complete();
    }

    dismiss() {
        const subject = this.responseSubject;
        this.activeConfig.set(null);
        subject.next(null);
        subject.complete();
    }
}
