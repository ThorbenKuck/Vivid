import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { EnvironmentCreateRequest, EnvironmentDto } from '../dtos';
import { HttpService } from './http.service';

@Injectable({ providedIn: 'root' })
export class EnvironmentService {
  private readonly baseUrl = '/api/web/environments';

  private environmentsSubject = new BehaviorSubject<EnvironmentDto[]>([]);
  environments$ = this.environmentsSubject.asObservable();

  private selectedEnvironmentIdSubject = new BehaviorSubject<string | null>(null);
  selectedEnvironmentId$ = this.selectedEnvironmentIdSubject.asObservable();

  constructor(private http: HttpService) {}

  loadAll(): Observable<EnvironmentDto[]> {
    return this.http.get<EnvironmentDto[]>(`${this.baseUrl}/all`).pipe(
      tap(list => {
        this.environmentsSubject.next(list);
        // initial selection logic
        if (list.length === 1) {
          this.selectedEnvironmentIdSubject.next(list[0].id ?? null);
        } else if (list.length === 0) {
          this.selectedEnvironmentIdSubject.next(null);
        } else {
          this.selectedEnvironmentIdSubject.next(null);
        }
      })
    );
  }

  search(q: string, page: number, size: number) {
    return this.http.get<any>(this.baseUrl, { q, page, size });
  }

  getById(id: string): Observable<EnvironmentDto> {
    return this.http.get<EnvironmentDto>(`${this.baseUrl}/${id}`);
  }

  create(req: EnvironmentCreateRequest): Observable<EnvironmentDto> {
    return this.http.post<EnvironmentDto>(this.baseUrl, req).pipe(
      tap(() => this.refresh())
    );
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      tap(() => this.refresh())
    );
  }

  select(id: string | null) {
    this.selectedEnvironmentIdSubject.next(id);
  }

  private refresh() {
    this.http.get<EnvironmentDto[]>(`${this.baseUrl}/all`).subscribe(list => this.environmentsSubject.next(list));
  }
}
