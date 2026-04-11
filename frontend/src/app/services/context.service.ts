import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ContextService {
  private activeDepartmentIdSubject = new BehaviorSubject<string | null>(localStorage.getItem('vivid_department_id'));
  activeDepartmentId$ = this.activeDepartmentIdSubject.asObservable();

  private activeEnvironmentIdSubject = new BehaviorSubject<string | null>(localStorage.getItem('vivid_environment_id'));
  activeEnvironmentId$ = this.activeEnvironmentIdSubject.asObservable();

  constructor() {}

  setActiveDepartment(id: string | null) {
    if (id) {
      localStorage.setItem('vivid_department_id', id);
    } else {
      localStorage.removeItem('vivid_department_id');
    }
    this.activeDepartmentIdSubject.next(id);
  }

  getActiveDepartmentId(): string | null {
    return this.activeDepartmentIdSubject.value;
  }

  setActiveEnvironment(id: string | null) {
    if (id) {
      localStorage.setItem('vivid_environment_id', id);
    } else {
      localStorage.removeItem('vivid_environment_id');
    }
    this.activeEnvironmentIdSubject.next(id);
  }

  getActiveEnvironmentId(): string | null {
    return this.activeEnvironmentIdSubject.value;
  }
}
