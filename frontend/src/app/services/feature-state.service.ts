import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, combineLatest } from 'rxjs';
import { FeatureDto, EnvironmentOverrideDto } from '../dtos';

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

  updateOverride(envId: string, update: Partial<EnvironmentOverrideDto>) {
    const current = this.draftState$.value;
    if (current) {
      const overrides = current.overrides.map(ovr => 
        ovr.environmentId === envId ? { ...ovr, ...update } : ovr
      );
      console.log('Updated overrides:', overrides);
      this.draftState$.next({ ...current, overrides });
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

  isOverrideFieldDirty(envId: string, fieldName: keyof EnvironmentOverrideDto): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origOvr = original.overrides.find(e => e.environmentId === envId);
        const draftOvr = draft.overrides.find(e => e.environmentId === envId);
        if (!origOvr || !draftOvr) return false;
        return JSON.stringify(origOvr[fieldName]) !== JSON.stringify(draftOvr[fieldName]);
      })
    );
  }

  isOverrideMetadataKeyDirty(envId: string, key: string): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origOvr = original.overrides.find(e => e.environmentId === envId);
        const draftOvr = draft.overrides.find(e => e.environmentId === envId);
        if (!origOvr || !draftOvr) return false;
        const origVal = origOvr.metadata[key];
        const draftVal = draftOvr.metadata[key];
        return JSON.stringify(origVal) !== JSON.stringify(draftVal);
      })
    );
  }

  isOverrideFlagDirty(envId: string, key: string): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origOvr = original.overrides.find(e => e.environmentId === envId);
        const draftOvr = draft.overrides.find(e => e.environmentId === envId);
        if (!origOvr || !draftOvr) return false;
        const origVal = origOvr.flags[key];
        const draftVal = draftOvr.flags[key];
        return origVal !== draftVal;
      })
    );
  }

  isOverrideDirty(envId: string): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origOvr = original.overrides.find(e => e.environmentId === envId);
        const draftOvr = draft.overrides.find(e => e.environmentId === envId);
        if (!origOvr || !draftOvr) return false;
        return JSON.stringify(origOvr) !== JSON.stringify(draftOvr);
      })
    );
  }
}
