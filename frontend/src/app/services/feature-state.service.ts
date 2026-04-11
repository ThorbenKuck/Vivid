import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, combineLatest } from 'rxjs';
import { FeatureDto, FeatureEnvironmentDto } from '../dtos';

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

  updateEnvState(envId: string, update: Partial<FeatureEnvironmentDto>) {
    const current = this.draftState$.value;
    if (current) {
      const environments = current.environments.map(env => 
        env.environmentId === envId ? { ...env, ...update } : env
      );
      this.draftState$.next({ ...current, environments });
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

  isEnvFieldDirty(envId: string, fieldName: keyof FeatureEnvironmentDto): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origEnv = original.environments.find(e => e.environmentId === envId);
        const draftEnv = draft.environments.find(e => e.environmentId === envId);
        if (!origEnv || !draftEnv) return false;
        return JSON.stringify(origEnv[fieldName]) !== JSON.stringify(draftEnv[fieldName]);
      })
    );
  }

  isEnvMetadataKeyDirty(envId: string, key: string): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origEnv = original.environments.find(e => e.environmentId === envId);
        const draftEnv = draft.environments.find(e => e.environmentId === envId);
        if (!origEnv || !draftEnv) return false;
        const origVal = origEnv.metadata[key];
        const draftVal = draftEnv.metadata[key];
        return JSON.stringify(origVal) !== JSON.stringify(draftVal);
      })
    );
  }

  isEnvFlagDirty(envId: string, key: string): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origEnv = original.environments.find(e => e.environmentId === envId);
        const draftEnv = draft.environments.find(e => e.environmentId === envId);
        if (!origEnv || !draftEnv) return false;
        const origVal = origEnv.flags[key];
        const draftVal = draftEnv.flags[key];
        return origVal !== draftVal;
      })
    );
  }

  isEnvDirty(envId: string): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        const origEnv = original.environments.find(e => e.environmentId === envId);
        const draftEnv = draft.environments.find(e => e.environmentId === envId);
        if (!origEnv || !draftEnv) return false;
        return JSON.stringify(origEnv) !== JSON.stringify(draftEnv);
      })
    );
  }
}
