import {Injectable} from '@angular/core';
import {BehaviorSubject, filter, finalize, Observable, tap} from 'rxjs';
import {EnvironmentCreateRequest, EnvironmentDto} from '../dtos';
import {HttpService} from './http.service';
import {DepartmentService} from "./department.service";

@Injectable({providedIn: 'root'})
export class EnvironmentService {
    private readonly baseUrl = '/api/web/environments';

    private environmentsSubject = new BehaviorSubject<EnvironmentDto[] | null>(null);
    environments$ = this.environmentsSubject.asObservable().pipe(
        filter((value): value is EnvironmentDto[] => value !== null)
    );

    private selectedEnvironmentSubject = new BehaviorSubject<EnvironmentDto | null>(null);
    selectedEnvironment$ = this.selectedEnvironmentSubject.asObservable();

    private loadingAll = false;

    constructor(
        private http: HttpService,
        private departmentService: DepartmentService
    ) {
        departmentService.departments$.subscribe(d => {
            this.select(null);
            // this.environments
        })
    }

    loadAll(): Observable<EnvironmentDto[]> {
        if (this.environmentsSubject.value === null && !this.loadingAll) {
            this.loadingAll = true;

            this.http.get<EnvironmentDto[]>(`${this.baseUrl}/all`)
                .pipe(
                    tap((list) => {
                        this.environmentsSubject.next(list);
                        this.initialize(list);
                    }),
                    finalize(() => {
                        this.loadingAll = false;
                    })
                )
                .subscribe({
                    error: (error) => {
                        console.error('Error fetching environments:', error);
                        this.environmentsSubject.next([]);
                    }
                });
        }

        return this.environments$;
    }

    search(q: string, page: number, size: number) {
        return this.http.get<any>(this.baseUrl, {q, page, size});
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

    selectById(id: string | null) {
        if (!id) {
            this.select(null);
        } else {
            const env = this.environmentsSubject.value?.find(env => env.id === id);
            if (env) {
                this.select(env);
            }
        }
    }

    select(env: EnvironmentDto | null) {
        console.log("Selecting environment:", env);
        this.selectedEnvironmentSubject.next(env);
    }

    private initialize(environments: EnvironmentDto[]) {
        if (environments.length === 1) {
            this.selectedEnvironmentSubject.next(environments[0] ?? null);
        } else if (environments.length === 0) {
            this.selectedEnvironmentSubject.next(null);
        } else {
            this.selectedEnvironmentSubject.next(null);
        }
    }

    refresh() {
        this.http.get<EnvironmentDto[]>(`${this.baseUrl}/all`).subscribe({
            next: (list) => {
                this.environmentsSubject.next(list);
            },
            error: (error) => {
                console.error('Error fetching environments:', error);
            }
        });
    }
}