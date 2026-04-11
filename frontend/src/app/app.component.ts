import {Component, OnInit, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './components/header/header.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { EnvironmentService } from './services/environment.service';
import { LoadingService } from './services/loading.service';
import { delay, filter } from 'rxjs/operators';
import { LoadingIndicator } from "./shared/components/loading-indicator/loading-indicator";
import { map, Observable, shareReplay } from "rxjs";
import { ToastComponent } from './shared/components/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, HeaderComponent, SidebarComponent, RouterOutlet, LoadingIndicator, ToastComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  sidebarCollapsed = false;
  loading$ = this.loadingService.loading$.pipe(delay(0));
  showShell$: Observable<boolean | undefined>;
  initialized$ = signal(false);

  constructor(
      private envs: EnvironmentService,
      private loadingService: LoadingService,
      private router: Router,
      private activatedRoute: ActivatedRoute,
  ) {
    this.showShell$ = this.router.events.pipe(
        filter(event => event instanceof NavigationEnd),
        map(() => {
          let route = this.activatedRoute.firstChild;
          while (route?.firstChild) {
            route = route.firstChild;
          }

          if (!route) {
            return undefined;
          }

          const data = route.snapshot.data;

          if ('showShell' in data) {
            return data['showShell'] === true;
          }

          return true;
        }),
        shareReplay(1)
    );
  }

  ngOnInit(): void {
    this.showShell$
        .pipe(filter((show): show is boolean => show !== undefined))
        .subscribe(show => {
          if (show) {
            this.envs.loadAll().subscribe();
          }

          this.initialized$.set(true);
          console.log("Initialized app with showShell: " + show);
        });
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}