import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { DepartmentDto } from '../dtos/DepartmentDto';
import { HttpService } from './http.service';
import { ContextService } from './context.service';
import {DepartmentCreateRequest} from "../dtos/DepartmentCreateRequest";

@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private readonly baseUrl = '/api/web/departments';

  private departmentsSubject = new BehaviorSubject<DepartmentDto[]>([]);
  departments$ = this.departmentsSubject.asObservable();

  constructor(
    private http: HttpService,
    private contextService: ContextService
  ) {}

  loadAll(): Observable<DepartmentDto[]> {
    return this.http.get<DepartmentDto[]>(this.baseUrl).pipe(
      tap(list => {
        this.departmentsSubject.next(list);
        
        // Auto-selection: If only one Department exists, select it automatically.
        if (list.length === 1) {
          this.contextService.setActiveDepartment(list[0].id);
        } else if (!this.contextService.getActiveDepartmentId() && list.length > 0) {
          // If none selected but some exist, we could select the first one or leave it
          // The requirement says "select it automatically" only if only one exists.
        }
      })
    );
  }

  getById(id: string): Observable<DepartmentDto> {
    return this.http.get<DepartmentDto>(`${this.baseUrl}/${id}`);
  }

  create(req: DepartmentCreateRequest): Observable<DepartmentDto> {
    return this.http.post<DepartmentDto>(this.baseUrl, req).pipe(
      tap(() => this.refresh())
    );
  }

  refresh() {
    this.http.get<DepartmentDto[]>(this.baseUrl)
        .subscribe(list => this.departmentsSubject.next(list));
  }
}
