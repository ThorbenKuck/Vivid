import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, combineLatest } from 'rxjs';
import { FeatureDto } from '../dtos';

@Injectable({
  providedIn: 'root'
})
export class FeatureStateService {
  private originalState$ = new BehaviorSubject<FeatureDto | null>(null);
  private draftState$ = new BehaviorSubject<FeatureDto | null>(null);

  feature$ = this.draftState$.asObservable();
  
  isDirty$ = combineLatest([this.originalState$, this.draftState$]).pipe(
    map(([original, draft]) => {
      if (!original || !draft) return false;
      return JSON.stringify(original) !== JSON.stringify(draft);
    })
  );

  constructor() {}

  load(feature: FeatureDto) {
    // Clone to avoid reference sharing
    const clone = JSON.parse(JSON.stringify(feature));
    this.originalState$.next(clone);
    this.draftState$.next(JSON.parse(JSON.stringify(clone)));
  }

  updateDraft(update: Partial<FeatureDto>) {
    const current = this.draftState$.value;
    if (current) {
      this.draftState$.next({ ...current, ...update });
    }
  }

  updateMetadata(key: string, value: any) {
    const current = this.draftState$.value;
    if (current) {
      const metadata = { ...current.metadata, [key]: value };
      this.draftState$.next({ ...current, metadata });
    }
  }

  removeMetadata(key: string) {
    const current = this.draftState$.value;
    if (current) {
      const metadata = { ...current.metadata };
      delete metadata[key];
      this.draftState$.next({ ...current, metadata });
    }
  }

  getOriginal() {
    return this.originalState$.value;
  }

  getDraft() {
    return this.draftState$.value;
  }

  revert() {
    const original = this.originalState$.value;
    if (original) {
      this.draftState$.next(JSON.parse(JSON.stringify(original)));
    }
  }

  isFieldDirty(fieldName: keyof FeatureDto): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        return JSON.stringify(original[fieldName]) !== JSON.stringify(draft[fieldName]);
      })
    );
  }

  isMetadataKeyDirty(key: string): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origVal = original.metadata[key];
        const draftVal = draft.metadata[key];
        return JSON.stringify(origVal) !== JSON.stringify(draftVal);
      })
    );
  }
}
