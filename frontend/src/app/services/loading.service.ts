import {Injectable, signal} from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  // Privates Signal für den Status
  private readonly _isFetching = signal(false);
  private readonly _isApplicationLoading = signal(false);

  // Öffentliches Signal (Read-only für Komponenten)
  readonly isFetching = this._isFetching.asReadonly();
  readonly isApplicationLoading = this._isApplicationLoading.asReadonly();

  private debounceTimeout?: any;

  setFetching(value: boolean) {
    if (value) {
      // Wenn ein Request startet, warte 250ms bevor du die Bar anzeigst
      if (!this.debounceTimeout) {
        this.debounceTimeout = setTimeout(() => {
          this._isFetching.set(true);
        }, 250);
      }
    } else {
      // Wenn fertig: Timer sofort löschen und Status auf false
      clearTimeout(this.debounceTimeout);
      this.debounceTimeout = undefined;
      this._isFetching.set(false);
    }
  }

  setApplicationLoading(value: boolean) {
    if (value) {
      // Wenn ein Request startet, warte 250ms bevor du die Bar anzeigst
      if (!this.debounceTimeout) {
        this.debounceTimeout = setTimeout(() => {
          this._isApplicationLoading.set(true);
        }, 250);
      }
    } else {
      // Wenn fertig: Timer sofort löschen und Status auf false
      clearTimeout(this.debounceTimeout);
      this.debounceTimeout = undefined;
      this._isApplicationLoading.set(false);
    }
  }
}
