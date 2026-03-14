import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, combineLatest } from 'rxjs';
import { TeamDto } from '../dtos';

@Injectable()
export class TeamStateService {
  private originalState$ = new BehaviorSubject<TeamDto | null>(null);
  private draftState$ = new BehaviorSubject<TeamDto | null>(null);

  team$ = this.draftState$.asObservable();
  
  isDirty$ = combineLatest([this.originalState$, this.draftState$]).pipe(
    map(([original, draft]) => {
      if (!original || !draft) return false;
      return JSON.stringify(original) !== JSON.stringify(draft);
    })
  );

  constructor() {}

  load(team: TeamDto) {
    const clone = JSON.parse(JSON.stringify(team));
    this.originalState$.next(clone);
    this.draftState$.next(JSON.parse(JSON.stringify(clone)));
  }

  updateDraft(update: Partial<TeamDto>) {
    const current = this.draftState$.value;
    if (current) {
      this.draftState$.next({ ...current, ...update });
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

  isFieldDirty(fieldName: keyof TeamDto): Observable<boolean> {
    return combineLatest([this.originalState$, this.draftState$]).pipe(
      map(([original, draft]) => {
        if (!original || !draft) return false;
        // @ts-ignore
        return JSON.stringify(original[fieldName]) !== JSON.stringify(draft[fieldName]);
      })
    );
  }
}
