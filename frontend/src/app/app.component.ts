import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PageHeaderComponent} from './components/header/page-header.component';
import {SidebarComponent} from './components/sidebar/sidebar.component';
import {ActivatedRoute, NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {EnvironmentService} from './services/environment.service';
import {LoadingService} from './services/loading.service';
import {filter} from 'rxjs/operators';
import {LoadingIndicator} from "./shared/components/loading-indicator/loading-indicator";
import {catchError, finalize, forkJoin, map, Observable, of, shareReplay, take} from "rxjs";
import {ToastComponent} from './shared/components/toast/toast.component';
import {PermissionService} from "./services/permission.service";
import {ModalComponent} from "./shared/components/modal/modal.component";
import {LanguageService} from "./services/language.service";

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [CommonModule, PageHeaderComponent, SidebarComponent, RouterOutlet, LoadingIndicator, ToastComponent, ModalComponent],
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    sidebarCollapsed = false;
    showShell$: Observable<boolean>;

    constructor(
        private languageService: LanguageService,
        private envs: EnvironmentService,
        protected loadingService: LoadingService,
        private permissionService: PermissionService,
        private router: Router,
        private activatedRoute: ActivatedRoute,
    ) {
        loadingService.setApplicationLoading(true);
        this.showShell$ = this.router.events.pipe(
            filter(event => event instanceof NavigationEnd),
            map(() => {
                let route = this.activatedRoute.firstChild;
                while (route?.firstChild) {
                    route = route.firstChild;
                }

                const data = route?.snapshot.data;
                return data?.['hideShell'] !== true;
            }),
            shareReplay(1)
        );
    }

    ngOnInit(): void {
        // 1. Shell-Status ermitteln (NavigationEnd)
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd),
            // Wir warten, bis wir wissen, ob wir die Shell zeigen
            take(1)
        ).subscribe(() => {
            this.initializeApp();
        });

        this.showShell$
            .pipe(filter((show): show is boolean => show !== undefined))
            .subscribe(show => {
                if (show) {
                    this.envs.loadAll().subscribe();
                }

                this.loadingService.setApplicationLoading(false);
            });
    }

    private initializeApp() {
        forkJoin({
            permissions: this.permissionService.fetchPermissions().pipe(catchError(() => of(null))),
            envs: this.envs.loadAll().pipe(catchError(() => of([])))
        }).pipe(
            finalize(() => {
                this.loadingService.setApplicationLoading(false);
            })
        ).subscribe();
    }

    toggleSidebar() {
        this.sidebarCollapsed = !this.sidebarCollapsed;
    }
}